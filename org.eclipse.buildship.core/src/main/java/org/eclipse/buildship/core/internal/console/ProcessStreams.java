/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

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
