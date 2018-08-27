/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.internal;

/**
 * Exception type marking root import failure.
 */
public class ImportRootProjectException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ImportRootProjectException(Exception cause) {
        super(cause);
    }
}