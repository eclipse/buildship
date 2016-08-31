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

import org.gradle.api.specs.Spec;

import com.gradleware.tooling.toolingmodel.OmniGradleProject;
import com.gradleware.tooling.toolingmodel.Path;

/**
 * Contains useful {@code Spec} implementations.
 */
public final class Specs {

    private Specs() {
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
