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

package org.eclipse.buildship.core.workspace.internal;

import com.gradleware.tooling.toolingmodel.OmniEclipseProject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.buildship.core.workspace.GradleClasspathContainer;

/**
 * Updates a Gradle project.
 */
public final class GradleProjectUpdater {

    private GradleProjectUpdater() {
    }

    public static void update(OmniEclipseProject modelProject, IProject workspaceProject) throws CoreException {
        // TODO (donat) the LinkedResourcesUpdater is also called from the classpath container initializer
        // since that is automatically called on startup we should find a smarter way to unify the initializer with
        // the project refresh functionality
        if (workspaceProject.isAccessible()) {
            LinkedResourcesUpdater.update(workspaceProject, modelProject.getLinkedResources(), new NullProgressMonitor());
            if (workspaceProject.hasNature(JavaCore.NATURE_ID)) {
                // the classpath container also updates the linked resources
                IJavaProject javaProject = JavaCore.create(workspaceProject);
                GradleClasspathContainer.requestUpdateOf(javaProject);
            }
        }
    }

}
