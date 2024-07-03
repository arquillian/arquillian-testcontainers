/*
 * Copyright The Arquillian Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.arquillian.testcontainers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.testcontainers.api.TestContainerInstances;
import org.jboss.arquillian.testcontainers.api.TestContainerResource;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class ContainerInjectionTestEnricher implements TestEnricher {
    @Inject
    private Instance<TestContainerInstances> instances;

    @Override
    public void enrich(final Object testCase) {
        for (Field field : SecurityActions.getFieldsWithAnnotation(testCase.getClass(), TestContainerResource.class)) {
            Object value;
            try {
                final List<Annotation> qualifiers = Stream.of(field.getAnnotations())
                        .filter(a -> !(a instanceof TestContainerResource))
                        .collect(Collectors.toList());

                value = TestContainerLookup.lookup(field.getType(), qualifiers, instances.get());
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
        final Object[] values = new Object[method.getParameterTypes().length];
        final Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];
            if (parameter.isAnnotationPresent(TestContainerResource.class)) {
                final List<Annotation> qualifiers = Stream.of(parameter.getAnnotations())
                        .filter(a -> !(a instanceof TestContainerResource))
                        .collect(Collectors.toList());
                values[i] = TestContainerLookup.lookup(parameter.getType(), qualifiers, instances.get());
            }
        }
        return values;
    }
}
