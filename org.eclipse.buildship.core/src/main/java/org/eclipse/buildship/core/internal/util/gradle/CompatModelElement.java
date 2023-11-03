/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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
