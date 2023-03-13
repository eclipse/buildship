/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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
