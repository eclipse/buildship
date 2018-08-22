/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal;

import org.eclipse.buildship.core.GradleWorkspace;
import org.eclipse.buildship.core.configuration.ConfigurationManager;

/**
 * Default implementation for {@link GradleWorkspace}.
 *
 * @author Donat Csikos
 */
public final class DefaultGradleWorkspace implements GradleWorkspace {

    @Override
    public ConfigurationManager getConfigurationManager() {
        return CorePlugin.newConfigurationManager();
    }
}
