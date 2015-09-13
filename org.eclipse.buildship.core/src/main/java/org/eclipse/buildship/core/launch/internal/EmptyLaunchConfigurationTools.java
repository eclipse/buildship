/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - refactoring and integration
 */

package org.eclipse.buildship.core.launch.internal;

import org.eclipse.buildship.core.launch.LaunchConfigurationTools;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Empty implementation of the {@code LaunchConfigurationTools} interface.
 */
public final class EmptyLaunchConfigurationTools implements LaunchConfigurationTools {

    @Override
    public void launch(ILaunchConfiguration configuration, String mode) {
        throw new UnsupportedOperationException("The Core Plugin does not support launching launch configurations.");
    }

}
