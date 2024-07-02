/*
 * Copyright The Arquillian Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.arquillian.testcontainers.api;

import java.util.function.Consumer;
import java.util.logging.Logger;

import org.testcontainers.containers.output.OutputFrame;

/**
 * A simple consumer for containers which logs the container lines to a {@linkplain Logger logger}.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class LoggingConsumer implements Consumer<OutputFrame> {

    private final Logger logger;

    /**
     * Creates a new logger with the name of {@link Class#getName()}.
     *
     * @param type the type to extract the name from
     */
    public LoggingConsumer(final Class<?> type) {
        this(type.getName());
    }

    /**
     * Creates a new logger with the name passed in.
     *
     * @param name the name for the logger
     */
    public LoggingConsumer(final String name) {
        this.logger = Logger.getLogger(name);
    }

    /**
     * Creates a new logger with the name of {@link Class#getName()}.
     *
     * @param type the type to extract the name from
     */
    public static LoggingConsumer of(final Class<?> type) {
        return new LoggingConsumer(type);
    }

    /**
     * Creates a new logger with the name passed in.
     *
     * @param name the name for the logger
     */
    public static LoggingConsumer of(final String name) {
        return new LoggingConsumer(name);
    }

    @Override
    public void accept(final OutputFrame outputFrame) {
        final OutputFrame.OutputType outputType = outputFrame.getType();
        final String utf8String = outputFrame.getUtf8StringWithoutLineEnding();
        switch (outputType) {
            case END:
                break;
            case STDOUT:
                logger.info(utf8String);
                break;
            case STDERR:
                logger.severe(utf8String);
                break;
            default:
                throw new IllegalArgumentException("Unexpected outputType " + outputType);
        }
    }
}
