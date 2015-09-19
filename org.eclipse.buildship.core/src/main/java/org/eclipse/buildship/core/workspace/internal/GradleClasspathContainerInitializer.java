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

import org.eclipse.buildship.core.workspace.SynchronizeJavaWorkspaceProjectJob;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Initializes the classpath of each Eclipse workspace project that has a Gradle nature with the
 * linked resources/sources/project and external dependencies of the underlying Gradle project.
 * <p/>
 * When this initializer is invoked, it looks up the {@link com.gradleware.tooling.toolingmodel.OmniEclipseProject} for the given
 * Eclipse workspace project, applies all the found linked resources and the sources, reads the
 * project dependencies and external dependencies and adds the dependencies to the
 * {@link org.eclipse.buildship.core.workspace.GradleClasspathContainer#CONTAINER_ID} classpath
 * container.
 * <p/>
 * This initializer is assigned to the projects via the
 * {@code org.eclipse.jdt.core.classpathContainerInitializer} extension point.
 * <p/>
 * The initialization is scheduled as a job, to not block the IDE upon startup.
 */
public final class GradleClasspathContainerInitializer extends ClasspathContainerInitializer {

    @Override
    public void initialize(IPath containerPath, IJavaProject javaProject) {
        scheduleJavaWorkspaceProjectSynchronization(javaProject);
    }

    @Override
    public void requestClasspathContainerUpdate(IPath containerPath, IJavaProject javaProject, IClasspathContainer containerSuggestion) {
        scheduleJavaWorkspaceProjectSynchronization(javaProject);
    }

    private void scheduleJavaWorkspaceProjectSynchronization(IJavaProject project) {
        SynchronizeJavaWorkspaceProjectJob job = new SynchronizeJavaWorkspaceProjectJob(project);
        job.schedule();
    }

}
