/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
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
 * Default implementation of {@link org.eclipse.buildship.core.internal.console.ProcessStreamsProvider} that provides {@link System#out},
 * {@link System#err}, and {@link System#in}.
 * <p>
 * This implementation is useful in non-UI test scenarios.
 */
public final class StdProcessStreamsProvider implements ProcessStreamsProvider {

    private final ProcessStreams stdStreams = new ProcessStreams() {

        @Override
        public OutputStream getConfiguration() {
            return System.err;
        }

        @Override
        public OutputStream getOutput() {
            return System.out;
        }

        @Override
        public OutputStream getError() {
            return System.err;
        }

        @Override
        public InputStream getInput() {
            return System.in;
        }

        @Override
        public void close() {
            // do nothing since we never want to close the std streams
        }

    };

    @Override
    public ProcessStreams getBackgroundJobProcessStreams() {
        return this.stdStreams;
    }

    @Override
    public ProcessStreams createProcessStreams(ProcessDescription processDescription) {
        return this.stdStreams;
    }

    @Override
    public ProcessStreams getOrCreateProcessStreams(ProcessDescription processDescription) {
        return this.stdStreams;
    }
}
