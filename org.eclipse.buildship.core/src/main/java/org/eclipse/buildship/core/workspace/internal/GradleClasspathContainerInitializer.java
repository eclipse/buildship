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

import com.google.common.base.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.util.progress.SynchronizationJob;
import org.eclipse.buildship.core.workspace.GradleBuild;

/**
 * Updates the Gradle classpath container of the given Java workspace project.
 * <p/>
 * This initializer is assigned to the projects via the
 * {@code org.eclipse.jdt.core.classpathContainerInitializer} extension point.
 * <p/>
 *
 * @see GradleClasspathContainerUpdater
 */
public final class GradleClasspathContainerInitializer extends ClasspathContainerInitializer {

    @Override
    public void initialize(IPath containerPath, IJavaProject javaProject) throws JavaModelException {
        loadClasspath(javaProject);
    }

    @Override
    public void requestClasspathContainerUpdate(IPath containerPath, IJavaProject javaProject, IClasspathContainer containerSuggestion) throws JavaModelException {
        loadClasspath(javaProject);
    }

    private void loadClasspath(IJavaProject javaProject) throws JavaModelException {
        IProject project = javaProject.getProject();
        boolean updatedFromStorage = updateFromStorage(javaProject);

        if (!updatedFromStorage) {
            Optional<GradleBuild> gradleBuild = CorePlugin.gradleWorkspaceManager().getGradleBuild(project);
            if (!gradleBuild.isPresent()) {
                GradleClasspathContainerUpdater.clear(javaProject, null);
            } else {
                Job job = new SynchronizationJob(gradleBuild.get()) {

                    @Override
                    protected void handleStatus(IStatus status) {
                        CorePlugin.getInstance().getLog().log(status);
                    }
                };

                job.setUser(false);
                job.schedule();
            }
        }
    }

    private boolean updateFromStorage(IJavaProject javaProject) throws JavaModelException {
        return GradleClasspathContainerUpdater.updateFromStorage(javaProject, null);
    }

    @Override
    public Object getComparisonID(IPath containerPath, IJavaProject project) {
        return project;
    }
}
