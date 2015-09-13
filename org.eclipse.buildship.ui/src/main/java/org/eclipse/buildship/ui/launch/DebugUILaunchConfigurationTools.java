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

package org.eclipse.buildship.ui.launch;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;

import org.eclipse.buildship.core.launch.LaunchConfigurationTools;

/**
 * Default implementation of {@link LaunchConfigurationTools} that is backed by the {@code DebugUITools} class.
 */
public final class DebugUILaunchConfigurationTools implements LaunchConfigurationTools {

    @Override
    public void launch(ILaunchConfiguration configuration, String mode) {
        DebugUITools.launch(configuration, mode);
    }

}
