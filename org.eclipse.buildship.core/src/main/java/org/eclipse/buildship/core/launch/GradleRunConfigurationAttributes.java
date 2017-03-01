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

package org.eclipse.buildship.core.launch;

import java.io.File;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingclient.GradleDistribution;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.util.configuration.FixedRequestAttributesBuilder;
import org.eclipse.buildship.core.util.file.FileUtils;
import org.eclipse.buildship.core.util.gradle.GradleDistributionSerializer;
import org.eclipse.buildship.core.util.variable.ExpressionUtils;

/**
 * Contains the attributes that describe a Gradle run configuration.
 */
public final class GradleRunConfigurationAttributes {

    // keys used when setting/getting attributes from an ILaunchConfiguration instance
    private static final String TASKS = "tasks";
    private static final String WORKING_DIR = "working_dir";
    private static final String USE_GRADLE_DISTRIBUTION_FROM_IMPORT = "use_gradle_distribution_from_import";
    private static final String GRADLE_DISTRIBUTION = "gradle_distribution";
    private static final String JAVA_HOME = "java_home";
    private static final String JVM_ARGUMENTS = "jvm_arguments";
    private static final String ARGUMENTS = "arguments";
    private static final String SHOW_EXECUTION_VIEW = "show_execution_view";
    private static final String SHOW_CONSOLE_VIEW = "show_console_view";
    private static final String OVERRIDE_WORKSPACE_SETTINGS = "override_workspace_settings";
    private static final String OFFLINE_MODE = "offline_mode";
    private static final String BUILD_SCANS_ENABLED = "build_scans_enabled";

    private final ImmutableList<String> tasks;
    private final String workingDirExpression;
    private final boolean useGradleDistributionFromImport;
    private final String gradleDistribution;
    private final String javaHomeExpression;
    private final ImmutableList<String> jvmArgumentExpressions;
    private final ImmutableList<String> argumentExpressions;
    private final boolean showExecutionView;
    private final boolean showConsoleView;
    private final boolean overrideWorkspaceSettings;
    private final boolean isOffline;
    private final boolean isBuildScansEnabled;

    private GradleRunConfigurationAttributes(List<String> tasks, String workingDirExpression, String gradleDistribution,
            String javaHomeExpression, List<String> jvmArgumentExpressions, List<String> argumentExpressions, boolean showExecutionView, boolean showConsoleView, boolean useGradleDistributionFromImport,
            boolean overrideWorkspaceSettings, boolean isOffline, boolean isBuildScansEnabled) {
        this.tasks = ImmutableList.copyOf(tasks);
        this.workingDirExpression = Preconditions.checkNotNull(workingDirExpression);
        this.gradleDistribution = gradleDistribution;
        this.javaHomeExpression = javaHomeExpression;
        this.jvmArgumentExpressions = ImmutableList.copyOf(jvmArgumentExpressions);
        this.argumentExpressions = ImmutableList.copyOf(argumentExpressions);
        this.showExecutionView = showExecutionView;
        this.showConsoleView = showConsoleView;
        this.useGradleDistributionFromImport = useGradleDistributionFromImport;
        this.overrideWorkspaceSettings = overrideWorkspaceSettings;
        this.isOffline = isOffline;
        this.isBuildScansEnabled = isBuildScansEnabled;
    }

    public ImmutableList<String> getTasks() {
        return this.tasks;
    }

    public String getWorkingDirExpression() {
        return this.workingDirExpression;
    }

    public File getWorkingDir() {
        try {
            String location = ExpressionUtils.decode(this.workingDirExpression);
            return new File(location).getAbsoluteFile();
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot resolve working directory expression %s.", this.workingDirExpression));
        }
    }

    public boolean isUseGradleDistributionFromImport() {
        return this.useGradleDistributionFromImport;
    }

