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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.common.base.Preconditions;
import com.gradleware.tooling.eclipse.core.GradlePluginsRuntimeException;
import com.gradleware.tooling.eclipse.core.console.ProcessStreams;

/**
 * Process stream driving output into a target file.
 */
public final class FileOutputProcessStream implements ProcessStreams {

    private final FileOutputStream outputStream;

    public FileOutputProcessStream(File file, boolean append) {
        Preconditions.checkNotNull(file);
        Preconditions.checkState(file.exists());
        try {
            this.outputStream = new FileOutputStream(file, append);
        } catch (Exception e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }

    @Override
    public OutputStream getOutput() {
        return this.outputStream;
    }

    @Override
    public OutputStream getError() {
        return this.outputStream;
    }

    @Override
    public InputStream getInput() {
        throw new UnsupportedOperationException("Stream can only write to the target file");
    }

    @Override
    public void close() {
        try {
            this.outputStream.close();
        } catch (Exception e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }

}
