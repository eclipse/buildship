/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
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

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import org.eclipse.buildship.core.GradleDistribution;
import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.util.file.FileUtils;
import org.eclipse.buildship.core.internal.util.variable.ExpressionUtils;

/**
 * Contains launch attributes common to all Gradle run configurations.
 */
public abstract class BaseRunConfigurationAttributes {
    // keys used when setting/getting attributes from an ILaunchConfiguration instance
    protected static final String WORKING_DIR = "working_dir";
    protected static final String GRADLE_DISTRIBUTION = "gradle_distribution";
    protected static final String GRADLE_USER_HOME = "gradle_user_home";
    protected static final String JAVA_HOME = "java_home";
    protected static final String JVM_ARGUMENTS = "jvm_arguments";
    protected static final String ARGUMENTS = "arguments";
    protected static final String SHOW_EXECUTION_VIEW = "show_execution_view";
    protected static final String SHOW_CONSOLE_VIEW = "show_console_view";
    protected static final String OVERRIDE_BUILD_SETTINGS = "override_workspace_settings";
    protected static final String OFFLINE_MODE = "offline_mode";
    protected static final String BUILD_SCANS_ENABLED = "build_scans_enabled";

    protected final String workingDirExpression;
    protected final String gradleDistribution;
    protected final String gradleUserHomeExpression;
    protected final String javaHomeExpression;
    protected final ImmutableList<String> jvmArgumentExpressions;
    protected final ImmutableList<String> argumentExpressions;
    protected final boolean showExecutionView;
    protected final boolean showConsoleView;
    protected final boolean overrideBuildSettings;
    protected final boolean isOffline;
    protected final boolean isBuildScansEnabled;

