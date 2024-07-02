/*
 * Copyright The Arquillian Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.arquillian.testcontainers;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.container.test.impl.enricher.resource.OperatesOnDeploymentAwareProvider;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.annotation.ClassScoped;
import org.jboss.arquillian.testcontainers.api.TestContainerInstances;
import org.testcontainers.containers.GenericContainer;

class TestContainerProvider extends OperatesOnDeploymentAwareProvider {
    @Inject
    @ClassScoped
    private Instance<TestContainerInstances> containerInstances;

    @Override
    public Object doLookup(ArquillianResource resource, Annotation... qualifiers) {
        TestContainerInstances instances = containerInstances.get();
        if (instances != null) {
            // if there is only 1 container, return the instance
            if (instances.all().size() == 1) {
                return instances.get(0);
            }
            // if there is more than 1 container, search if there is a matching qualifier
            for (GenericContainer<?> container : instances.all()) {
                for (Annotation qualifier : qualifiers) {
                    if (container.getClass().isAnnotationPresent(qualifier.annotationType())) {
                        return container;
                    }
                }
            }
        }
        // if there are more than 1 container and no qualifier matches, return all instances
        return instances;
    }

    @Override
    public boolean canProvide(Class<?> type) {
        return TestContainerInstances.class.isAssignableFrom(type) || GenericContainer.class.isAssignableFrom(type);
    }
}
