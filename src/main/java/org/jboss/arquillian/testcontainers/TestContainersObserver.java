/*
 * Copyright The Arquillian Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.arquillian.testcontainers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.annotation.ClassScoped;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;

@SuppressWarnings("unused")
public class TestContainersObserver {
    @Inject
    @ClassScoped
    protected InstanceProducer<TestContainerInstances> containersWrapper;

    public void createContainer(@Observes(precedence = 500) BeforeClass beforeClass) {
        TestClass javaClass = beforeClass.getTestClass();
        TestContainers tcAnnos = javaClass.getAnnotation(TestContainers.class);
        TestContainer tcAnno = javaClass.getAnnotation(TestContainer.class);
        if (tcAnno != null || tcAnnos != null) {
            TestContainer[] testContainers = (tcAnnos != null) ? tcAnnos.value() : new TestContainer[] { tcAnno };
            List<GenericContainer<?>> containers = new ArrayList<>();
            for (TestContainer testContainer : testContainers) {
                checkForDocker(testContainer.failIfNoDocker());

                Class<? extends GenericContainer<?>> clazz = testContainer.value();
                try {
                    final GenericContainer<?> container = clazz.getConstructor().newInstance();
                    containers.add(container);
                } catch (Exception e) { // Clean up
                    throw new RuntimeException(e);
                }
            }
            TestContainerInstances instances = new TestContainerInstances(containers);
            containersWrapper.set(instances);
            instances.beforeStart();
            for (GenericContainer<?> container : instances.all()) {
                container.start();
            }
            instances.afterStart();
        }
    }

    public void stopContainer(@Observes AfterClass afterClass) {
        TestContainerInstances instances = containersWrapper.get();
        if (instances != null) {
            instances.beforeStop();
            for (GenericContainer<?> container : instances.all()) {
                container.stop();
            }
        }
    }

    private void checkForDocker(boolean failIfNoDocker) {
        final String detailMessage = "No Docker environment is available.";
        if (!isDockerAvailable()) {
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
}
