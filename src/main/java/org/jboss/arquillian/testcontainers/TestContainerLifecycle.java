package org.jboss.arquillian.testcontainers;

public interface TestContainerLifecycle {

    default void beforeStart(TestContainerInstances testContainerInstances) {
    }

    default void afterStart(TestContainerInstances testContainerInstances) {
    }

    default void beforeStop(TestContainerInstances testContainerInstances) {
    }

}
