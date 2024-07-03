/*
 * Copyright The Arquillian Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.arquillian.testcontainers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
final class SecurityActions {

    static List<Field> getFieldsWithAnnotation(final Class<?> source,
            final Class<? extends Annotation> annotationClass) {
        return AccessController.doPrivileged((PrivilegedAction<List<Field>>) () -> {
            final List<Field> foundFields = new ArrayList<>();
            Class<?> nextSource = source;
            while (nextSource != Object.class) {
                for (Field field : nextSource.getDeclaredFields()) {
                    if (field.isAnnotationPresent(annotationClass)) {
                        if (!field.trySetAccessible()) {
                            throw new IllegalStateException(String.format("Could not make field %s accessible", field));
                        }
                        foundFields.add(field);
                    }
                }
                nextSource = nextSource.getSuperclass();
            }
            return List.copyOf(foundFields);
        });
    }
}
