/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.console;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Contains the typical input/output streams that are part of invoking an external process.
 */
public interface ProcessStreams {

    /**
     * Returns a stream dedicated to displaying configuration information.
     *
     * @return the configuration output stream
     */
    OutputStream getConfiguration();

    /**
     * Returns the default output stream.
     *
     * @return the default output stream
     */
    OutputStream getOutput();

    /**
     * Returns the error stream.
     *
     * @return the error stream
     */
    OutputStream getError();

    /**
     * Returns the input stream.
     *
     * @return the input stream
     */
    InputStream getInput();

    /**
     * Closes all the contained streams.
     */
    void close();

}
