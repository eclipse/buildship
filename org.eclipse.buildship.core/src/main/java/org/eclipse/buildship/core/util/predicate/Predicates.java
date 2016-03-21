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

package org.eclipse.buildship.core.util.predicate;

import com.google.common.base.Predicate;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.configuration.GradleProjectNature;

/**
 * Supplies some useful {@link Predicate} instances.
 */
public final class Predicates {

    private Predicates() {
    }

    public static Predicate<IProject> accessibleGradleProject() {
        return new Predicate<IProject>() {

            @Override
            public boolean apply(IProject project) {
                return GradleProjectNature.INSTANCE.isPresentOn(project);
            }
        };
    }

}
