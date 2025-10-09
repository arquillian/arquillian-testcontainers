/*
 * Copyright The Arquillian Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.arquillian.testcontainers;

import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestEnricher;

class TestContainersExtension implements LoadableExtension {
    @Override
    public void register(ExtensionBuilder builder) {
        builder
                .observer(TestContainersObserver.class)
                .service(TestEnricher.class, ContainerInjectionTestEnricher.class);
    }
}
