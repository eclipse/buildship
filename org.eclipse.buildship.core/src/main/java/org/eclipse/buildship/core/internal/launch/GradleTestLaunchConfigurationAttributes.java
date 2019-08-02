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
public class GradleTestLaunchConfigurationAttributes extends BaseLaunchConfigurationAttributes {

    // keys used when setting/getting attributes from an ILaunchConfiguration instance
    private static final String TEST_ClASSES = "test_classes";
    private static final String TEST_METHODS = "test_methods";

    private final List<String> testClasses;
    private final List<String> testMethods;

    public GradleTestLaunchConfigurationAttributes(String workingDirExpression, String gradleDistribution, String gradleUserHomeExpression,
            String javaHomeExpression, List<String> jvmArgumentExpressions, List<String> argumentExpressions,
            boolean showExecutionView, boolean showConsoleView, boolean overrideWorkspaceSettings,
            boolean isOffline, boolean isBuildScansEnabled, List<String> testClasses, List<String> testMethods) {
        super(workingDirExpression, gradleDistribution, gradleUserHomeExpression, javaHomeExpression, jvmArgumentExpressions, argumentExpressions, showExecutionView, showConsoleView, overrideWorkspaceSettings, isOffline, isBuildScansEnabled);
        this.testClasses = testClasses;
        this.testMethods = testMethods;
    }

    public List<String> getTestClasses() {
        return this.testClasses;
    }

    public List<String> getTestMethods() {
        return this.testMethods;
    }

    public boolean hasSameUniqueAttributes(ILaunchConfiguration launchConfiguration) {
        try {
                return this.workingDirExpression.equals(launchConfiguration.getAttribute(WORKING_DIR, ""))
                        && this.testClasses.equals(launchConfiguration.getAttribute(TEST_ClASSES, Collections.<String>emptyList()))
                        && this.testMethods.equals(launchConfiguration.getAttribute(TEST_METHODS, Collections.<String>emptyList()));
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot read Gradle launch configuration %s.", launchConfiguration), e);
        }
    }

    public void apply(ILaunchConfigurationWorkingCopy launchConfiguration) {
        super.apply(launchConfiguration);
        applyTestClasses(this.testClasses, launchConfiguration);
        applyTestMethods(this.testMethods, launchConfiguration);
    }

    public static void applyTestClasses(List<String> testClasses, ILaunchConfigurationWorkingCopy launchConfiguration) {
        launchConfiguration.setAttribute(TEST_ClASSES, testClasses);
    }

    public static void applyTestMethods(List<String> testMethods, ILaunchConfigurationWorkingCopy launchConfiguration) {
        launchConfiguration.setAttribute(TEST_METHODS, testMethods);
    }

    public static GradleTestLaunchConfigurationAttributes from(ILaunchConfiguration launchConfiguration) {
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
        List<String> testClasses = getListAttribute(TEST_ClASSES, launchConfiguration);
        List<String> testMethods = getListAttribute(TEST_METHODS, launchConfiguration);
        return new GradleTestLaunchConfigurationAttributes(workingDirExpression, gradleDistribution, gradleUserHomeExpression, javaHomeExpression, jvmArgumentExpressions, argumentExpressions,
                showExecutionView, showConsoleView, overrideWorkspaceSettings, isOffline, isBuildScansEnabled, testClasses, testMethods);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GradleTestLaunchConfigurationAttributes) {
            GradleTestLaunchConfigurationAttributes other = (GradleTestLaunchConfigurationAttributes) obj;
            return super.equals(obj)
                    && Objects.equal(this.testClasses, other.testClasses)
                    && Objects.equal(this.testMethods, other.testMethods);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), this.testClasses, this.testMethods);
    }

}
