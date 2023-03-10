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

import org.eclipse.buildship.core.GradleDistribution;;

/**
 * Encapsulates settings that are the same for all Gradle projects in the workspace.
 *
 * @author Stefan Oehme
 *
 */
public final class WorkspaceConfiguration {

    private final GradleDistribution gradleDistribution;
    private final File gradleUserHome;
    private final File javaHome;
    private final boolean gradleIsOffline;
    private final boolean buildScansEnabled;
    private final boolean autoSync;
    private final List<String> arguments;
    private final List<String> jvmArguments;
    private final boolean showConsoleView;
    private final boolean showExecutionsView;
    private final boolean experimentalModuleSupportEnabled;

    public WorkspaceConfiguration(GradleDistribution gradleDistribution, File gradleUserHome,
                                  File javaHome, boolean gradleIsOffline, boolean buildScansEnabled,
                                  boolean autoSync, List<String> arguments,
                                  List<String> jvmArguments, boolean showConsoleView,
                                  boolean showExecutionsView, boolean experimentalModuleSupportEnabled) {
        this.gradleDistribution = gradleDistribution;
        this.gradleUserHome = gradleUserHome;
        this.javaHome = javaHome;
        this.gradleIsOffline = gradleIsOffline;
        this.buildScansEnabled = buildScansEnabled;
        this.autoSync = autoSync;
        this.arguments = arguments;
        this.jvmArguments = jvmArguments;
        this.showConsoleView = showConsoleView;
        this.showExecutionsView = showExecutionsView;
        this.experimentalModuleSupportEnabled = experimentalModuleSupportEnabled;
    }

    public GradleDistribution getGradleDistribution() {
        return this.gradleDistribution;
    }

    public File getGradleUserHome() {
        return this.gradleUserHome;
    }

    public File getJavaHome() {
        return this.javaHome;
    }

    public boolean isOffline() {
        return this.gradleIsOffline;
    }

    public boolean isBuildScansEnabled() {
        return this.buildScansEnabled;
    }

    public boolean isAutoSync() {
        return this.autoSync;
    }

    public List<String> getArguments() {
        return this.arguments;
    }

    public List<String> getJvmArguments() {
        return this.jvmArguments;
    }

    public boolean isShowConsoleView() {
        return this.showConsoleView;
    }


    public boolean isShowExecutionsView() {
        return this.showExecutionsView;
    }


    public boolean isExperimentalModuleSupportEnabled() {
        return this.experimentalModuleSupportEnabled;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WorkspaceConfiguration) {
            WorkspaceConfiguration other = (WorkspaceConfiguration) obj;
            return Objects.equal(this.gradleDistribution, other.gradleDistribution)
                    && Objects.equal(this.gradleUserHome, other.gradleUserHome)
                    && Objects.equal(this.javaHome, other.javaHome)
                    && Objects.equal(this.gradleIsOffline, other.gradleIsOffline)
                    && Objects.equal(this.buildScansEnabled, other.buildScansEnabled)
                    && Objects.equal(this.autoSync, other.autoSync)
                    && Objects.equal(this.arguments, other.arguments)
                    && Objects.equal(this.jvmArguments, other.jvmArguments)
                    && Objects.equal(this.showConsoleView, other.showConsoleView)
                    && Objects.equal(this.showExecutionsView, other.showExecutionsView)
                    && Objects.equal(this.experimentalModuleSupportEnabled, other.experimentalModuleSupportEnabled);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.gradleDistribution, this.gradleUserHome, this.javaHome, this.gradleIsOffline, this.buildScansEnabled, this.autoSync, this.arguments, this.jvmArguments, this.showConsoleView, this.showExecutionsView, this.experimentalModuleSupportEnabled);
    }
}
