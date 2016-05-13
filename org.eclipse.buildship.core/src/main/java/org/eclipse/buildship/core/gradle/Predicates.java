/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.gradle;

import java.io.File;

import org.gradle.tooling.model.eclipse.EclipseProjectIdentifier;

import com.google.common.base.Predicate;

import com.gradleware.tooling.toolingmodel.OmniEclipseProject;

/**
 * Supplies some useful {@link Predicate} instances.
 */
public final class Predicates {

    private Predicates() {
    }

    /**
     * Returns a predicate that matches if the the project directory of a {@code OmniEclipseProject}
     * instance matches the given project directory.
     *
     * @param projectDir the project directory to match
     * @return the predicate
     */
    public static Predicate<OmniEclipseProject> eclipseProjectMatchesProjectDir(final File projectDir) {
        return new Predicate<OmniEclipseProject>() {

            @Override
            public boolean apply(OmniEclipseProject candidate) {
                return candidate.getProjectDirectory().equals(projectDir);
            }
        };
    }

    /**
     * Returns a predicate that matches if the the project identifier of a {@code OmniEclipseProject}
     * instance matches the given identifier
     *
     * @param projectDir the project directory to match
     * @return the predicate
     */
    public static Predicate<OmniEclipseProject> eclipseProjectMatchesIdentifier(final EclipseProjectIdentifier id) {
        return new Predicate<OmniEclipseProject>() {

            @Override
            public boolean apply(OmniEclipseProject candidate) {
                return candidate.getIdentifier().equals(id);
            }
        };
    }

    /**
     * Returns a predicate that matches if the the project directory of a {@code OmniEclipseProject}
     * 's root project matches the given root directory.
     *
     * @param rootDir the root directory to match
     * @return the predicate
     */
    public static Predicate<OmniEclipseProject> isSubProjectOf(final File rootDir) {
        return new Predicate<OmniEclipseProject>() {

            @Override
            public boolean apply(OmniEclipseProject candidate) {
                return candidate.getRoot().getProjectDirectory().equals(rootDir);
            }
        };
    }
}
