/*
 * Copyright The Arquillian Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.arquillian.testcontainers;

import java.lang.reflect.AnnotatedElement;

import org.jboss.arquillian.testcontainers.api.Testcontainer;
import org.testcontainers.containers.GenericContainer;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class TestcontainerDescription {

    final Testcontainer testcontainer;
    final GenericContainer<?> instance;
    final AnnotatedElement element;

    TestcontainerDescription(final Testcontainer testcontainer, final AnnotatedElement element,
            final GenericContainer<?> instance) {
        this.testcontainer = testcontainer;
        this.element = element;
        this.instance = instance;
    }
}