    protected BaseRunConfigurationAttributes(String workingDirExpression, String gradleDistribution, String gradleUserHomeExpression,
            String javaHomeExpression, List<String> jvmArgumentExpressions, List<String> argumentExpressions, boolean showExecutionView, boolean showConsoleView, boolean overrideWorkspaceSettings, boolean isOffline, boolean isBuildScansEnabled) {
        this.workingDirExpression = Preconditions.checkNotNull(workingDirExpression);
        this.gradleDistribution = gradleDistribution;
        this.gradleUserHomeExpression = gradleUserHomeExpression;
        this.javaHomeExpression = javaHomeExpression;
        this.jvmArgumentExpressions = ImmutableList.copyOf(jvmArgumentExpressions);
        this.argumentExpressions = ImmutableList.copyOf(argumentExpressions);
        this.showExecutionView = showExecutionView;
        this.showConsoleView = showConsoleView;
        this.overrideBuildSettings = overrideWorkspaceSettings;
        this.isOffline = isOffline;
        this.isBuildScansEnabled = isBuildScansEnabled;
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

    public GradleDistribution getGradleDistribution() {
        try {
            return GradleDistribution.fromString(this.gradleDistribution);
        } catch (RuntimeException ignore) {
            return GradleDistribution.fromBuild();
        }
    }

    public String getGradleUserHomeHomeExpression() {
        return this.gradleUserHomeExpression;
    }

    public File getGradleUserHome() {
        try {
            String location = ExpressionUtils.decode(this.gradleUserHomeExpression);
            return FileUtils.getAbsoluteFile(location).orNull();
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot resolve Gradle user home directory expression %s.", this.javaHomeExpression));
        }
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

    public boolean isOverrideBuildSettings() {
        return this.overrideBuildSettings;
    }

    public boolean isOffline() {
        return this.isOffline;
    }

    public boolean isBuildScansEnabled() {
        return this.isBuildScansEnabled;
    }

    public void apply(ILaunchConfigurationWorkingCopy launchConfiguration) {
        applyWorkingDirExpression(this.workingDirExpression, launchConfiguration);
        applyGradleDistribution(this.gradleDistribution, launchConfiguration);
        applyGradleUserHomeExpression(this.gradleUserHomeExpression, launchConfiguration);
        applyJavaHomeExpression(this.javaHomeExpression, launchConfiguration);
        applyJvmArgumentExpressions(this.jvmArgumentExpressions, launchConfiguration);
        applyArgumentExpressions(this.argumentExpressions, launchConfiguration);
        applyShowExecutionView(this.showExecutionView, launchConfiguration);
        applyShowConsoleView(this.showConsoleView, launchConfiguration);
        applyOverrideBuildSettings(this.overrideBuildSettings, launchConfiguration);
        applyOfflineMode(this.isOffline, launchConfiguration);
        applyBuildScansEnabled(this.isBuildScansEnabled, launchConfiguration);
    }

    public static void applyWorkingDirExpression(String workingDirExpression, ILaunchConfigurationWorkingCopy launchConfiguration) {
        launchConfiguration.setAttribute(WORKING_DIR, Preconditions.checkNotNull(workingDirExpression));
    }

    public static void applyGradleDistribution(String gradleDistribution, ILaunchConfigurationWorkingCopy launchConfiguration) {
        launchConfiguration.setAttribute(GRADLE_DISTRIBUTION, gradleDistribution);
    }

    public static void applyGradleDistribution(GradleDistribution gradleDistribution, ILaunchConfigurationWorkingCopy launchConfiguration) {
        launchConfiguration.setAttribute(GRADLE_DISTRIBUTION, gradleDistribution.toString());
    }

    public static void applyGradleUserHomeExpression(String gradleUserHomeExpression, ILaunchConfigurationWorkingCopy launchConfiguration) {
        launchConfiguration.setAttribute(GRADLE_USER_HOME, gradleUserHomeExpression);
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

    public static void applyOverrideBuildSettings(boolean overrideWorkspaceSettings, ILaunchConfigurationWorkingCopy launchConfiguration) {
        launchConfiguration.setAttribute(OVERRIDE_BUILD_SETTINGS, overrideWorkspaceSettings);
    }

    public static void applyOfflineMode(boolean offlineMode, ILaunchConfigurationWorkingCopy launchConfiguration) {
        launchConfiguration.setAttribute(OFFLINE_MODE, offlineMode);
    }

    public static void applyBuildScansEnabled(boolean buildScansEnabled, ILaunchConfigurationWorkingCopy launchConfiguration) {
        launchConfiguration.setAttribute(BUILD_SCANS_ENABLED, buildScansEnabled);
    }

    protected static List<String> getListAttribute(String name, ILaunchConfiguration configuration) {
        try {
            return configuration.getAttribute(name, ImmutableList.<String>of());
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot read launch configuration attribute '%s'.", name));
        }
    }

    protected static String getStringAttribute(String name, String defaultValue, ILaunchConfiguration configuration) {
        try {
            return configuration.getAttribute(name, defaultValue);
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot read launch configuration attribute '%s'.", name));
        }
    }

    protected static boolean getBooleanAttribute(String name, boolean defaultValue, ILaunchConfiguration configuration) {
        try {
            return configuration.getAttribute(name, defaultValue);
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot read launch configuration attribute '%s'.", name));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GradleRunConfigurationAttributes) {
            BaseRunConfigurationAttributes other = (BaseRunConfigurationAttributes) obj;
            return Objects.equal(this.workingDirExpression, other.workingDirExpression)
                    && Objects.equal(this.gradleDistribution, other.gradleDistribution)
                    && Objects.equal(this.gradleUserHomeExpression, other.gradleUserHomeExpression)
                    && Objects.equal(this.javaHomeExpression, other.javaHomeExpression)
                    && Objects.equal(this.jvmArgumentExpressions, other.jvmArgumentExpressions)
                    && Objects.equal(this.argumentExpressions, other.argumentExpressions)
                    && Objects.equal(this.showExecutionView, other.showExecutionView)
                    && Objects.equal(this.showConsoleView, other.showConsoleView)
                    && Objects.equal(this.overrideBuildSettings, other.overrideBuildSettings)
                    && Objects.equal(this.isOffline, other.isOffline)
                    && Objects.equal(this.isBuildScansEnabled, other.isBuildScansEnabled);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.workingDirExpression,
                    this.gradleDistribution,
                    this.gradleUserHomeExpression,
                    this.javaHomeExpression,
                    this.jvmArgumentExpressions,
                    this.argumentExpressions,
                    this.showExecutionView,
                    this.showConsoleView,
                    this.overrideBuildSettings,
                    this.isOffline,
                    this.isBuildScansEnabled);
    }
}