    public GradleDistribution getGradleDistribution() {
        if (this.useGradleDistributionFromImport) {
            Optional<IProject> project = CorePlugin.workspaceOperations().findProjectByLocation(getWorkingDir());
            if (!project.isPresent()) {
                return GradleDistribution.fromBuild();
            } else {
                ProjectConfiguration configuration = CorePlugin.projectConfigurationManager().readProjectConfiguration(project.get());
                return configuration.getGradleDistribution();
            }
        } else {
            return this.gradleDistribution != null ? GradleDistributionSerializer.INSTANCE.deserializeFromString(this.gradleDistribution) : GradleDistribution.fromBuild();
        }
    }

    public File getGradleUserHome() {
        return CorePlugin.workspaceConfigurationManager().loadWorkspaceConfiguration().getGradleUserHome();
    }

    public String getJavaHomeExpression() {
        return this.javaHomeExpression;
    }

    public File getJavaHome() {
        try {
            String location = ExpressionUtils.decode(this.javaHomeExpression);
            return FileUtils.getAbsoluteFile(location).orNull();
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot resolve Java home directory expression %s.", this.javaHomeExpression));
        }
    }

    public ImmutableList<String> getJvmArgumentExpressions() {
        return this.jvmArgumentExpressions;
    }

    public ImmutableList<String> getJvmArguments() {
        return FluentIterable.from(this.jvmArgumentExpressions).transform(new Function<String, String>() {

            @Override
            public String apply(String input) {
                try {
                    return ExpressionUtils.decode(input);
                } catch (CoreException e) {
                    throw new GradlePluginsRuntimeException(String.format("Cannot resolve JVM argument expression %s.", input));
                }
            }
        }).toList();
    }

    public ImmutableList<String> getArgumentExpressions() {
        return this.argumentExpressions;
    }

    public ImmutableList<String> getArguments() {
        return FluentIterable.from(this.argumentExpressions).transform(new Function<String, String>() {

            @Override
            public String apply(String input) {
                try {
                    return ExpressionUtils.decode(input);
                } catch (CoreException e) {
                    throw new GradlePluginsRuntimeException(String.format("Cannot resolve argument expression %s.", input));
                }
            }
        }).toList();
    }

    public boolean isShowExecutionView() {
        return this.showExecutionView;
    }

    public boolean isShowConsoleView() {
        return this.showConsoleView;
    }

    public boolean isOverrideWorkspaceSettings() {
        return this.overrideWorkspaceSettings;
    }

    public boolean isOffline() {
        return this.isOffline;
    }

    public boolean isBuildScansEnabled() {
        return this.isBuildScansEnabled;
    }

    public boolean hasSameUniqueAttributes(ILaunchConfiguration launchConfiguration) {
        // reuse an existing run configuration if the working directory and the tasks are the same,
        // regardless of the other settings of the launch configuration
        try {
            return this.tasks.equals(launchConfiguration.getAttribute(TASKS, ImmutableList.<String> of()))
                    && this.workingDirExpression.equals(launchConfiguration.getAttribute(WORKING_DIR, ""));
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot read Gradle launch configuration %s.", launchConfiguration), e);
        }
    }

    public FixedRequestAttributes toFixedRequestAttributes() {
        FixedRequestAttributesBuilder builder = FixedRequestAttributesBuilder.fromWorkspaceSettings(getWorkingDir())
                .gradleUserHome(getGradleUserHome())
                .gradleDistribution(getGradleDistribution())
                .javaHome(getJavaHome())
                .jvmArguments(getJvmArguments())
                .arguments(getArguments());
        if (isOverrideWorkspaceSettings()) {
            builder.buildScansEnabled(isBuildScansEnabled());
            builder.offlineMode(isOffline());
        }
        return builder.build();
    }

