/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.launch;

import java.io.File;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.configuration.Test;
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
        for (ILaunchConfiguration launchConfiguration : getGradleLaunchConfigurations(GradleRunConfigurationDelegate.ID)) {
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

    @Override
    public ILaunchConfiguration getOrCreateTestRunConfiguration(GradleTestRunConfigurationAttributes configurationAttributes) {
        Preconditions.checkNotNull(configurationAttributes);
        Optional<ILaunchConfiguration> launchConfiguration = getTestLaunchConfiguration(configurationAttributes);
        return launchConfiguration.isPresent() ? launchConfiguration.get() : createTestLaunchConfiguration(configurationAttributes);
    }

    private Optional<ILaunchConfiguration> getTestLaunchConfiguration(GradleTestRunConfigurationAttributes configurationAttributes) {
        Preconditions.checkNotNull(configurationAttributes);
        for (ILaunchConfiguration launchConfiguration : getGradleLaunchConfigurations(GradleTestRunConfigurationDelegate.ID)) {
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

            // try to persist the launch configuration and return it
            return persistConfiguration(launchConfiguration);
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot create Gradle launch configuration %s.", launchConfigurationName), e);
        }
    }

    private ILaunchConfiguration createTestLaunchConfiguration(GradleTestRunConfigurationAttributes configurationAttributes) {
        List<Test> tests = configurationAttributes.getTests();
        String rawLaunchConfigurationName = testRunConfigName(configurationAttributes.getWorkingDir(), tests);

        String launchConfigurationName = this.launchManager.generateLaunchConfigurationName(rawLaunchConfigurationName.replace(':', '.'));
        ILaunchConfigurationType launchConfigurationType = this.launchManager.getLaunchConfigurationType(GradleTestRunConfigurationDelegate.ID);

        try {
            // create new launch configuration instance
            ILaunchConfigurationWorkingCopy launchConfiguration = launchConfigurationType.newInstance(null, launchConfigurationName);

            // configure the launch configuration
            configurationAttributes.apply(launchConfiguration);

            // try to persist the launch configuration and return it
            return persistConfiguration(launchConfiguration);
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot create Gradle launch configuration %s.", launchConfigurationName), e);
        }
    }

    private String testRunConfigName(File workingDir, List<Test> tests) {
        // TODO (donat) add test coverage
        String rawLaunchConfigurationName;
        if (tests.isEmpty()) {
            rawLaunchConfigurationName = workingDir.getName();
        } else {
            rawLaunchConfigurationName = tests.get(0).getSimpleName() + ( tests.size() > 1 ? " (and " + (tests.size() - 1) + " more)" : "");
        }
        return rawLaunchConfigurationName;
    }

    private ILaunchConfiguration persistConfiguration(ILaunchConfigurationWorkingCopy launchConfiguration) {
        try {
            return launchConfiguration.doSave();
        } catch (CoreException e) {
            CorePlugin.logger().warn("Cannot persist Gradle launch configuration", e);
            return launchConfiguration;
        }
    }

    private ILaunchConfiguration[] getGradleLaunchConfigurations(String launchConfigID) {
        ILaunchConfigurationType launchConfigurationType = this.launchManager.getLaunchConfigurationType(launchConfigID);

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
