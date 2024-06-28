/*
 * JBoss, Home of Professional Open Source
 * Copyright 2024 Red Hat Inc. and/or its affiliates and other contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.testcontainers;

import java.lang.reflect.Constructor;

import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.annotation.ClassScoped;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;

import com.github.dockerjava.api.DockerClient;

public class TestContainersObserver {
    @Inject
    @ClassScoped
    protected InstanceProducer<GenericContainer<?>> containerWrapper;

    public void createContainer(@Observes(precedence = 500) BeforeClass beforeClass) {
        TestClass javaClass = beforeClass.getTestClass();
        TestContainer tcAnno = javaClass.getAnnotation(TestContainer.class);
        if (tcAnno != null) {
            checkForDocker(tcAnno.failIfNoDocker());

            Class<? extends GenericContainer<?>> clazz = tcAnno.value();
            try {
                final GenericContainer<?> container = clazz.getConstructor().newInstance();
                container.start();
                containerWrapper.set(container);
            } catch (Exception e) { // Clean up
                throw new RuntimeException(e);
            }
        }
    }

    public void stopContainer(@Observes AfterClass afterClass) {
        GenericContainer<?> container = containerWrapper.get();
        if (container != null) {
            container.stop();
        }
    }

    private void checkForDocker(boolean failIfNoDocker) {
        final String detailMessage = "No Docker environment is available.";
        if (!isDockerAvailable() && failIfNoDocker) {
            throw new AssertionError(detailMessage);
        } else {
            try {
                Class<?> clazz = Class.forName("org.junit.AssumptionViolatedException");
                Constructor<?> ctor = clazz.getDeclaredConstructor(String.class);
                throw (RuntimeException) ctor.newInstance(detailMessage);
            } catch (Exception e) {
                // TestNG?
            }
        }
    }

    private boolean isDockerAvailable() {
        try (DockerClient client = DockerClientFactory.instance().client()) {
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }
}
