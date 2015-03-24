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

package com.gradleware.tooling.eclipse.core.configuration;

import java.io.File;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.Path;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

/**
 * Describes the Gradle-specific configuration of an Eclipse project.
 */
public final class ProjectConfiguration {

    private final FixedRequestAttributes requestAttributes;
    private final Path projectPath;
    private final File projectDir;

    private ProjectConfiguration(FixedRequestAttributes requestAttributes, Path projectPath, File projectDir) {
        this.requestAttributes = Preconditions.checkNotNull(requestAttributes);
        this.projectPath = Preconditions.checkNotNull(projectPath);
        this.projectDir = Preconditions.checkNotNull(projectDir);
    }

    /**
     * Returns the request attributes that are used to connect to the Gradle project.
     *
     * @return the request attributes used to connect to the Gradle project, never null
     */
    public FixedRequestAttributes getRequestAttributes() {
        return this.requestAttributes;
    }

    /**
     * Returns the path of the Gradle project.
     *
     * @return the path of the Gradle project, never null
     */
    public Path getProjectPath() {
        return this.projectPath;
    }

    /**
     * Returns the location of the Gradle project.
     *
     * @return the location of the Gradle project, never null
     */
    public File getProjectDir() {
        return this.projectDir;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        ProjectConfiguration that = (ProjectConfiguration) other;
        return Objects.equal(this.requestAttributes, that.requestAttributes) && Objects.equal(this.projectDir, that.projectDir)
                && Objects.equal(this.projectPath, that.projectPath);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.requestAttributes, this.projectDir, this.projectPath);
    }

    /**
     * Creates a new instance.
     *
     * @param requestAttributes the connection aspects of the configuration
     * @param project the project aspects of the configuration
     * @return the new instance
     */
    public static ProjectConfiguration from(FixedRequestAttributes requestAttributes, OmniEclipseProject project) {
        return from(requestAttributes, project.getPath(), project.getProjectDirectory());
    }

    /**
     * Creates a new instance.
     *
     * @param requestAttributes the connection aspects of the configuration
     * @param projectPath the path of the Gradle project
     * @param projectDir the location of the Gradle project
     * @return the new instance
     */
    public static ProjectConfiguration from(FixedRequestAttributes requestAttributes, Path projectPath, File projectDir) {
        return new ProjectConfiguration(requestAttributes, projectPath, projectDir);
    }

}
