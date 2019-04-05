/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

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
