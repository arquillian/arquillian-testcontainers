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

import org.testcontainers.containers.GenericContainer;

/**
 * Used to annotate a field which <strong>must</strong> be an instance of a {@link GenericContainer}. A
 * {@link ContainerRequired} annotation must be present on the type to use Testcontainer injection.
 *
 * <pre>
 * &#064;ExtendWith(ArquillianExtension.class)
 * &#064;RunAsClient
 * // By throwing the TestAbortedException, the test will be skipped if a container engine is not available
 * &#064;DockerRequired(TestAbortedException.class)
 * public class ContainerTest {
 *
 *     &#064;Testcontainer
 *     private CustomTestContainer container;
 *
 *     &#064;Deployment
 *     public static JavaArchive createDeployment() {
 *         return ShrinkWrap.create(JavaArchive.class)
 *                 .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
 *     }
 *
 *     &#064;Test
 *     public void testContainerInjected() {
 *         Assertions.assertNotNull(container, "Expected the container to be injected.");
 *         Assertions.assertTrue(container.isRunning(), "Expected the container to be running");
 *     }
 * }
 * </pre>
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Testcontainer {

    /**
     * Indicates whether Arquillian should manage the starting of the Testcontainer. With a value of {@code false},
     * Arquillian will not start the server. It will still attempt to stop the server.
     *
     * @return {@code true} to have Arquillian manage the lifecycle of the Testcontainer
     */
    boolean value() default true;

    /**
     * The type used to create the value for the field. The type must have a no-arg constructor.
     * <p>
     * If left as the default value, {@link GenericContainer}, the type to construct is derived from the
     * {@linkplain Field#getType() field}.
     * </p>
     *
     * @return the type to construct
     */
    Class<? extends GenericContainer> type() default GenericContainer.class;
}
