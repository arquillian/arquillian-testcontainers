/*
 * Copyright The Arquillian Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.arquillian.testcontainers;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jboss.arquillian.testcontainers.api.Testcontainer;
import org.testcontainers.lifecycle.Startable;

class TestcontainerRegistry implements Iterable<TestcontainerDescription> {
    private final List<TestcontainerDescription> containers;

    TestcontainerRegistry() {
        containers = new CopyOnWriteArrayList<>();
    }

    /**
     * Lookup the container in the test container instances. If more than one container is found for the type or
     * qualifier, an {@link IllegalArgumentException} is thrown.
     *
     * @param type       the type to lookup
     * @param qualifiers any qualifying annotations
     *
     * @return the generic type
     */
    Startable lookupOrCreate(final Class<Startable> type, final AnnotatedElement element, final Testcontainer testcontainer,
            final List<Annotation> qualifiers) {
        Startable result = lookup(type, qualifiers);
        if (result == null) {
            try {
                Class<? extends Startable> constructType = (testcontainer.type() == Startable.class) ? type
                        : testcontainer.type();
                if (constructType.isInterface()) {
                    throw new IllegalArgumentException(
                            String.format("Type %s is an interface and cannot be created.", constructType));
                }
                final Constructor<? extends Startable> constructor = constructType.getConstructor();
                result = constructor.newInstance();
                this.containers.add(new TestcontainerDescription(testcontainer, element, result));
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalArgumentException(String.format("Could create container %s", type.getName()), e);
            }
        }
        return result;
    }

    /**
     * Lookup the container in the test container instances. If more than one container is found for the type or
     * qualifier, an {@link IllegalArgumentException} is thrown.
     *
     * @param type       the type to lookup
     * @param qualifiers any qualifying annotations
     *
     * @return the generic type
     */
    Startable lookup(final Class<?> type, final List<Annotation> qualifiers) {
        final List<TestcontainerDescription> foundContainers = new ArrayList<>();
        if (qualifiers.isEmpty()) {
            for (TestcontainerDescription containerDesc : this.containers) {
                if (type.isAssignableFrom(containerDesc.instance.getClass())) {
                    foundContainers.add(containerDesc);
                }
            }
        } else {
            for (TestcontainerDescription containerDesc : this.containers) {
                for (Annotation qualifier : qualifiers) {
                    if (type.isAssignableFrom(containerDesc.instance.getClass())
                            && type.isAnnotationPresent(qualifier.annotationType())) {
                        foundContainers.add(containerDesc);
                    }
                }
            }
        }
        if (foundContainers.isEmpty()) {
            return null;
        }
        if (foundContainers.size() > 1) {
            throw new IllegalArgumentException(
                    String.format("Multiple containers found for type %s: %s", type, foundContainers));
        }
        return foundContainers.get(0).instance;
    }

    /**
     * @return an iterator for the container instances
     */
    @Override
    public Iterator<TestcontainerDescription> iterator() {
        return containers.iterator();
    }
}
