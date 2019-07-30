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

package org.eclipse.buildship.core.internal.launch;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.util.collections.CollectionsUtils;
import org.eclipse.buildship.core.internal.util.variable.ExpressionUtils;

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
    public ILaunchConfiguration getOrCreateTestRunConfiguration(IProject project, List<IType> types, List<IMethod> methods) {
        Optional<ILaunchConfiguration> launchConfiguration = getTestRunConfiguration(project, types, methods);
        return launchConfiguration.isPresent() ? launchConfiguration.get() : createTestLaunchConfiguration(project, types, methods);
    }

    private Optional<ILaunchConfiguration> getTestRunConfiguration(IProject project, List<IType> types, List<IMethod> methods) {
        try {
            Collection<String> st = serializeTypes(types);
            Collection<String> sm = serializeMethods(methods);

            for (ILaunchConfiguration lc : getGradleLaunchConfigurations(GradleTestLaunchConfigurationDelegate.ID)) {
                String workingDir = lc.getAttribute("working_dir", "");
                List<String> testClasses = lc.getAttribute("test_classes", Collections.emptyList());
                List<String> testMethods = lc.getAttribute("test_methods", Collections.emptyList());

                if (project.getLocation() != null && project.getLocation().toPortableString().equals(ExpressionUtils.decode(workingDir)) && testClasses.equals(st) && testMethods.equals(sm)) {
                        return Optional.of(lc);
                }
            }
        } catch (CoreException e) {
            CorePlugin.logger().warn("Cannot look up existing test launch configurations", e);
        }
        return Optional.absent();

    }

    private static Collection<String> serializeTypes(List<IType> types) {
        return types.stream().map(t -> TestType.from(t).getQualifiedName()).collect(Collectors.<String>toList());
    }

    private static Collection<String> serializeMethods(List<IMethod> methods) {
        return methods.stream().map(t -> TestMethod.from(t).getQualifiedName()).collect(Collectors.<String> toList());
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

    private ILaunchConfiguration createTestLaunchConfiguration(IProject project, List<IType> types, List<IMethod> methods) {
        String rawLaunchConfigurationName = String.format("Testing %s", project.getName());
        String launchConfigurationName = this.launchManager.generateLaunchConfigurationName(rawLaunchConfigurationName.replace(':', '.'));
        ILaunchConfigurationType launchConfigurationType = this.launchManager.getLaunchConfigurationType(GradleTestLaunchConfigurationDelegate.ID);

        try {
            // create new launch configuration instance
            ILaunchConfigurationWorkingCopy launchConfiguration = launchConfigurationType.newInstance(null, launchConfigurationName);

            // configure the launch configuration
            launchConfiguration.setAttribute("working_dir", ExpressionUtils.encodeWorkspaceLocation(project));
            launchConfiguration.setAttribute("test_classes", serializeTypes(types));
            launchConfiguration.setAttribute("test_methods", serializeMethods(methods));

            // try to persist the launch configuration and return it
            return persistConfiguration(launchConfiguration);
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot create Gradle launch configuration %s.", launchConfigurationName), e);
        }
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
