/*
 * Copyright (c) 2017 the original author or authors.
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
 * Properties backing a {@code RunConfiguration} instance.
 *
 * @author Donat Csikos
 */
final class LaunchConfigurationProperties extends BaseLaunchConfigurationProperties {

    private final List<String> tasks;

    public LaunchConfigurationProperties(List<String> tasks, GradleDistribution gradleDistribution, File gradleUserHome, File javaHome, List<String> jvmArguments, List<String> arguments, boolean showConsoleView, boolean showExecutionsView, boolean overrideBuildSettings, boolean buildScansEnabled, boolean offlineMode) {
        super(gradleDistribution, gradleUserHome, javaHome, jvmArguments, arguments, showConsoleView, showExecutionsView, overrideBuildSettings, buildScansEnabled, offlineMode);
        this.tasks = tasks;
    }

    public List<String> getTasks() {
        return this.tasks;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LaunchConfigurationProperties) {
            LaunchConfigurationProperties other = (LaunchConfigurationProperties) obj;
            return super.equals(obj) && Objects.equal(this.tasks, other.tasks);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), this.tasks);
    }
}