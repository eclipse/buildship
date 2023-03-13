/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace;

import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.GradleCore;
import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.DefaultGradleBuild;
import org.eclipse.buildship.core.internal.operation.ToolingApiJobResultHandler;
import org.eclipse.buildship.core.internal.operation.ToolingApiStatus;

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

            Optional<GradleBuild> gradleBuild = GradleCore.getWorkspace().getBuild(project);
            if (!gradleBuild.isPresent()) {
                GradleClasspathContainerUpdater.clear(javaProject, null);
            } else if (!((DefaultGradleBuild)gradleBuild.get()).isSynchronizing()) {
                SynchronizationJob job = new SynchronizationJob(gradleBuild.get());
                job.setResultHandler(new ResultHander());
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

    /**
     * Custom result handler that only logs the failure.
     */
    private static final class ResultHander implements ToolingApiJobResultHandler<Void> {

        @Override
        public void onSuccess(Void result) {
        }

        @Override
        public void onFailure(ToolingApiStatus status) {
            CorePlugin.getInstance().getLog().log(status);
        }
    }
}
