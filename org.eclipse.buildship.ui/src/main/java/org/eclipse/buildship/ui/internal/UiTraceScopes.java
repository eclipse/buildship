/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal;

import org.eclipse.buildship.core.internal.TraceScope;

/**
 * Available tracing scopes for the UI plug-in.
 */
public enum UiTraceScopes implements TraceScope {
    NAVIGATOR("navigator");

    private final String scopeKey;

    UiTraceScopes(String scopeKey) {
        this.scopeKey = scopeKey;
    }

    @Override
    public String getScopeKey() {
        return this.scopeKey;
    }
}
