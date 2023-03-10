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

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import org.eclipse.buildship.core.GradleDistribution;
import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;

/**
 * Contains the attributes that describe a Gradle run configuration.
 */
public final class GradleRunConfigurationAttributes extends BaseRunConfigurationAttributes {

    // keys used when setting/getting attributes from an ILaunchConfiguration instance
    private static final String TASKS = "tasks";

    private final ImmutableList<String> tasks;

    public GradleRunConfigurationAttributes(List<String> tasks, String workingDirExpression, String gradleDistribution, String gradleUserHomeExpression,
            String javaHomeExpression, List<String> jvmArgumentExpressions, List<String> argumentExpressions, boolean showExecutionView, boolean showConsoleView, boolean overrideWorkspaceSettings, boolean isOffline, boolean isBuildScansEnabled) {
        super(workingDirExpression, gradleDistribution, gradleUserHomeExpression, javaHomeExpression, jvmArgumentExpressions, argumentExpressions, showExecutionView, showConsoleView, overrideWorkspaceSettings, isOffline, isBuildScansEnabled);
        this.tasks = ImmutableList.copyOf(tasks);
    }

    public ImmutableList<String> getTasks() {
        return this.tasks;
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

    public void apply(ILaunchConfigurationWorkingCopy launchConfiguration) {
        super.apply(launchConfiguration);
        applyTasks(this.tasks, launchConfiguration);
    }

    public static void applyTasks(List<String> tasks, ILaunchConfigurationWorkingCopy launchConfiguration) {
        launchConfiguration.setAttribute(TASKS, tasks);
    }

    public static GradleRunConfigurationAttributes from(ILaunchConfiguration launchConfiguration) {
        Preconditions.checkNotNull(launchConfiguration);
        List<String> tasks = getListAttribute(TASKS, launchConfiguration);
        String workingDirExpression = getStringAttribute(WORKING_DIR, "", launchConfiguration);

        String gradleDistribution = getStringAttribute(GRADLE_DISTRIBUTION, GradleDistribution.fromBuild().toString(), launchConfiguration);
        String gradleUserHomeExpression = getStringAttribute(GRADLE_USER_HOME, null, launchConfiguration);
        String javaHomeExpression = getStringAttribute(JAVA_HOME, null, launchConfiguration);
        List<String> jvmArgumentExpressions = getListAttribute(JVM_ARGUMENTS, launchConfiguration);
        List<String> argumentExpressions = getListAttribute(ARGUMENTS, launchConfiguration);
        boolean showExecutionView = getBooleanAttribute(SHOW_EXECUTION_VIEW, true, launchConfiguration);
        boolean showConsoleView = getBooleanAttribute(SHOW_CONSOLE_VIEW, true, launchConfiguration);
        boolean overrideWorkspaceSettings = getBooleanAttribute(OVERRIDE_BUILD_SETTINGS, false, launchConfiguration);
        boolean isOffline = getBooleanAttribute(OFFLINE_MODE, false, launchConfiguration);
        boolean isBuildScansEnabled = getBooleanAttribute(BUILD_SCANS_ENABLED, false, launchConfiguration);
        return new GradleRunConfigurationAttributes(tasks, workingDirExpression, gradleDistribution, gradleUserHomeExpression, javaHomeExpression, jvmArgumentExpressions, argumentExpressions,
                showExecutionView, showConsoleView, overrideWorkspaceSettings, isOffline, isBuildScansEnabled);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GradleRunConfigurationAttributes) {
            GradleRunConfigurationAttributes other = (GradleRunConfigurationAttributes) obj;
            return super.equals(obj) && Objects.equal(this.tasks, other.tasks);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), this.tasks);
    }
}
