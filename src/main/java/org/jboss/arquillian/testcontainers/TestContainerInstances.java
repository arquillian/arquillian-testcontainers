package org.jboss.arquillian.testcontainers;

import java.util.List;

import org.testcontainers.containers.GenericContainer;

public class TestContainerInstances {
    private List<GenericContainer<?>> containers;

    public TestContainerInstances(List<GenericContainer<?>> containers) {
        this.containers = containers;
    }

    public List<GenericContainer<?>> all() {
        return containers;
    }

    public GenericContainer<?> get(int index) {
        return containers.get(index);
    }

    public void beforeStart() {
        for (GenericContainer<?> container : all()) {
            if (container instanceof TestContainerLifecycle) {
                ((TestContainerLifecycle) container).beforeStart(this);
            }
        }
    }

    public void afterStart() {
        for (GenericContainer<?> container : all()) {
            if (container instanceof TestContainerLifecycle) {
                ((TestContainerLifecycle) container).afterStart(this);
            }
        }
    }

    public void beforeStop() {
        for (GenericContainer<?> container : all()) {
            if (container instanceof TestContainerLifecycle) {
                ((TestContainerLifecycle) container).beforeStop(this);
            }
        }
    }

}
