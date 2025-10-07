/*
 * Copyright The Arquillian Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.arquillian.testcontainers.common;

import org.arquillian.testcontainers.api.LoggingConsumer;
import org.arquillian.testcontainers.TypeSpecifiedInjectionTest;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class WildFlyContainer extends GenericContainer<WildFlyContainer> {
    public WildFlyContainer() {
        super(DockerImageName.parse("quay.io/wildfly/wildfly:32.0.1.Final-jdk11"));
    }

    @SuppressWarnings("resource")
    @Override
    protected void configure() {
        withLogConsumer(LoggingConsumer.of(TypeSpecifiedInjectionTest.class));
        waitingFor(Wait.forListeningPorts(8080));
        waitingFor(Wait.forLogMessage(".*WFLYSRV0025.*", 1));
    }
}
