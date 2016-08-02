/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.workspace.internal;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntryResolver;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;

import org.eclipse.buildship.core.workspace.GradleClasspathContainer;

/**
 * {@link IRuntimeClasspathEntryResolver} implementation to resolve Gradle classpath container
 * entries.
 *
 * @author Donat Csikos
 */
public class GradleClasspathContainerRuntimeClasspathEntryResolver implements IRuntimeClasspathEntryResolver {

    @Override
    public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, ILaunchConfiguration configuration) throws CoreException {
        if (entry == null || entry.getJavaProject() == null) {
            return new IRuntimeClasspathEntry[0];
        }
        return resolveRuntimeClasspathEntry(entry, entry.getJavaProject());
    }

    @Override
    public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, IJavaProject project) throws CoreException {
        if (entry.getType() != IRuntimeClasspathEntry.CONTAINER || !entry.getPath().equals(GradleClasspathContainer.CONTAINER_PATH)) {
            return new IRuntimeClasspathEntry[0];
        }

        IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), project);

        List<IRuntimeClasspathEntry> result = Lists.newArrayList();
        for (final IClasspathEntry cpe : container.getClasspathEntries()) {
            if (cpe.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
                result.add(JavaRuntime.newArchiveRuntimeClasspathEntry(cpe.getPath()));
            } else if (cpe.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
                Optional<IProject> candidate = findAccessibleJavaProject(cpe.getPath().segment(0));
                if (candidate.isPresent()) {
                    IJavaProject dependencyProject = JavaCore.create(candidate.get());
                    result.add(JavaRuntime.newProjectRuntimeClasspathEntry(dependencyProject));
                }
            }
        }

        return result.toArray(new IRuntimeClasspathEntry[result.size()]);
    }

    private static Optional<IProject> findAccessibleJavaProject(String name) {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
        if (project != null && project.isAccessible() && hasJavaNature(project)) {
            return Optional.of(project);
        } else {
            return Optional.absent();
        }
    }

    private static boolean hasJavaNature(IProject project) {
        try {
            return project.hasNature(JavaCore.NATURE_ID);
        } catch (CoreException e) {
            return false;
        }
    }

    @Override
    public IVMInstall resolveVMInstall(IClasspathEntry entry) throws CoreException {
        return null;
    }

}
