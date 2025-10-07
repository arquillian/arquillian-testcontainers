/*
 * Copyright The Arquillian Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.arquillian.testcontainers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.arquillian.testcontainers.api.Testcontainer;
import org.testcontainers.containers.GenericContainer;

/**
 * A registry to store the testcontainer descriptions.
 */
class TestcontainerRegistry implements Iterable<TestcontainerDescription> {
    private final List<TestcontainerDescription> containers;

    TestcontainerRegistry() {
        containers = new CopyOnWriteArrayList<>();
    }

    /**
     * Lookup the container in the test container instances. If more than one container is found for the type or
     * qualifier, an {@link IllegalArgumentException} is thrown.
     *
     * @param type          the type to lookup
     * @param testcontainer the test container annotation
     * @param qualifiers    any qualifying annotations
     *
     * @return the generic type
     */
    GenericContainer<?> lookupOrCreate(final Class<GenericContainer<?>> type, final Testcontainer testcontainer,
            final List<Annotation> qualifiers) {
        GenericContainer<?> result = lookup(type, qualifiers);
        if (result == null) {
            try {
                final Constructor<? extends GenericContainer<?>> constructor = getConstructor(type, testcontainer);
                result = constructor.newInstance();
                this.containers.add(new TestcontainerDescription(testcontainer, result));
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
    GenericContainer<?> lookup(final Class<?> type, final List<Annotation> qualifiers) {
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

    private static Constructor<? extends GenericContainer<?>> getConstructor(final Class<GenericContainer<?>> type,
            final Testcontainer testcontainer) throws NoSuchMethodException {
        @SuppressWarnings("unchecked")
        Class<? extends GenericContainer<?>> constructType = (testcontainer.type() == GenericContainer.class) ? type
                : (Class<? extends GenericContainer<?>>) testcontainer.type();
        if (constructType.isInterface()) {
            throw new IllegalArgumentException(
                    String.format("Type %s is an interface and cannot be created.", constructType));
        }
        return constructType.getConstructor();
    }
}
