/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.gradle.internal;

import com.gradleware.tooling.toolingclient.ToolingClient;

import org.eclipse.buildship.core.gradle.ToolingClientProvider;

/**
 * Profoundly basic implementation of the {@link ToolingClientProvider} interface.
 */
public final class DefaultToolingClientProvider implements ToolingClientProvider {

    @Override
    public ToolingClient newClient() {
        return ToolingClient.newClient();
    }

}
