/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core;

import org.eclipse.buildship.core.configuration.ConfigurationManager;

/**
 * Manages the Gradle builds contained in the current Eclipse workspace.
 *
 * @author Donat Csikos
 * @since 3.0
 */
public interface GradleWorkspace {

    /**
     * Returns a reference to the configuration manager service.
     *
     * @return the reference
     */
    ConfigurationManager getConfigurationManager();
}
