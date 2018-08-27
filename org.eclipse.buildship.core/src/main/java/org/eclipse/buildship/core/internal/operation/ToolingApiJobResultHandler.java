/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

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
