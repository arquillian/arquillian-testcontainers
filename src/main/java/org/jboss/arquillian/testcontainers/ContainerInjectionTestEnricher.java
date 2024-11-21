/*
 * Copyright The Arquillian Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.arquillian.testcontainers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.testcontainers.api.ContainerRequired;
import org.jboss.arquillian.testcontainers.api.Testcontainer;
import org.testcontainers.containers.GenericContainer;

/**
 * A test enricher for injecting a {@link GenericContainer} into fields annotated with {@link Testcontainer @Testcontainer}.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SuppressWarnings({ "unchecked" })
public class ContainerInjectionTestEnricher implements TestEnricher {
    @Inject
    private Instance<TestcontainerRegistry> instances;

    @Override
    public void enrich(final Object testCase) {
        if (!isAnnotatedWith(testCase.getClass(), ContainerRequired.class)) {
            return;
        }
        for (Field field : getFieldsWithAnnotation(testCase.getClass())) {
            Object value;
            try {
                final List<Annotation> qualifiers = Stream.of(field.getAnnotations())
                        .filter(a -> !(a instanceof Testcontainer))
                        .collect(Collectors.toList());
                final Testcontainer testcontainer = field.getAnnotation(Testcontainer.class);

                // If the field is the default GenericContainer, validate the field is a GenericContainer
                if (testcontainer.type() == GenericContainer.class) {
                    if (!(GenericContainer.class.isAssignableFrom(field.getType()))) {
                        throw new IllegalArgumentException(
                                String.format("Field %s is not assignable to %s", field, testcontainer.type()
                                        .getName()));
                    }
                } else {
                    // An explicit type was defined, make sure we can assign the type to the field
                    if (!(field.getType().isAssignableFrom(testcontainer.type()))) {
                        throw new IllegalArgumentException(
                                String.format("Field %s is not assignable to %s", field, testcontainer.type()
                                        .getName()));
                    }
                }

                value = instances.get()
                        .lookupOrCreate((Class<GenericContainer<?>>) field.getType(), testcontainer, qualifiers);
            } catch (Exception e) {
                throw new RuntimeException("Could not lookup value for field " + field, e);
            }
            try {
                // Field marked as accessible during lookup to fail early if it cannot be made accessible. See the
                // getFieldsWithAnnotation() method.
                field.set(testCase, value);
            } catch (Exception e) {
                throw new RuntimeException("Could not set value on field " + field + " using " + value, e);
            }
        }
    }

    @Override
    public Object[] resolve(final Method method) {
        return new Object[method.getParameterTypes().length];
    }

    private static List<Field> getFieldsWithAnnotation(final Class<?> source) {
        final List<Field> foundFields = new ArrayList<>();
        Class<?> nextSource = source;
        while (nextSource != Object.class) {
            for (Field field : nextSource.getDeclaredFields()) {
                if (field.isAnnotationPresent(Testcontainer.class)) {
                    if (!field.trySetAccessible()) {
                        throw new InaccessibleObjectException(String.format("Could not make field %s accessible", field));
                    }
                    foundFields.add(field);
                }
            }
            nextSource = nextSource.getSuperclass();
        }
        return List.copyOf(foundFields);
    }

    private static boolean isAnnotatedWith(final Class<?> clazz, final Class<? extends Annotation> annotation) {
        if (clazz == null) {
            return false;
        }
        if (clazz.isAnnotationPresent(annotation)) {
            return true;
        }
        for (Class<?> intf : clazz.getInterfaces()) {
            if (isAnnotatedWith(intf, annotation)) {
                return true;
            }
        }
        return isAnnotatedWith(clazz.getSuperclass(), annotation);
    }
}