    public void apply(ILaunchConfigurationWorkingCopy launchConfiguration) {
        applyTasks(this.tasks, launchConfiguration);
        applyWorkingDirExpression(this.workingDirExpression, launchConfiguration);
        applyUseGradleDistributionFromImport(this.useGradleDistributionFromImport, launchConfiguration);
        applyGradleDistribution(this.gradleDistribution, launchConfiguration);
        applyJavaHomeExpression(this.javaHomeExpression, launchConfiguration);
        applyJvmArgumentExpressions(this.jvmArgumentExpressions, launchConfiguration);
        applyArgumentExpressions(this.argumentExpressions, launchConfiguration);
        applyShowExecutionView(this.showExecutionView, launchConfiguration);
        applyShowConsoleView(this.showConsoleView, launchConfiguration);
        applyWorkspaceOverride(this.overrideWorkspaceSettings, this.isOffline, this.isBuildScansEnabled, launchConfiguration);
    }

    public static void applyTasks(List<String> tasks, ILaunchConfigurationWorkingCopy launchConfiguration) {
        launchConfiguration.setAttribute(TASKS, tasks);
    }

    public static void applyWorkingDirExpression(String workingDirExpression, ILaunchConfigurationWorkingCopy launchConfiguration) {
        launchConfiguration.setAttribute(WORKING_DIR, Preconditions.checkNotNull(workingDirExpression));
    }

    public static void applyUseGradleDistributionFromImport(boolean useGradleDistributionFromImport, ILaunchConfigurationWorkingCopy launchConfiguration) {
        launchConfiguration.setAttribute(USE_GRADLE_DISTRIBUTION_FROM_IMPORT, useGradleDistributionFromImport);
    }

    public static void applyGradleDistribution(String gradleDistribution, ILaunchConfigurationWorkingCopy launchConfiguration) {
        launchConfiguration.setAttribute(GRADLE_DISTRIBUTION, gradleDistribution);
    }

    public static void applyGradleDistribution(GradleDistribution gradleDistribution, ILaunchConfigurationWorkingCopy launchConfiguration) {
        launchConfiguration.setAttribute(GRADLE_DISTRIBUTION, Preconditions.checkNotNull(GradleDistributionSerializer.INSTANCE.serializeToString(gradleDistribution)));
    }

    public static void applyJavaHomeExpression(String javaHomeExpression, ILaunchConfigurationWorkingCopy launchConfiguration) {
        launchConfiguration.setAttribute(JAVA_HOME, javaHomeExpression);
    }

    public static void applyJvmArgumentExpressions(List<String> jvmArgumentsExpression, ILaunchConfigurationWorkingCopy launchConfiguration) {
        launchConfiguration.setAttribute(JVM_ARGUMENTS, jvmArgumentsExpression);
    }

    public static void applyArgumentExpressions(List<String> argumentsExpression, ILaunchConfigurationWorkingCopy launchConfiguration) {
        launchConfiguration.setAttribute(ARGUMENTS, argumentsExpression);
    }

    public static void applyShowExecutionView(boolean showExecutionView, ILaunchConfigurationWorkingCopy launchConfiguration) {
        launchConfiguration.setAttribute(SHOW_EXECUTION_VIEW, showExecutionView);
    }

    public static void applyShowConsoleView(boolean showConsoleView, ILaunchConfigurationWorkingCopy launchConfiguration) {
        launchConfiguration.setAttribute(SHOW_CONSOLE_VIEW, showConsoleView);
    }

    public static void applyWorkspaceOverride(boolean overrideWorkspaceSettings, boolean offlineMode, boolean buildScansEnabled, ILaunchConfigurationWorkingCopy launchConfiguration) {
        launchConfiguration.setAttribute(OVERRIDE_WORKSPACE_SETTINGS, overrideWorkspaceSettings);
        if (overrideWorkspaceSettings) {
            launchConfiguration.setAttribute(OFFLINE_MODE, offlineMode);
            launchConfiguration.setAttribute(BUILD_SCANS_ENABLED, buildScansEnabled);
        } else {
            launchConfiguration.removeAttribute(OFFLINE_MODE);
            launchConfiguration.removeAttribute(BUILD_SCANS_ENABLED);
        }
    }

