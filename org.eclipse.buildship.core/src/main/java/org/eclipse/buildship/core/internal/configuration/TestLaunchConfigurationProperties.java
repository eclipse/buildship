/*
 * Copyright (c) 2019 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

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
final class TestLaunchConfigurationProperties extends BaseLaunchConfigurationProperties {

    private final List<String> tests;

    public TestLaunchConfigurationProperties(GradleDistribution gradleDistribution, File gradleUserHome, File javaHome, List<String> jvmArguments, List<String> arguments, boolean showConsoleView, boolean showExecutionsView, boolean overrideBuildSettings, boolean buildScansEnabled, boolean offlineMode, List<String> tests) {
        super(gradleDistribution, gradleUserHome, javaHome, jvmArguments, arguments, showConsoleView, showExecutionsView, overrideBuildSettings, buildScansEnabled, offlineMode);
        this.tests = tests;
    }

    public List<String> getTests() {
        return this.tests;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TestLaunchConfigurationProperties) {
            TestLaunchConfigurationProperties other = (TestLaunchConfigurationProperties) obj;
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
