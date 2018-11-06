/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal;

/**
 * Available tracing scopes for the core plug-in.
 */
public enum CoreTraceScopes implements TraceScope {
    PREFERENCES("preferences"),
    PROJECT_CONFIGURATORS("projectConfigurators");

    private final String scopeKey;

    CoreTraceScopes(String scopeKey) {
        this.scopeKey = scopeKey;
    }

    @Override
    public String getScopeKey() {
        return this.scopeKey;
    }
}
