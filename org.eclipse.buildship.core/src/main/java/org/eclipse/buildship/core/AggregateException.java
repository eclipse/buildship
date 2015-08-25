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

package org.eclipse.buildship.core;

import java.io.PrintWriter;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Describes one or more unchecked exceptions rallied in one task.
 */
public final class AggregateException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final List<Exception> exceptions;

    public AggregateException(List<? extends Exception> exceptions) {
        Preconditions.checkNotNull(exceptions);
        Preconditions.checkState(!exceptions.isEmpty());
        this.exceptions = ImmutableList.copyOf(exceptions);
    }

    public List<Exception> getExceptions() {
        return this.exceptions;
    }

    @Override
    public void printStackTrace(PrintWriter printWriter) {
        for (Exception exception : this.exceptions) {
            exception.printStackTrace(printWriter);
        }
    }

}
