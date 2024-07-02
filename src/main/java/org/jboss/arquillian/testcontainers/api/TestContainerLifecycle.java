/*
 * Copyright The Arquillian Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.arquillian.testcontainers.api;

import org.jboss.arquillian.container.spi.ContainerRegistry;

public interface TestContainerLifecycle {

    default void beforeStart(TestContainerInstances testContainerInstances, ContainerRegistry registry) {
    }

    default void afterStart(TestContainerInstances testContainerInstances, ContainerRegistry registry) {
    }

    default void beforeStop(TestContainerInstances testContainerInstances, ContainerRegistry registry) {
    }

}
