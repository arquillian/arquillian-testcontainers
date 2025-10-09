/*
 * Copyright The Arquillian Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.arquillian.testcontainers.common;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.shrinkwrap.api.Archive;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class TestDeployableContainer implements DeployableContainer<TestDeployableContainer.EmptyContainerConfiguration> {
    @Override
    public Class<EmptyContainerConfiguration> getConfigurationClass() {
        return EmptyContainerConfiguration.class;
    }

    @Override
    public ProtocolDescription getDefaultProtocol() {
        return new ProtocolDescription("TestDeployableContainer");
    }

    @Override
    public ProtocolMetaData deploy(final Archive<?> archive) {
        return new ProtocolMetaData();
    }

    @Override
    public void undeploy(final Archive<?> archive) {
    }

    public static class EmptyContainerConfiguration implements ContainerConfiguration {

        @Override
        public void validate() throws ConfigurationException {

        }
    }
}
