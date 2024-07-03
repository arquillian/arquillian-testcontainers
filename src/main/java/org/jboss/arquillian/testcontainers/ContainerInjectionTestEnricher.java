/*
 * Copyright The Arquillian Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.arquillian.testcontainers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.testcontainers.api.DockerRequired;
import org.jboss.arquillian.testcontainers.api.Testcontainer;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SuppressWarnings({ "unchecked", "resource" })
public class ContainerInjectionTestEnricher implements TestEnricher {
    @Inject
    private Instance<TestcontainerRegistry> instances;

    @Override
    public void enrich(final Object testCase) {
        if (!isAnnotatedWith(testCase.getClass(), DockerRequired.class)) {
            return;
        }
        final boolean isDockerAvailable = isDockerAvailable();
        for (Field field : getFieldsWithAnnotation(testCase.getClass())) {
            checkForDocker(isDockerAvailable);
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
                        .lookupOrCreate((Class<GenericContainer<?>>) field.getType(), field, testcontainer,
                                qualifiers);
            } catch (Exception e) {
                throw new RuntimeException("Could not lookup value for field " + field, e);
            }
            try {
                if (field.trySetAccessible()) {
                    field.set(testCase, value);
                } else {
                    throw new RuntimeException("Could not set value for field " + field);
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Could not set value on field " + field + " using " + value, e);
            }
        }
    }

    @Override
    public Object[] resolve(final Method method) {
        return new Object[method.getParameterTypes().length];
    }

    private static void checkForDocker(boolean isDockerAvailable) {
        final String detailMessage = "No Docker environment is available.";
        if (!isDockerAvailable) {
            throw new AssertionError(detailMessage);
        }
    }

    @SuppressWarnings({ "resource", "BooleanMethodIsAlwaysInverted" })
    private static boolean isDockerAvailable() {
        try {
            DockerClientFactory.instance().client();
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    private static List<Field> getFieldsWithAnnotation(final Class<?> source) {
        final List<Field> foundFields = new ArrayList<>();
        Class<?> nextSource = source;
        while (nextSource != Object.class) {
            for (Field field : nextSource.getDeclaredFields()) {
                if (field.isAnnotationPresent(Testcontainer.class)) {
                    if (!field.trySetAccessible()) {
                        throw new IllegalStateException(String.format("Could not make field %s accessible", field));
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
