/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal;

/**
 * Available tracing scopes for the core plug-in.
 */
public enum CoreTraceScopes implements TraceScope {
    CLASSPATH("classpath"),
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
