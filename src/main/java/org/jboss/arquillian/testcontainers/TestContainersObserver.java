/*
 * Copyright The Arquillian Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.arquillian.testcontainers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.annotation.ClassScoped;
import org.jboss.arquillian.test.spi.event.enrichment.AfterEnrichment;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.testcontainers.api.ContainerRequired;
import org.testcontainers.DockerClientFactory;

@SuppressWarnings("unused")
class TestContainersObserver {
    @Inject
    @ClassScoped
    private InstanceProducer<TestcontainerRegistry> containerRegistry;

    @Inject
    private Instance<ContainerRegistry> registry;

    /**
     * This first checks if the {@link ContainerRequired} annotation is present on the test class failing if necessary. It
     * then creates the {@link TestcontainerRegistry} and stores it in a {@link ClassScoped} instance.
     *
     * @param beforeClass the before class event
     *
     * @throws Throwable if an error occurs
     */
    public void createContainer(@Observes(precedence = 500) BeforeClass beforeClass) throws Throwable {
        final TestClass javaClass = beforeClass.getTestClass();
        final ContainerRequired dockerRequired = javaClass.getAnnotation(ContainerRequired.class);
        if (dockerRequired != null) {
            if (!isDockerAvailable()) {
                var throwable = dockerRequired.value();
                final var overrideClass = System.getProperty("org.arquillian.testcontainers.docker.required.exception");
                if (overrideClass != null && !overrideClass.isBlank()) {
                    Class<?> override = Class.forName(overrideClass);
                    if (Throwable.class.isAssignableFrom(override)) {
                        throwable = (Class<? extends Throwable>) override;
                    }
                }
                throw createException(throwable);
            }
        }
        final TestcontainerRegistry instances = new TestcontainerRegistry();
        containerRegistry.set(instances);
    }

    /**
     * Stops all containers, even ones not managed via Arquillian, after the test is complete
     *
     * @param afterClass the after class event
     */
    public void stopContainer(@Observes AfterClass afterClass) {
        TestcontainerRegistry registry = containerRegistry.get();
        if (registry != null) {
            for (TestcontainerDescription container : registry) {
                container.instance.stop();
            }
        }
    }

    /**
     * Starts all containers after enrichment is done. This happens after the {@link ContainerInjectionTestEnricher} is
     * invoked.
     *
     * @param event the after enrichment event
     */
    public void startContainer(@Observes(precedence = 500) final AfterEnrichment event) {
        TestcontainerRegistry registry = containerRegistry.get();
        if (registry != null) {
            // Look for the servers to start on fields only
            for (TestcontainerDescription description : registry) {
                if (description.testcontainer.value()) {
                    description.instance.start();
                }
            }
        }
    }

    @SuppressWarnings({ "resource", "BooleanMethodIsAlwaysInverted" })
    private boolean isDockerAvailable() {
        try {
            DockerClientFactory.instance().client();
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    private static Throwable createException(final Class<? extends Throwable> value) {
        // First try the String.class constructor
        try {
            final Constructor<? extends Throwable> constructor = value.getConstructor(String.class);
            return constructor.newInstance("No Docker environment is available.");
        } catch (NoSuchMethodException ignore) {
            try {
                final Constructor<? extends Throwable> constructor = value.getConstructor();
                return constructor.newInstance();
            } catch (NoSuchMethodException unused) {
                throw new AssertionError(String.format("No String or no-arg constructor found for %s", value));
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new AssertionError(String.format("Failed to create exception for type %s", value), e);
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new AssertionError(String.format("Failed to create exception for type %s", value), e);
        }
    }
}
