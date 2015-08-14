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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.buildship.core.workspace.GradleClasspathContainer;

/**
 * Updates a Gradle project.
 * <p/>
 * The update happens synchronously and unprotected, meaning it should be called from
 * {@link org.eclipse.core.runtime.jobs.Job} instance guarded by proper rules.
 */
public final class GradleProjectUpdater {

    private GradleProjectUpdater() {
    }

    public static void update(OmniEclipseProject gradleProject, IProject project, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask("Update project " + project.getName(), 3);
        try {
            if (project.isAccessible()) {
                // update linked resources
                LinkedResourcesUpdater.update(project, gradleProject.getLinkedResources(), new SubProgressMonitor(monitor, 1));

                // update Java-specific aspects
                if (project.hasNature(JavaCore.NATURE_ID)) {
                    // adapt to Java project
                    IJavaProject javaProject = JavaCore.create(project);

                    // update the sources
                    SourceFolderUpdater.update(javaProject, gradleProject.getSourceDirectories(), new SubProgressMonitor(monitor, 1));

                    // update project/external dependencies
                    ClasspathContainerUpdater.update(javaProject, gradleProject, new Path(GradleClasspathContainer.CONTAINER_ID), new SubProgressMonitor(monitor, 1));
                }
            }
        } finally {
            monitor.done();
        }
    }

}
