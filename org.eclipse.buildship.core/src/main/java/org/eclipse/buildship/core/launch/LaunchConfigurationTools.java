/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 *     Simon Scholz (vogella GmbH) - Bug 465723
 */

package org.eclipse.buildship.core.launch;

import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Launches an {@link ILaunchConfiguration} via DebugUITools class.
 */
public interface LaunchConfigurationTools {

    /**
     * Launches the target configuration.
     *
     * @param configuration the configuration
     * @param mode the target mode
     */
    void launch(final ILaunchConfiguration configuration, final String mode);
}
