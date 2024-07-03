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
import org.jboss.arquillian.testcontainers.api.DockerRequired;
import org.testcontainers.DockerClientFactory;

@SuppressWarnings("unused")
class TestContainersObserver {
    @Inject
    @ClassScoped
    private InstanceProducer<TestcontainerRegistry> containerRegistry;

    @Inject
    private Instance<ContainerRegistry> registry;

    public void createContainer(@Observes(precedence = 500) BeforeClass beforeClass) throws Throwable {
        final TestClass javaClass = beforeClass.getTestClass();
        final DockerRequired dockerRequired = javaClass.getAnnotation(DockerRequired.class);
        if (dockerRequired != null) {
            if (!isDockerAvailable()) {
                throw createException(dockerRequired.value());
            }
        }
        final TestcontainerRegistry instances = new TestcontainerRegistry();
        containerRegistry.set(instances);
    }

    public void stopContainer(@Observes AfterClass afterClass) {
        TestcontainerRegistry instances = containerRegistry.get();
        if (instances != null) {
            for (TestcontainerDescription container : instances) {
                container.instance.stop();
            }
        }
    }

    public void startContainer(@Observes(precedence = 500) final AfterEnrichment event) {
        // Look for the servers to start on fields only
        for (TestcontainerDescription description : containerRegistry.get()) {
            if (description.testcontainer.value()) {
                description.instance.start();
            }
        }
    }

    private void checkForDocker(boolean failIfNoDocker, boolean isDockerAvailable) {
        final String detailMessage = "No Docker environment is available.";
        if (!isDockerAvailable) {
            if (failIfNoDocker) {
                throw new AssertionError(detailMessage);
            } else {
                // First attempt to throw a JUnit 5 assumption
                throwAssumption("org.opentest4j.TestAbortedException", detailMessage);
                // Not found, attempt to throw a JUnit exception
                throwAssumption("org.junit.AssumptionViolatedException", detailMessage);
                // No supported test platform found. Throw an AssertionError.
                throw new AssertionError(
                        "Failed to find a support test platform and no Docker environment is available.");
            }
        }
    }

    private void throwAssumption(final String type, final String detailMessage) {
        try {
            Class<?> clazz = Class.forName(type);
            Constructor<?> ctor = clazz.getConstructor(String.class);
            throw (RuntimeException) ctor.newInstance(detailMessage);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException ignore) {
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
