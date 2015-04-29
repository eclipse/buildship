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

package org.eclipse.buildship.core.console.internal;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.buildship.core.console.ProcessDescription;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.console.ProcessStreamsProvider;

/**
 * Default implementation of {@link org.eclipse.buildship.core.console.ProcessStreamsProvider} that provides {@link System#out},
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

}
