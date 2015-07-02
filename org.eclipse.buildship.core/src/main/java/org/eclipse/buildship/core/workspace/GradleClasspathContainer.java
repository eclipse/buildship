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

package org.eclipse.buildship.core.workspace;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
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
        return new DefaultGradleClasspathContainer(classpathEntries, new Path(GradleClasspathContainer.CONTAINER_ID));
    }

    /**
     * Creates a {@link IClasspathEntry} instance describing a Gradle classpath container. If the
     * entry is assigned to a project classpath, it triggers the lazy classpath initialization via
     * the {@code org.eclipse.jdt.core.classpathContainerInitializer} extension.
     *
     * @return the classpath entry to assign to a project's classpath
     * @throws JavaModelException
     */
    public static IClasspathEntry newClasspathEntry() throws JavaModelException {
        // http://www-01.ibm.com/support/knowledgecenter/SSZND2_6.0.0/org.eclipse.jdt.doc.isv/guide/jdt_api_classpath.htm?cp=SSZND2_6.0.0%2F3-1-1-0-0-2
        Path containerPath = new Path(CONTAINER_ID);
        return JavaCore.newContainerEntry(containerPath, true);
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
    public static void requestUpdateOn(IJavaProject project) {
        ClasspathContainerInitializer initializer = JavaCore.getClasspathContainerInitializer(CONTAINER_ID);
        org.eclipse.core.runtime.Path containerPath = new org.eclipse.core.runtime.Path(CONTAINER_ID);
        try {
            initializer.requestClasspathContainerUpdate(containerPath, project, null);
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }
}