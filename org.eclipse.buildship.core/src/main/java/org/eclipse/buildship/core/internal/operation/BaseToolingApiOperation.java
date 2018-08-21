/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

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
