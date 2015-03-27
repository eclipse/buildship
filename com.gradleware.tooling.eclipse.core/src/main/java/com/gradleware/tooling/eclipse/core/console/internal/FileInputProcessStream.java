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
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.common.base.Preconditions;
import com.gradleware.tooling.eclipse.core.GradlePluginsRuntimeException;
import com.gradleware.tooling.eclipse.core.console.ProcessStreams;

/**
 * Process stream taking input from a file.
 */
public final class FileInputProcessStream implements ProcessStreams {

    private final FileInputStream inputStream;

    public FileInputProcessStream(File file) {
        try {
            Preconditions.checkState(file.exists() || file.createNewFile());
            Preconditions.checkNotNull(file);
            this.inputStream = new FileInputStream(file);
        } catch (Exception e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }

    @Override
    public OutputStream getOutput() {
        throw new UnsupportedOperationException("Stream can only read fom the target file");
    }

    @Override
    public OutputStream getError() {
        throw new UnsupportedOperationException("Stream can only read fom the target file");
    }

    @Override
    public InputStream getInput() {
        return this.inputStream;
    }

    @Override
    public void close() {
        try {
            this.inputStream.close();
        } catch (Exception e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }

}
