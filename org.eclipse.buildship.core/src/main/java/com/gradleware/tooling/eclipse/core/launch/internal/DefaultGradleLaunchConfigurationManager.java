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

package com.gradleware.tooling.eclipse.core.launch.internal;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.gradleware.tooling.eclipse.core.CorePlugin;
import com.gradleware.tooling.eclipse.core.GradlePluginsRuntimeException;
import com.gradleware.tooling.eclipse.core.launch.GradleLaunchConfigurationManager;
import com.gradleware.tooling.eclipse.core.launch.GradleRunConfigurationAttributes;
import com.gradleware.tooling.eclipse.core.launch.GradleRunConfigurationDelegate;
import com.gradleware.tooling.eclipse.core.util.collections.CollectionsUtils;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

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
    public ILaunchConfiguration getOrCreateRunConfiguration(GradleRunConfigurationAttributes configurationAttributes) {
        Preconditions.checkNotNull(configurationAttributes);
        Optional<ILaunchConfiguration> launchConfiguration = findLaunchConfiguration(configurationAttributes);
        return launchConfiguration.isPresent() ? launchConfiguration.get() : createLaunchConfiguration(configurationAttributes);
    }

    private Optional<ILaunchConfiguration> findLaunchConfiguration(GradleRunConfigurationAttributes configurationAttributes) {
        for (ILaunchConfiguration launchConfiguration : getGradleLaunchConfigurations()) {
            if (configurationAttributes.hasSameUniqueAttributes(launchConfiguration)) {
                return Optional.of(launchConfiguration);
            }
        }
        return Optional.absent();
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
        } catch (Exception e) {
            String message = String.format("Cannot create Gradle launch configuration %s.", launchConfigurationName);
            CorePlugin.logger().error(message, e);
            throw new GradlePluginsRuntimeException(e);
        }
    }

    private ILaunchConfiguration[] getGradleLaunchConfigurations() {
        ILaunchConfigurationType launchConfigurationType = this.launchManager.getLaunchConfigurationType(GradleRunConfigurationDelegate.ID);

        try {
            return this.launchManager.getLaunchConfigurations(launchConfigurationType);
        } catch (Exception e) {
            String message = "Cannot get Gradle launch configurations.";
            CorePlugin.logger().error(message, e);
            throw new GradlePluginsRuntimeException(message, e);
        }
    }

}
