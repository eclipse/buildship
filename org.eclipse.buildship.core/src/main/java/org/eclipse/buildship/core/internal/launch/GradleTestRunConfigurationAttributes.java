/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
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
    private static final String TEST_TASK = "test_task";
    private static final String TEST_ClASSES2 = "test_classes";
    private static final String TEST_METHODS = "test_methods";
    private static final String TEST_PACKAGES = "test_packages";
    private static final String TEST_PATTERNS = "test_patterns";

    private final List<String> testNames;
    private final String testTask;
    private final List<String> testClasses;
    private final List<String> testMethods;
    private final List<String> testPackages;
    private final List<String> testPatterns;


    public GradleTestRunConfigurationAttributes(
            String workingDirExpression,
            String gradleDistribution,
            String gradleUserHomeExpression,
            String javaHomeExpression,
            List<String> jvmArgumentExpressions,
            List<String> argumentExpressions,
            boolean showExecutionView,
            boolean showConsoleView,
            boolean overrideWorkspaceSettings,
            boolean isOffline,
            boolean isBuildScansEnabled,
            List<String> testNames, // will run on all tests
            String testTask, // fields below will run with this specific tests
            List<String> testClasses,
            List<String> testMethods,
            List<String> testPackages,
            List<String> testPatterns
            ) {
        super(workingDirExpression, gradleDistribution, gradleUserHomeExpression, javaHomeExpression, jvmArgumentExpressions, argumentExpressions, showExecutionView, showConsoleView, overrideWorkspaceSettings, isOffline, isBuildScansEnabled);
        this.testNames = testNames;
        this.testTask = testTask;
        this.testClasses = testClasses;
        this.testMethods = testMethods;
        this.testPackages = testPackages;
        this.testPatterns = testPatterns;

    }

    public List<String> getTestNames() {
        return this.testNames;
    }


    public String getTestTask() {
        return this.testTask;
    }


    public List<String> getTestClasses() {
        return this.testClasses;
    }


    public List<String> getTestMethods() {
        return this.testMethods;
    }


    public List<String> getTestPackages() {
        return this.testPackages;
    }


    public List<String> getTestPatterns() {
        return this.testPatterns;
    }

    public List<Test> getTests() {
        return Test.fromString(this.testNames);
    }

    public boolean hasSameUniqueAttributes(ILaunchConfiguration launchConfiguration) {
        try {
                return this.workingDirExpression.equals(launchConfiguration.getAttribute(WORKING_DIR, ""))
                    && this.testNames.equals(launchConfiguration.getAttribute(TEST_ClASSES, Collections.<String>emptyList()));
//                    && this.testTask.equals(launchConfiguration.getAttribute(TEST_TASK, ""))
//                    && this.testClasses.equals(launchConfiguration.getAttribute(TEST_ClASSES, Collections.<String>emptyList()))
//                    && this.testMethods.equals(launchConfiguration.getAttribute(TEST_METHODS, Collections.<String>emptyList()))
//                    && this.testPackages.equals(launchConfiguration.getAttribute(TEST_PACKAGES, Collections.<String>emptyList()))
//                    && this.testPatterns.equals(launchConfiguration.getAttribute(TEST_PATTERNS, Collections.<String>emptyList()));

        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot read Gradle launch configuration %s.", launchConfiguration), e);
        }
    }

    @Override
    public void apply(ILaunchConfigurationWorkingCopy launchConfiguration) {
        super.apply(launchConfiguration);
        applyTestNames(this.testNames, launchConfiguration, this.testTask, this.testClasses, this.testMethods, this.testPackages, this.testPatterns);
    }

    public static void applyTestNames(List<String> testNames, ILaunchConfigurationWorkingCopy launchConfiguration, String testTask, List<String> testClasses, List<String> testMethods, List<String> testPackages, List<String> testPatterns) {
        launchConfiguration.setAttribute(TEST_ClASSES, testNames);
        launchConfiguration.setAttribute(TEST_TASK, testTask);
        launchConfiguration.setAttribute(TEST_ClASSES2, testClasses);
        launchConfiguration.setAttribute(TEST_METHODS, testMethods);
        launchConfiguration.setAttribute(TEST_PACKAGES, testPackages);
        launchConfiguration.setAttribute(TEST_PATTERNS, testPatterns);
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

        String testTask = getStringAttribute(TEST_TASK, "", launchConfiguration);
        List<String> testClasses = getListAttribute(TEST_ClASSES2, launchConfiguration);
        List<String> testMethods = getListAttribute(TEST_METHODS, launchConfiguration);
        List<String> testPackages = getListAttribute(TEST_PACKAGES, launchConfiguration);
        List<String> testPatterns = getListAttribute(TEST_PATTERNS, launchConfiguration);

        return new GradleTestRunConfigurationAttributes(
                workingDirExpression,
                gradleDistribution,
                gradleUserHomeExpression,
                javaHomeExpression,
                jvmArgumentExpressions,
                argumentExpressions,
                showExecutionView,
                showConsoleView,
                overrideWorkspaceSettings,
                isOffline,
                isBuildScansEnabled,
                testNames,
                testTask,
                testClasses,
                testMethods,
                testPackages,
                testPatterns);
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
