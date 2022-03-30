/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.configuration;

import java.io.File;
import java.util.List;

import com.google.common.base.Objects;

import org.eclipse.buildship.core.GradleDistribution;

/**
 * Properties backing a {@code TestLaunchConfiguration} instance.
 *
 * @author Donat Csikos
 */
final class TestRunConfigurationProperties extends BaseRunConfigurationProperties {

    private final List<Test> tests;
    private final String testTask;
    private final List<String> testClasses;
    private final List<String> testMethods;
    private final List<String> testPackages;
    private final List<String> testPatterns;

    public TestRunConfigurationProperties(
            GradleDistribution gradleDistribution,
            File gradleUserHome,
            File javaHome,
            List<String> jvmArguments,
            List<String> arguments,
            boolean showConsoleView,
            boolean showExecutionsView,
            boolean overrideBuildSettings,
            boolean buildScansEnabled,
            boolean offlineMode,
            List<Test> tests,
            String testTask,
            List<String> testClasses,
            List<String> testMethods,
            List<String> testPackages,
            List<String> testPatterns) {
        super(gradleDistribution, gradleUserHome, javaHome, jvmArguments, arguments, showConsoleView, showExecutionsView, overrideBuildSettings, buildScansEnabled, offlineMode);
        this.tests = tests;
        this.testTask = testTask;
        this.testClasses = testClasses;
        this.testMethods = testMethods;
        this.testPackages = testPackages;
        this.testPatterns = testPatterns;
    }

    public List<Test> getTests() {
        return this.tests;
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TestRunConfigurationProperties) {
            TestRunConfigurationProperties other = (TestRunConfigurationProperties) obj;
            return super.equals(obj)
                    && Objects.equal(this.tests, other.tests)
                    && Objects.equal(this.testTask, other.testTask)
                    && Objects.equal(this.testClasses, other.testClasses)
                    && Objects.equal(this.testMethods, other.testMethods)
                    && Objects.equal(this.testPackages, other.testPackages)
                    && Objects.equal(this.testPatterns, other.testPatterns);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), this.tests, this.testTask, this.testClasses, this.testMethods, this.testPackages, this.testPatterns);
    }
}
