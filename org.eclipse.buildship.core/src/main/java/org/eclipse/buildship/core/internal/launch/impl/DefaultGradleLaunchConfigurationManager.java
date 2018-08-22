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

package org.eclipse.buildship.core.internal.launch.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.launch.GradleLaunchConfigurationManager;
import org.eclipse.buildship.core.internal.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.core.internal.launch.GradleRunConfigurationDelegate;
import org.eclipse.buildship.core.internal.util.collections.CollectionsUtils;

/**
 * Default implementation of the {@link GradleLaunchConfigurationManager} interface.
 */
public final class DefaultGradleLaunchConfigurationManager implements GradleLaunchConfigurationManager {

    private final ILaunchManager launchManager;

    public DefaultGradleLaunchConfigurationManager() {
        this(DebugPlugin.getDefault().getLaunchManager());
    }

    public DefaultGradleLaunchConfigurationManager(ILaunchManager launchManager) {
        this.launchManager = Preconditions.checkNotNull(launchManager);
    }

    @Override
    public Optional<ILaunchConfiguration> getRunConfiguration(GradleRunConfigurationAttributes configurationAttributes) {
        Preconditions.checkNotNull(configurationAttributes);
        for (ILaunchConfiguration launchConfiguration : getGradleLaunchConfigurations()) {
            if (configurationAttributes.hasSameUniqueAttributes(launchConfiguration)) {
                return Optional.of(launchConfiguration);
            }
        }
        return Optional.absent();
    }

    @Override
    public ILaunchConfiguration getOrCreateRunConfiguration(GradleRunConfigurationAttributes configurationAttributes) {
        Preconditions.checkNotNull(configurationAttributes);
        Optional<ILaunchConfiguration> launchConfiguration = getRunConfiguration(configurationAttributes);
        return launchConfiguration.isPresent() ? launchConfiguration.get() : createLaunchConfiguration(configurationAttributes);
    }

    private ILaunchConfiguration createLaunchConfiguration(GradleRunConfigurationAttributes configurationAttributes) {
        // derive the name of the launch configuration from the configuration attributes
        // since the launch configuration name must not contain ':', we replace all ':' with '.'
        String taskNamesOrDefault = configurationAttributes.getTasks().isEmpty() ? "(default tasks)" : CollectionsUtils.joinWithSpace(configurationAttributes.getTasks());
        String rawLaunchConfigurationName = String.format("%s - %s", configurationAttributes.getWorkingDir().getName(), taskNamesOrDefault);
        String launchConfigurationName = this.launchManager.generateLaunchConfigurationName(rawLaunchConfigurationName.replace(':', '.'));
        ILaunchConfigurationType launchConfigurationType = this.launchManager.getLaunchConfigurationType(GradleRunConfigurationDelegate.ID);

        try {
            // create new launch configuration instance
            ILaunchConfigurationWorkingCopy launchConfiguration = launchConfigurationType.newInstance(null, launchConfigurationName);

            // configure the launch configuration
            configurationAttributes.apply(launchConfiguration);

            // persist the launch configuration and return it
            return launchConfiguration.doSave();
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot create Gradle launch configuration %s.", launchConfigurationName), e);
        }
    }

    private ILaunchConfiguration[] getGradleLaunchConfigurations() {
        ILaunchConfigurationType launchConfigurationType = this.launchManager.getLaunchConfigurationType(GradleRunConfigurationDelegate.ID);

        try {
            return this.launchManager.getLaunchConfigurations(launchConfigurationType);
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException("Cannot get Gradle launch configurations.", e);
        }
    }

    @Override
    public void launch(ILaunchConfiguration configuration, String mode) {
        try {
            // launch the run configuration in headless mode
            // if the UI plugin is activated, this implementation is replaced by the
            // DebugUITools.launch(ILaunchConfiguration,String)
            configuration.launch(mode, new NullProgressMonitor());
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }

}
