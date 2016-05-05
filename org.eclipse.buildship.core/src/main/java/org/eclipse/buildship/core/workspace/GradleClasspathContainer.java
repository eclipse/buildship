/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 473348
 */

package org.eclipse.buildship.core.workspace;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.workspace.internal.DefaultGradleClasspathContainer;

/**
 * API to define classpath container for Buildship project and external dependencies.
 */
public abstract class GradleClasspathContainer implements IClasspathContainer {

    /**
     * Id and string representation of the path where all Gradle projects store their external
     * dependencies. This path is added during the project import and the
     * {@code org.eclipse.jdt.core.classpathContainerInitializer} extension populates it with the
     * actual external (source and binary) jars.
     */
    public static final String CONTAINER_ID = "org.eclipse.buildship.core.gradleclasspathcontainer";

    /**
     * Creates a new classpath container instance.
     *
     * @param classpathEntries the list of dependencies the container holds
     * @return the classpath container references
     */
    public static IClasspathContainer newInstance(List<IClasspathEntry> classpathEntries) {
        Path containerPath = new Path(CONTAINER_ID);
        return new DefaultGradleClasspathContainer(containerPath, classpathEntries);
    }

    /**
     * Adds the Gradle classpath container to the given project if it is not yet defined.
     *
     * @param javaProject the project to add the container to
     * @param progress the monitor to report progress on
     * @throws JavaModelException if the container cannot be added
     */
    public static void addIfNotPresent(IJavaProject javaProject, IProgressMonitor progress) throws JavaModelException {
        IClasspathEntry[] oldClasspath = javaProject.getRawClasspath();
        for (IClasspathEntry entry : oldClasspath) {
            if (entry.getPath().equals(new Path(CONTAINER_ID))) {
                return;
            }
        }

        IClasspathEntry[] newClasspath = new IClasspathEntry[oldClasspath.length + 1];
        System.arraycopy(oldClasspath, 0, newClasspath, 0, oldClasspath.length);
        newClasspath[newClasspath.length - 1] = newClasspathEntry();
        javaProject.setRawClasspath(newClasspath, progress);
    }

    /**
     * Updates the Gradle classpath container on the given project with the given attributes. Does
     * nothing if the classpath container is not present.
     *
     * @param javaProject the project to update the container in
     * @param extraAttributes the classpath attributes to set on the container
     * @param progress the monitor to report progress on
     * @throws JavaModelException if the container cannot be updated
     */
    public static void update(IJavaProject javaProject, IClasspathAttribute[] extraAttributes, SubMonitor progress) throws JavaModelException {
        IClasspathEntry[] oldClasspath = javaProject.getRawClasspath();
        IClasspathEntry[] newClasspath = new IClasspathEntry[oldClasspath.length];
        for (int i = 0; i < oldClasspath.length; i++) {
            IClasspathEntry entry = oldClasspath[i];
            if (entry.getPath().equals(new Path(GradleClasspathContainer.CONTAINER_ID))) {
                IClasspathEntry newContainer = GradleClasspathContainer.newClasspathEntry(extraAttributes);
                newClasspath[i] = newContainer;
            } else {
                newClasspath[i] = entry;
            }
        }
        if (!Arrays.equals(oldClasspath, newClasspath)) {
            javaProject.setRawClasspath(newClasspath, progress);
        }
    }

    private static IClasspathEntry newClasspathEntry(IClasspathAttribute... extraAttributes) throws JavaModelException {
        Path containerPath = new Path(CONTAINER_ID);
        return JavaCore.newContainerEntry(containerPath, null, extraAttributes, false);
    }

    /**
     * Updates the content of the Gradle classpath container asynchronously on the target project.
     * <p/>
     * This method finds the Gradle classpath container on the project and requests the content
     * update.
     *
     * @throws GradlePluginsRuntimeException if the classpath container update request fails
     * @param project the target project
     */
    public static void requestUpdateOf(IJavaProject project) {
        ClasspathContainerInitializer initializer = JavaCore.getClasspathContainerInitializer(CONTAINER_ID);
        try {
            IPath containerPath = new Path(CONTAINER_ID);
            initializer.requestClasspathContainerUpdate(containerPath, project, null);
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }

}
