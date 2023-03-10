/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.launch;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;

import org.eclipse.buildship.core.internal.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.core.internal.launch.GradleLaunchConfigurationManager;
import org.eclipse.buildship.core.internal.launch.GradleTestRunConfigurationAttributes;

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
    public ILaunchConfiguration getOrCreateTestRunConfiguration(GradleTestRunConfigurationAttributes configurationAttributes) {
        return this.delegate.getOrCreateTestRunConfiguration(configurationAttributes);
    }

    @Override
    public void launch(ILaunchConfiguration configuration, String mode) {
        DebugUITools.launch(configuration, mode);
    }

}
