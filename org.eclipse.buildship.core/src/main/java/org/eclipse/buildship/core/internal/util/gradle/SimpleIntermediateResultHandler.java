/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.util.gradle;

import org.gradle.tooling.IntermediateResultHandler;

public final class SimpleIntermediateResultHandler<T> implements IntermediateResultHandler<T> {

    private T result;

    @Override
    public void onComplete(T result) {
        this.result = result;
    }

    public T getValue() {
        return this.result;
    }
}
