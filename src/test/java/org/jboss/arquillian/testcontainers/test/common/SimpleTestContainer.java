/*
 * Copyright The Arquillian Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.arquillian.testcontainers.test.common;

import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class SimpleTestContainer extends MockServerContainer {

    public SimpleTestContainer() {
        super(DockerImageName
                .parse("mockserver/mockserver")
                .withTag("latest"));
    }
}
