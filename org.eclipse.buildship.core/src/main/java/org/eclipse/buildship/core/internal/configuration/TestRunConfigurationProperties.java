/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
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

    public TestRunConfigurationProperties(GradleDistribution gradleDistribution, File gradleUserHome, File javaHome, List<String> jvmArguments, List<String> arguments, boolean showConsoleView, boolean showExecutionsView, boolean overrideBuildSettings, boolean buildScansEnabled, boolean offlineMode, List<Test> tests) {
        super(gradleDistribution, gradleUserHome, javaHome, jvmArguments, arguments, showConsoleView, showExecutionsView, overrideBuildSettings, buildScansEnabled, offlineMode);
        this.tests = tests;
    }

    public List<Test> getTests() {
        return this.tests;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TestRunConfigurationProperties) {
            TestRunConfigurationProperties other = (TestRunConfigurationProperties) obj;
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
