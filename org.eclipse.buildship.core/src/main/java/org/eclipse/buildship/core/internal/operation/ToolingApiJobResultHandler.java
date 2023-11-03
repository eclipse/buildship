/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.operation;

/**
 * Callback to execute when a {@link ToolingApiJob} finishes.
 *
 * @author Donat Csikos
 *
 * @param <T> the result type
 */
public interface ToolingApiJobResultHandler<T> {

    void onSuccess(T result);

    void onFailure(ToolingApiStatus status);
}
