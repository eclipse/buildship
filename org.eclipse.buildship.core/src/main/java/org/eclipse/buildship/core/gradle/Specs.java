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

package org.eclipse.buildship.core.gradle;

import java.io.File;

import org.gradle.api.specs.Spec;

import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniGradleProject;
import com.gradleware.tooling.toolingmodel.Path;

/**
 * Contains useful {@code Spec} implementations.
 */
public final class Specs {

    private Specs() {
    }

    /**
     * Returns a spec that matches the project if it is a root project.
     *
     * @return the spec, never null
     */
    public static Spec<OmniEclipseProject> isRootProject() {
        return new Spec<OmniEclipseProject>() {

            @Override
            public boolean isSatisfiedBy(OmniEclipseProject project) {
                return project.getParent() == null;
            }

        };
    }

    /**
     * Returns a spec that matches if the the project directory of a {@code OmniEclipseProject}
     * instance matches the given project directory.
     *
     * @param projectDir the project directory to match
     * @return the spec
     */
    public static Spec<OmniEclipseProject> eclipseProjectMatchesProjectDir(final File projectDir) {
        return new Spec<OmniEclipseProject>() {

            @Override
            public boolean isSatisfiedBy(OmniEclipseProject candidate) {
                return candidate.getProjectDirectory().equals(projectDir);
            }
        };
    }


    /**
     * Returns a spec that matches if the the project directory of a {@code OmniEclipseProject}'s root project
     *  matches the given root directory.
     *
     * @param rootDir the root directory to match
     * @return the spec
     */
    public static Spec<OmniEclipseProject> isSubProjectOf(final File rootDir) {
        return new Spec<OmniEclipseProject>() {

            @Override
            public boolean isSatisfiedBy(OmniEclipseProject candidate) {
                return candidate.getRoot().getProjectDirectory().equals(rootDir);
            }
        };
    }

    /**
     * Returns a spec that matches if the the project path of a {@code OmniGradleProject} instance
     * matches the given project path.
     *
     * @param projectPath the project path to match
     * @return the spec
     */
    public static Spec<OmniGradleProject> gradleProjectMatchesProjectPath(final Path projectPath) {
        return new Spec<OmniGradleProject>() {

            @Override
            public boolean isSatisfiedBy(OmniGradleProject candidate) {
                return candidate.getPath().equals(projectPath);
            }
        };
    }

}
