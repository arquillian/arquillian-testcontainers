/*
 * Copyright The Arquillian Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.arquillian.testcontainers;

import org.arquillian.testcontainers.api.Testcontainer;
import org.arquillian.testcontainers.api.TestcontainersRequired;
import org.arquillian.testcontainers.common.SimpleTestContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opentest4j.TestAbortedException;
import org.testcontainers.containers.GenericContainer;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
@TestcontainersRequired(TestAbortedException.class)
public class SameInstanceTest {

    @Testcontainer
    private static SimpleTestContainer globalContainer;

    @Testcontainer
    private SimpleTestContainer instanceContainer;

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @BeforeAll
    @AfterAll
    public static void checkAvailable() {
        // check(globalContainer);
    }

    @BeforeEach
    @AfterEach
    public void checkInstanceAvailable() {
        check(instanceContainer);
    }

    @Test
    public void checkSame() {
        check(instanceContainer);
        check(globalContainer);
        Assertions.assertEquals(instanceContainer, globalContainer);
    }

    private static void check(final GenericContainer<?> container) {
        Assertions.assertNotNull(container, "Expected the container to be injected.");
        Assertions.assertTrue(container.isRunning(), "Expected the container to be running");
    }
}
