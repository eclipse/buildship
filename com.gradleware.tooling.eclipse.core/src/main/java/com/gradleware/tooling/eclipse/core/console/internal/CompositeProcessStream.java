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

package com.gradleware.tooling.eclipse.core.console.internal;

import java.io.InputStream;
import java.io.OutputStream;

import com.google.common.base.Preconditions;
import com.gradleware.tooling.eclipse.core.console.ProcessStreams;


/**
 * Can compose multiple console streams where one stream is used only for reading input and the other one is writing the output.
 */
public final class CompositeProcessStream implements ProcessStreams {

    private final ProcessStreams inputStram;
    private final ProcessStreams outputStram;
    private final ProcessStreams errorStream;

    public CompositeProcessStream(ProcessStreams inputStream, ProcessStreams outputStream, ProcessStreams errorStream) {
        this.inputStram = Preconditions.checkNotNull(inputStream);
        this.outputStram = Preconditions.checkNotNull(outputStream);
        this.errorStream = Preconditions.checkNotNull(errorStream);
    }

    @Override
    public OutputStream getOutput() {
        return this.outputStram.getOutput();
    }

    @Override
    public OutputStream getError() {
        return this.errorStream.getError();
    }

    @Override
    public InputStream getInput() {
        return this.inputStram.getInput();
    }

    @Override
    public void close() {
        // if the same objects were passed to the constructor, then we don't want to call close multiple times
        this.inputStram.close();
        if (this.inputStram != this.outputStram) {
            this.outputStram.close();
        }
        if (this.inputStram != this.errorStream && this.outputStram != this.errorStream) {
            this.errorStream.close();
        }
    }

}
