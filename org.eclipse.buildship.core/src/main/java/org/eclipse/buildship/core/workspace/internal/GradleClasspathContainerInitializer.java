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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.configuration.GradleProjectNature;

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
    public void initialize(IPath containerPath, IJavaProject javaProject) {
        loadClasspath(javaProject);
    }

    @Override
    public void requestClasspathContainerUpdate(IPath containerPath, IJavaProject javaProject, IClasspathContainer containerSuggestion) {
        loadClasspath(javaProject);
    }

    private void loadClasspath(IJavaProject javaProject) {
        IProject project = javaProject.getProject();
        if (!GradleProjectNature.isPresentOn(project)) {
            return;
        }

        boolean updatedFromStorage;
        try {
            updatedFromStorage = GradleClasspathContainerUpdater.updateFromStorage(javaProject, null);
        } catch (JavaModelException e) {
            throw new GradlePluginsRuntimeException("Could not initialize Gradle classpath container.", e);
        }

        if (!updatedFromStorage) {
            resynchronize(javaProject);
        }
    }

    private void resynchronize(IJavaProject project) {
        CorePlugin.gradleWorkspaceManager().getGradleBuild(project.getProject()).get().synchronize();
    }

}
