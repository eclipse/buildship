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

package org.eclipse.buildship.ui.internal.launch;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;

import org.eclipse.buildship.core.internal.launch.GradleLaunchConfigurationManager;
import org.eclipse.buildship.core.internal.launch.GradleRunConfigurationAttributes;

/**
 * Decorates an original {@link GradleLaunchConfigurationManager} such that the
 * {@link #launch(ILaunchConfiguration, String)} method calls
 * {@link DebugUITools#launch(ILaunchConfiguration, String)}.
 */
public final class UiGradleLaunchConfigurationManager implements GradleLaunchConfigurationManager {

    private final GradleLaunchConfigurationManager delegate;

    public UiGradleLaunchConfigurationManager(GradleLaunchConfigurationManager delegate) {
        this.delegate = Preconditions.checkNotNull(delegate);
    }

    @Override
    public Optional<ILaunchConfiguration> getRunConfiguration(GradleRunConfigurationAttributes configurationAttributes) {
        return this.delegate.getRunConfiguration(configurationAttributes);
    }

    @Override
    public ILaunchConfiguration getOrCreateRunConfiguration(GradleRunConfigurationAttributes configurationAttributes) {
        return this.delegate.getOrCreateRunConfiguration(configurationAttributes);
    }

    @Override
    public void launch(ILaunchConfiguration configuration, String mode) {
        DebugUITools.launch(configuration, mode);
    }

}
