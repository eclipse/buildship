/*
 * Copyright (c) 2019 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.invocation;

import java.util.Arrays;
import java.util.List;

import org.eclipse.buildship.core.invocation.InvocationCustomizer;

/**
 * Adds an extra property to all builds so that build scripts can determine if the invocation
 * came from the command line or from Eclipse.
 */
public final class BuiltInInvocationCustomizer implements InvocationCustomizer {

    private static final List<String> BUILTIN_ARGUMENTS = Arrays.asList("-Porg.eclipse.buildship.present");

    @Override
    public List<String> getExtraArguments() {
        return BUILTIN_ARGUMENTS;
    }
}
