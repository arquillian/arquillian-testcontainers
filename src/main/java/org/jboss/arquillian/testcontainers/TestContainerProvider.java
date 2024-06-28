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
import org.testcontainers.containers.GenericContainer;

public class TestContainerProvider extends OperatesOnDeploymentAwareProvider {
    @Inject
    @ClassScoped
    private Instance<GenericContainer<?>> genericContainerInstance;

    @Override
    public Object doLookup(ArquillianResource resource, Annotation... qualifiers) {
        return genericContainerInstance.get();
    }

    @Override
    public boolean canProvide(Class<?> type) {
        return GenericContainer.class.isAssignableFrom(type);
    }
}
