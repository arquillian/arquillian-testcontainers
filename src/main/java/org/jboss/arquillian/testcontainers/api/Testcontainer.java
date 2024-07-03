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
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

import org.testcontainers.lifecycle.Startable;

/**
 * Used to annotate a field or parameter which <strong>must</strong> be an instance of a
 * {@link Startable}. A {@link DockerRequired} annotation must be present on the
 * type to use Testcontainer injection.
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
public @interface Testcontainer {

    /**
     * Indicates whether Arquillian should manage the starting of the Testcontainer. With a value of {@code false},
     * Arquillian will not start the server. It will still attempt to stop the server.
     *
     * @return {@code true} to have Arquillian manage the lifecycle of the Testcontainer
     */
    boolean value() default true;

    /**
     * The type used to create the value for the field or parameter. The type must have a no-arg constructor.
     * <p>
     * If left as the default value, {@link Startable}, the type to construct is derived from the
     * {@linkplain Field#getType() field} or {@linkplain Parameter#getType() parameter}.
     * </p>
     *
     * @return the type to construct
     */
    Class<? extends Startable> type() default Startable.class;
}
