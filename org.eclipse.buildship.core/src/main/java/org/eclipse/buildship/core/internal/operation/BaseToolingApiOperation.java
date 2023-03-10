/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.operation;

import com.google.common.base.Preconditions;

/**
 * Convenience {@link ToolingApiOperation} subclass implementing the {@code #getName} method.
 *
 * @author Donat Csikos
 */
public abstract class BaseToolingApiOperation implements ToolingApiOperation {

    private String name;

    public BaseToolingApiOperation(String name) {
        this.name = Preconditions.checkNotNull(name);
    }

    @Override
    public String getName() {
        return this.name;
    }
}
