/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.workspace.internal;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntryResolver;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.eclipse.jdt.launching.JavaRuntime;

import org.eclipse.buildship.core.CorePlugin;
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
        Set<String> mainClassSourceSets = collectSourceSetsForMainClass(configuration);
        return resolveRuntimeClasspathEntry(entry, entry.getJavaProject(), mainClassSourceSets);
    }


    private Set<String> collectSourceSetsForMainClass(ILaunchConfiguration configuration) {
        try {
            JavaLaunchDelegate launchDelegate = new JavaLaunchDelegate();
            IJavaProject javaProject = launchDelegate.getJavaProject(configuration);
            if (javaProject == null) {
                return Collections.emptySet();
            }

            String mainTypeName = launchDelegate.getMainTypeName(configuration);
            if (mainTypeName == null) {
                return Collections.emptySet();
            }

            IType mainType = javaProject.findType(mainTypeName);
            if (mainType == null) {
                return Collections.emptySet();
            }

            IJavaElement pkg = mainType.getPackageFragment().getParent();
            if (!(pkg instanceof IPackageFragmentRoot)) {
                return Collections.emptySet();
            }

            for (IClasspathAttribute attribute : ((IPackageFragmentRoot)pkg).getRawClasspathEntry().getExtraAttributes()) {
                if (attribute.getName().equals("gradle_source_sets")) {
                    return Sets.newHashSet(attribute.getValue().split(","));
                }
            }
        } catch (CoreException e) {
            CorePlugin.logger().warn("Cannot collect source set information for dependencies", e);
        }
        return Collections.emptySet();
    }

    @Override
    public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, IJavaProject project) throws CoreException {
        return resolveRuntimeClasspathEntry(entry, project, Collections.<String>emptySet());
    }

    private IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, IJavaProject project,  Set<String> mainClassSourceSets) throws CoreException {
        if (entry.getType() != IRuntimeClasspathEntry.CONTAINER || !entry.getPath().equals(GradleClasspathContainer.CONTAINER_PATH)) {
            return new IRuntimeClasspathEntry[0];
        }
        return collectContainerRuntimeClasspathIfPresent(project, mainClassSourceSets);
    }

    private IRuntimeClasspathEntry[] collectContainerRuntimeClasspathIfPresent(IJavaProject project, Set<String> mainClassSourceSets) throws CoreException {
        List<IRuntimeClasspathEntry> result = Lists.newArrayList();
        collectContainerRuntimeClasspathIfPresent(project, result, false, mainClassSourceSets);
        return result.toArray(new IRuntimeClasspathEntry[result.size()]);
    }

    private void collectContainerRuntimeClasspathIfPresent(IJavaProject project, List<IRuntimeClasspathEntry> result, boolean includeExportedEntriesOnly, Set<String> mainClassSourceSets) throws CoreException {
        IClasspathContainer container = JavaCore.getClasspathContainer(GradleClasspathContainer.CONTAINER_PATH, project);
        if (container != null) {
            collectContainerRuntimeClasspath(container, result, includeExportedEntriesOnly, mainClassSourceSets);
        }
    }

    private void collectContainerRuntimeClasspath(IClasspathContainer container, List<IRuntimeClasspathEntry> result, boolean includeExportedEntriesOnly, Set<String> mainClassSourceSets) throws CoreException {
        for (final IClasspathEntry cpe : container.getClasspathEntries()) {
            if (!includeExportedEntriesOnly || cpe.isExported()) {
                if (cpe.getEntryKind() == IClasspathEntry.CPE_LIBRARY && isLibraryEntryInSourceSet(cpe, mainClassSourceSets)) {
                    result.add(JavaRuntime.newArchiveRuntimeClasspathEntry(cpe.getPath()));
                } else if (cpe.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
                    Optional<IProject> candidate = findAccessibleJavaProject(cpe.getPath().segment(0));
                    if (candidate.isPresent()) {
                        IJavaProject dependencyProject = JavaCore.create(candidate.get());
                        IRuntimeClasspathEntry projectRuntimeEntry = JavaRuntime.newProjectRuntimeClasspathEntry(dependencyProject);
                        Collections.addAll(result, JavaRuntime.resolveRuntimeClasspathEntry(projectRuntimeEntry, dependencyProject));
                        collectContainerRuntimeClasspathIfPresent(dependencyProject, result, true, mainClassSourceSets);
                    }
                }
            }
        }
    }

    private boolean isLibraryEntryInSourceSet(IClasspathEntry entry, Set<String> mainClassSourceSets) {
        if (mainClassSourceSets.isEmpty()) {
            return true;
        }

        Set<String> librarySourceSets = collectLibraryEntrySourceSets(entry);
        if (librarySourceSets.isEmpty()) {
            return true;
        }

        return !Sets.intersection(mainClassSourceSets, librarySourceSets).isEmpty();
    }

    private Set<String> collectLibraryEntrySourceSets(IClasspathEntry entry) {
        for (IClasspathAttribute attribute : entry.getExtraAttributes()) {
            if (attribute.getName().equals("gradle_source_sets")) {
                return Sets.newHashSet(Splitter.on(',').split(attribute.getValue()));
            }
        }
        return Collections.emptySet();
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
