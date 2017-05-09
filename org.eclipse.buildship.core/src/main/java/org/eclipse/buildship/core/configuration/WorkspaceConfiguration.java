/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.buildship.core.configuration;

import java.io.File;

import org.gradle.api.Nullable;

import com.google.common.base.Objects;

/**
 * Encapsulates settings that are the same for all Gradle projects in the workspace.
 *
 * @author Stefan Oehme
 *
 */
public final class WorkspaceConfiguration {

    private final File gradleUserHome;
    private final boolean gradleIsOffline;
    private final boolean buildScansEnabled;

    public WorkspaceConfiguration(File gradleUserHome, boolean gradleIsOffline, boolean buildScansEnabled) {
        this.gradleUserHome = gradleUserHome;
        this.gradleIsOffline = gradleIsOffline;
        this.buildScansEnabled = buildScansEnabled;
    }

    @Nullable
    public File getGradleUserHome() {
        return this.gradleUserHome;
    }

    public boolean isOffline() {
        return this.gradleIsOffline;
    }

    public boolean isBuildScansEnabled() {
        return this.buildScansEnabled;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WorkspaceConfiguration) {
            WorkspaceConfiguration other = (WorkspaceConfiguration) obj;
            return Objects.equal(this.gradleUserHome, other.gradleUserHome)
                    && Objects.equal(this.gradleIsOffline, other.gradleIsOffline)
                    && Objects.equal(this.buildScansEnabled, other.buildScansEnabled);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.gradleUserHome, this.gradleIsOffline, this.buildScansEnabled);
    }
}