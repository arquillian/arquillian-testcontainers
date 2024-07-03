/*
 * Copyright The Arquillian Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.arquillian.testcontainers.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to annotate an instance of a {@link org.testcontainers.containers.GenericContainer} created
 * on a {@link TestContainer} annotation into an Arquillian field or method parameter.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
public @interface TestContainerResource {

    /**
     * Whether the Arquillian should be responsible for starting and stopping the test container.
     *
     * @return {@code true} if the Arquillian should control the lifecycle of the test container
     */
    boolean value() default true;
}
