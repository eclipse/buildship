/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
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
 * Properties backing a {@code RunConfiguration} instance.
 *
 * @author Donat Csikos
 */
final class RunConfigurationProperties extends BaseRunConfigurationProperties {

    private final List<String> tasks;

    public RunConfigurationProperties(List<String> tasks, GradleDistribution gradleDistribution, File gradleUserHome, File javaHome, List<String> jvmArguments, List<String> arguments, boolean showConsoleView, boolean showExecutionsView, boolean overrideBuildSettings, boolean buildScansEnabled, boolean offlineMode) {
        super(gradleDistribution, gradleUserHome, javaHome, jvmArguments, arguments, showConsoleView, showExecutionsView, overrideBuildSettings, buildScansEnabled, offlineMode);
        this.tasks = tasks;
    }

    public List<String> getTasks() {
        return this.tasks;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RunConfigurationProperties) {
            RunConfigurationProperties other = (RunConfigurationProperties) obj;
            return super.equals(obj) && Objects.equal(this.tasks, other.tasks);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), this.tasks);
    }
}
