/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.util.gradle;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.ProgressListener;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Container to hold those attributes of a {@link Request} that do change between request invocations.
 *
 * @author Etienne Studer
 */
public final class TransientRequestAttributes {

    private final boolean colorOutput;
    private final OutputStream standardOutput;
    private final OutputStream standardError;
    private final InputStream standardInput;
    private final ImmutableList<ProgressListener> progressListeners;
    private final ImmutableList<org.gradle.tooling.events.ProgressListener> typedProgressListeners;
    private final CancellationToken cancellationToken;

    public TransientRequestAttributes(boolean colorOutput, OutputStream standardOutput, OutputStream standardError, InputStream standardInput, List<ProgressListener> progressListeners,
                                      List<org.gradle.tooling.events.ProgressListener> typedProgressListeners, CancellationToken cancellationToken) {
        this.colorOutput = colorOutput;
        this.standardOutput = standardOutput;
        this.standardError = standardError;
        this.standardInput = standardInput;
        this.progressListeners = ImmutableList.copyOf(progressListeners);
        this.typedProgressListeners = ImmutableList.copyOf(typedProgressListeners);
        this.cancellationToken = Preconditions.checkNotNull(cancellationToken);
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean isColorOutput() {
        return this.colorOutput;
    }

    @SuppressWarnings("UnusedDeclaration")
    public OutputStream getStandardOutput() {
        return this.standardOutput;
    }

    @SuppressWarnings("UnusedDeclaration")
    public OutputStream getStandardError() {
        return this.standardError;
    }

    @SuppressWarnings("UnusedDeclaration")
    public InputStream getStandardInput() {
        return this.standardInput;
    }

    @SuppressWarnings("UnusedDeclaration")
    public List<ProgressListener> getProgressListeners() {
        return this.progressListeners;
    }

    @SuppressWarnings("UnusedDeclaration")
    public List<org.gradle.tooling.events.ProgressListener> getTypedProgressListeners() {
        return this.typedProgressListeners;
    }

    @SuppressWarnings("UnusedDeclaration")
    public CancellationToken getCancellationToken() {
        return this.cancellationToken;
    }
}
