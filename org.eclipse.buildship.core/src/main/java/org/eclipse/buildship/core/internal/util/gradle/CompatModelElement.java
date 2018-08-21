/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */


package org.eclipse.buildship.core.internal.util.gradle;

import com.google.common.base.Preconditions;

/**
 * Abstract superclass for all compatibility classes that hold a reference to the raw Tooling API model element.
 *
 * @author Donat Csikos
 *
 * @param <T> The TAPI model type.
 */
class CompatModelElement<T> {

    private final T element;

    CompatModelElement(T element) {
        this.element = Preconditions.checkNotNull(element);
    }

    public T getElement() {
        return this.element;
    }
}
