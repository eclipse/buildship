/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.util.gradle;

import java.io.Serializable;

/**
 * Holds two values.
 *
 * @param <S> the type of the first value
 * @param <T> the type of the second value
 * @author Etienne Studer
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public final class Pair<S, T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final S first;
    private final T second;

    public Pair(S first, T second) {
        this.first = first;
        this.second = second;
    }

    public S getFirst() {
        return this.first;
    }

    public T getSecond() {
        return this.second;
    }

}
