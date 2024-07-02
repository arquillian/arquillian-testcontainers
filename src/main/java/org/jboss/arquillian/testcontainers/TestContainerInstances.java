/*
 * Copyright The Arquillian Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.arquillian.testcontainers;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.List;

import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.testcontainers.containers.GenericContainer;

public class TestContainerInstances implements Iterable<GenericContainer<?>> {
    private List<GenericContainer<?>> containers;

    TestContainerInstances(List<GenericContainer<?>> containers) {
        this.containers = containers;
    }

    /**
     * @return the list of all instance in order of declaration
     */
    public List<GenericContainer<?>> all() {
        return containers;
    }

    /**
     * retrieve a container instance by its index in the declaration
     *
     * @return {@link IndexOutOfBoundsException} if index is wrong
     */
    public GenericContainer<?> get(int index) {
        return containers.get(index);
    }

    /**
     * search a container instance by its qualifier
     *
     * @param qualifier the annotation
     * @return the container instance or null, if none matches
     */
    public GenericContainer<?> get(Class<? extends Annotation> qualifier) {
        for (GenericContainer<?> container : containers) {
            if (container.getClass().isAnnotationPresent(qualifier)) {
                return container;
            }
        }
        return null;
    }

    void beforeStart(ContainerRegistry registry) {
        for (GenericContainer<?> container : all()) {
            if (container instanceof TestContainerLifecycle) {
                ((TestContainerLifecycle) container).beforeStart(this, registry);
            }
        }
    }

    void afterStart(ContainerRegistry registry) {
        for (GenericContainer<?> container : all()) {
            if (container instanceof TestContainerLifecycle) {
                ((TestContainerLifecycle) container).afterStart(this, registry);
            }
        }
    }

    void beforeStop(ContainerRegistry registry) {
        for (GenericContainer<?> container : all()) {
            if (container instanceof TestContainerLifecycle) {
                ((TestContainerLifecycle) container).beforeStop(this, registry);
            }
        }
    }

    /**
     * @return an iterator for the container instances
     */
    @Override
    public Iterator<GenericContainer<?>> iterator() {
        return containers.iterator();
    }
}
