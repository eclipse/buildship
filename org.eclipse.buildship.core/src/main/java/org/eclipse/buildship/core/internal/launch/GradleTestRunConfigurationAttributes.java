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

import java.util.Collections;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import org.eclipse.buildship.core.GradleDistribution;
import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;

/**
 * Contains the attributes that describe a Gradle run configuration.
 */
// TODO (donat) add test coverage for new attributes
public class GradleTestRunConfigurationAttributes extends BaseRunConfigurationAttributes {

    // keys used when setting/getting attributes from an ILaunchConfiguration instance
    private static final String TEST_ClASSES = "tests";

    private final List<String> tests;

    public GradleTestRunConfigurationAttributes(String workingDirExpression, String gradleDistribution, String gradleUserHomeExpression,
            String javaHomeExpression, List<String> jvmArgumentExpressions, List<String> argumentExpressions,
            boolean showExecutionView, boolean showConsoleView, boolean overrideWorkspaceSettings,
            boolean isOffline, boolean isBuildScansEnabled, List<String> tests) {
        super(workingDirExpression, gradleDistribution, gradleUserHomeExpression, javaHomeExpression, jvmArgumentExpressions, argumentExpressions, showExecutionView, showConsoleView, overrideWorkspaceSettings, isOffline, isBuildScansEnabled);
        this.tests = tests;
    }

    public List<String> getTests() {
        return this.tests;
    }

    public boolean hasSameUniqueAttributes(ILaunchConfiguration launchConfiguration) {
        try {
                return this.workingDirExpression.equals(launchConfiguration.getAttribute(WORKING_DIR, ""))
                        && this.tests.equals(launchConfiguration.getAttribute(TEST_ClASSES, Collections.<String>emptyList()));
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot read Gradle launch configuration %s.", launchConfiguration), e);
        }
    }

    public void apply(ILaunchConfigurationWorkingCopy launchConfiguration) {
        super.apply(launchConfiguration);
        applyTests(this.tests, launchConfiguration);
    }

    public static void applyTests(List<String> tests, ILaunchConfigurationWorkingCopy launchConfiguration) {
        launchConfiguration.setAttribute(TEST_ClASSES, tests);
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
        List<String> tests = getListAttribute(TEST_ClASSES, launchConfiguration);
        return new GradleTestRunConfigurationAttributes(workingDirExpression, gradleDistribution, gradleUserHomeExpression, javaHomeExpression, jvmArgumentExpressions, argumentExpressions,
                showExecutionView, showConsoleView, overrideWorkspaceSettings, isOffline, isBuildScansEnabled, tests);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GradleTestRunConfigurationAttributes) {
            GradleTestRunConfigurationAttributes other = (GradleTestRunConfigurationAttributes) obj;
            return super.equals(obj)
                    && Objects.equal(this.tests, other.tests);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), this.tests);
    }

}