    public static GradleRunConfigurationAttributes with(List<String> tasks, String workingDirExpression, String serializedGradleDistribution,
            String javaHomeExpression, List<String> jvmArgumentExpressions, List<String> argumentExpressions, boolean showExecutionView, boolean showConsoleView, boolean useGradleDistributionFromImport,
            boolean overrideWorkspaceSettings, boolean isOffline, boolean isBuildScansEnabled) {
        return new GradleRunConfigurationAttributes(tasks, workingDirExpression, serializedGradleDistribution, javaHomeExpression, jvmArgumentExpressions,
                argumentExpressions, showExecutionView, showConsoleView, useGradleDistributionFromImport, overrideWorkspaceSettings, isOffline, isBuildScansEnabled);
    }

    public static GradleRunConfigurationAttributes with(List<String> tasks, String workingDirExpression, GradleDistribution gradleDistribution,
            String javaHomeExpression, List<String> jvmArgumentExpressions, List<String> argumentExpressions, boolean showExecutionView, boolean showConsoleView, boolean useGradleDistributionFromImport) {
        String serializedDistribution = GradleDistributionSerializer.INSTANCE.serializeToString(gradleDistribution);
        return new GradleRunConfigurationAttributes(tasks, workingDirExpression, serializedDistribution, javaHomeExpression, jvmArgumentExpressions,
                argumentExpressions, showExecutionView, showConsoleView, useGradleDistributionFromImport, false, false, false);
    }

    public static GradleRunConfigurationAttributes from(ILaunchConfiguration launchConfiguration) {
        Preconditions.checkNotNull(launchConfiguration);

        List<String> tasks = getListAttribute(TASKS, launchConfiguration);
        String workingDirExpression = getStringAttribute(WORKING_DIR, "", launchConfiguration);
        boolean useGradleDistributionFromImport = getBooleanAttribute(USE_GRADLE_DISTRIBUTION_FROM_IMPORT, false, launchConfiguration);
        String gradleDistribution = getStringAttribute(GRADLE_DISTRIBUTION, "", launchConfiguration);
        String javaHomeExpression = getStringAttribute(JAVA_HOME, null, launchConfiguration);
        List<String> jvmArgumentExpressions = getListAttribute(JVM_ARGUMENTS, launchConfiguration);
        List<String> argumentExpressions = getListAttribute(ARGUMENTS, launchConfiguration);
        boolean showExecutionView = getBooleanAttribute(SHOW_EXECUTION_VIEW, true, launchConfiguration);
        boolean showConsoleView = getBooleanAttribute(SHOW_CONSOLE_VIEW, true, launchConfiguration);
        boolean overrideWorkspaceSettings = getBooleanAttribute(OVERRIDE_WORKSPACE_SETTINGS, false, launchConfiguration);
        if (overrideWorkspaceSettings) {
            boolean isOffline = getBooleanAttribute(OFFLINE_MODE, false, launchConfiguration);
            boolean isBuildScansEnabled = getBooleanAttribute(BUILD_SCANS_ENABLED, false, launchConfiguration);
            return with(tasks, workingDirExpression, gradleDistribution, javaHomeExpression, jvmArgumentExpressions, argumentExpressions,
                    showExecutionView, showConsoleView, useGradleDistributionFromImport, true, isOffline, isBuildScansEnabled);
        } else {
            return with(tasks, workingDirExpression, gradleDistribution, javaHomeExpression, jvmArgumentExpressions, argumentExpressions,
                    showExecutionView, showConsoleView, useGradleDistributionFromImport, false, false, false);
        }
    }

    private static List<String> getListAttribute(String name, ILaunchConfiguration configuration) {
        try {
            return configuration.getAttribute(name, ImmutableList.<String>of());
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot read launch configuration attribute '%s'.", name));
        }
    }

    private static String getStringAttribute(String name, String defaultValue, ILaunchConfiguration configuration) {
        try {
            return configuration.getAttribute(name, defaultValue);
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot read launch configuration attribute '%s'.", name));
        }
    }

    private static boolean getBooleanAttribute(String name, boolean defaultValue, ILaunchConfiguration configuration) {
        try {
            return configuration.getAttribute(name, defaultValue);
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot read launch configuration attribute '%s'.", name));
        }
    }

}
