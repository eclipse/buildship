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

import java.util.Collections;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import org.eclipse.buildship.core.GradleDistribution;
import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.configuration.Test;

/**
 * Contains the attributes that describe a Gradle run configuration.
 */
// TODO (donat) add test coverage for new attributes
public class GradleTestRunConfigurationAttributes extends BaseRunConfigurationAttributes {

    // keys used when setting/getting attributes from an ILaunchConfiguration instance
    private static final String TEST_ClASSES = "tests";

    private final List<String> testNames;

    public GradleTestRunConfigurationAttributes(String workingDirExpression, String gradleDistribution, String gradleUserHomeExpression,
            String javaHomeExpression, List<String> jvmArgumentExpressions, List<String> argumentExpressions,
            boolean showExecutionView, boolean showConsoleView, boolean overrideWorkspaceSettings,
            boolean isOffline, boolean isBuildScansEnabled, List<String> testNames) {
        super(workingDirExpression, gradleDistribution, gradleUserHomeExpression, javaHomeExpression, jvmArgumentExpressions, argumentExpressions, showExecutionView, showConsoleView, overrideWorkspaceSettings, isOffline, isBuildScansEnabled);
        this.testNames = testNames;
    }

    public List<String> getTestNames() {
        return this.testNames;
    }

    public List<Test> getTests() {
        return Test.fromString(this.testNames);
    }

    public boolean hasSameUniqueAttributes(ILaunchConfiguration launchConfiguration) {
        try {
                return this.workingDirExpression.equals(launchConfiguration.getAttribute(WORKING_DIR, ""))
                        && this.testNames.equals(launchConfiguration.getAttribute(TEST_ClASSES, Collections.<String>emptyList()));
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot read Gradle launch configuration %s.", launchConfiguration), e);
        }
    }

    public void apply(ILaunchConfigurationWorkingCopy launchConfiguration) {
        super.apply(launchConfiguration);
        applyTestNames(this.testNames, launchConfiguration);
    }

    public static void applyTestNames(List<String> testNames, ILaunchConfigurationWorkingCopy launchConfiguration) {
        launchConfiguration.setAttribute(TEST_ClASSES, testNames);
    }

    public static GradleTestRunConfigurationAttributes from(ILaunchConfiguration launchConfiguration) {
        Preconditions.checkNotNull(launchConfiguration);
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
        List<String> testNames = getListAttribute(TEST_ClASSES, launchConfiguration);

        return new GradleTestRunConfigurationAttributes(workingDirExpression, gradleDistribution, gradleUserHomeExpression, javaHomeExpression, jvmArgumentExpressions, argumentExpressions,
                showExecutionView, showConsoleView, overrideWorkspaceSettings, isOffline, isBuildScansEnabled, testNames);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GradleTestRunConfigurationAttributes) {
            GradleTestRunConfigurationAttributes other = (GradleTestRunConfigurationAttributes) obj;
            return super.equals(obj)
                    && Objects.equal(this.testNames, other.testNames);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), this.testNames);
    }

}
