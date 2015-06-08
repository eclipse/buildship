/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.propertytester;

import com.google.common.base.Predicate;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.internal.ui.packageview.PackageFragmentRootContainer;

import org.eclipse.buildship.core.configuration.GradleProjectNature;

/**
 * This class supplies some useful {@link Predicate} instances, which may for instance be used in
 * PropertyTesters.
 */
@SuppressWarnings("restriction")
public final class Predicates {

    private Predicates() {
    }

    public static Predicate<Object> hasGradleNature() {
        return new Predicate<Object>() {

            @Override
            public boolean apply(Object adaptable) {
                IProject project = (IProject) Platform.getAdapterManager().getAdapter(adaptable, IProject.class);
                if (project != null) {
                    return hasGradleNature(project);
                } else if (adaptable instanceof PackageFragmentRootContainer) {
                    project = ((PackageFragmentRootContainer) adaptable).getJavaProject().getProject();
                    return hasGradleNature(project);
                }
                return false;
            }
        };
    }

    private static boolean hasGradleNature(IProject project) {
        try {
            return project.hasNature(GradleProjectNature.ID);
        } catch (CoreException e) {
            // ignore
        }
        return false;
    }

}
