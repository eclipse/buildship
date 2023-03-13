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

import java.util.Optional;

import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.ResultHandler;

public class InspectableResultHandler<T> implements ResultHandler<T> {

    private T result;
    private GradleConnectionException failure;

    @Override
    public void onComplete(T result) {
        this.result = result;
    }

    @Override
    public void onFailure(GradleConnectionException failure) {
        this.failure = failure;
    }

    public Optional<T> getResult() {
        return Optional.ofNullable(this.result);
    }

    public void forwardResults(ResultHandler<? super T> handler) {
        if (this.failure == null) {
            handler.onComplete(this.result);
        } else {
            handler.onFailure(this.failure);
        }
    }
}
