/*
 * Copyright The Arquillian Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.arquillian.testcontainers.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.testcontainers.api.TestContainer;
import org.jboss.arquillian.testcontainers.api.TestContainerResource;
import org.jboss.arquillian.testcontainers.test.common.SimpleTestContainer;
import org.jboss.arquillian.testcontainers.test.common.WildFly;
import org.jboss.arquillian.testcontainers.test.common.WildFlyContainer;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ExtendWith(ArquillianExtension.class)
@TestContainer(value = { SimpleTestContainer.class, WildFlyContainer.class }, failIfNoDocker = false)
@RunAsClient
public class QualifiedInjectionTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @TestContainerResource
    @WildFly
    private WildFlyContainer wildfly;

    @TestContainerResource
    private SimpleTestContainer container;

    @Test
    public void checkWildFly() {
        Assertions.assertNotNull(wildfly);
        Assertions.assertTrue(wildfly.isRunning());
    }

    @Test
    public void checkSimpleTestContainer() {
        Assertions.assertNotNull(container, "Expected the container to be injected.");
        Assertions.assertTrue(container.isRunning(), "Expected the container to be running");
    }
}
