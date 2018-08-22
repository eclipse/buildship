/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.workspace.impl;

import java.util.Collections;
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

import org.eclipse.buildship.core.internal.launch.impl.GradleClasspathProvider;
import org.eclipse.buildship.core.internal.launch.impl.LaunchConfigurationScope;
import org.eclipse.buildship.core.internal.workspace.GradleClasspathContainer;

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
        LaunchConfigurationScope configurationScopes = LaunchConfigurationScope.from(configuration);
        return resolveRuntimeClasspathEntry(entry, entry.getJavaProject(), configurationScopes);
    }

    @Override
    public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, IJavaProject project) throws CoreException {
        return resolveRuntimeClasspathEntry(entry, project, LaunchConfigurationScope.INCLUDE_ALL);
    }

    private IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, IJavaProject project, LaunchConfigurationScope configurationScopes) throws CoreException {
        if (entry.getType() != IRuntimeClasspathEntry.CONTAINER || !entry.getPath().equals(GradleClasspathContainer.CONTAINER_PATH)) {
            return new IRuntimeClasspathEntry[0];
        }
        return collectContainerRuntimeClasspathIfPresent(project, configurationScopes);
    }

    private IRuntimeClasspathEntry[] collectContainerRuntimeClasspathIfPresent(IJavaProject project, LaunchConfigurationScope configurationScopes) throws CoreException {
        List<IRuntimeClasspathEntry> result = Lists.newArrayList();
        collectContainerRuntimeClasspathIfPresent(project, result, false, configurationScopes);
        return result.toArray(new IRuntimeClasspathEntry[result.size()]);
    }

    private void collectContainerRuntimeClasspathIfPresent(IJavaProject project, List<IRuntimeClasspathEntry> result, boolean includeExportedEntriesOnly,
            LaunchConfigurationScope configurationScopes) throws CoreException {
        IClasspathContainer container = JavaCore.getClasspathContainer(GradleClasspathContainer.CONTAINER_PATH, project);
        if (container != null) {
            collectContainerRuntimeClasspath(container, result, includeExportedEntriesOnly, configurationScopes);
        }
    }

    private void collectContainerRuntimeClasspath(IClasspathContainer container, List<IRuntimeClasspathEntry> result, boolean includeExportedEntriesOnly,
            LaunchConfigurationScope configurationScopes) throws CoreException {
        for (final IClasspathEntry cpe : container.getClasspathEntries()) {
            if (!includeExportedEntriesOnly || cpe.isExported()) {
                if (cpe.getEntryKind() == IClasspathEntry.CPE_LIBRARY && configurationScopes.isEntryIncluded(cpe)) {
                    result.add(JavaRuntime.newArchiveRuntimeClasspathEntry(cpe.getPath()));
                } else if (cpe.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
                    Optional<IProject> candidate = findAccessibleJavaProject(cpe.getPath().segment(0));
                    if (candidate.isPresent()) {
                        IJavaProject dependencyProject = JavaCore.create(candidate.get());
                        IRuntimeClasspathEntry projectRuntimeEntry = JavaRuntime.newProjectRuntimeClasspathEntry(dependencyProject);
                        // add the project entry itself so that the source lookup can find the classes
                        // see https://github.com/eclipse/buildship/issues/383
                        result.add(projectRuntimeEntry);
                        Collections.addAll(result, GradleClasspathProvider.resolveOutputLocations(projectRuntimeEntry, dependencyProject, configurationScopes));
                        collectContainerRuntimeClasspathIfPresent(dependencyProject, result, true, configurationScopes);
                    }
                }
            }
        }
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
