/*
 * Copyright The Arquillian Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.arquillian.testcontainers;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.testcontainers.api.TestContainerInstances;
import org.testcontainers.containers.GenericContainer;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class TestContainerLookup {

    /**
     * Lookup the container in the test container instances. If more than one container is found for the type or
     * qualifier, an {@link IllegalArgumentException} is thrown.
     *
     * @param type       the type to lookup
     * @param qualifiers any qualifying annotations
     * @param instances  the test instances
     *
     * @return the generic type
     */
    static GenericContainer<?> lookup(final Class<?> type, final List<Annotation> qualifiers,
            final TestContainerInstances instances) {
        final List<GenericContainer<?>> foundContainers = new ArrayList<>();
        if (qualifiers.isEmpty()) {
            for (GenericContainer<?> container : instances) {
                if (type.isAssignableFrom(container.getClass())) {
                    foundContainers.add(container);
                }
            }
        } else {
            for (GenericContainer<?> container : instances) {
                for (Annotation qualifier : qualifiers) {
                    if (type.isAssignableFrom(container.getClass()) && type.isAnnotationPresent(qualifier.annotationType())) {
                        foundContainers.add(container);
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
        return foundContainers.get(0);
    }
}
