/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.workspace.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.gradleware.tooling.toolingmodel.OmniEclipseClasspathContainer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;

import org.eclipse.buildship.core.util.classpath.ClasspathUtils;
import org.eclipse.buildship.core.workspace.GradleClasspathContainer;

/**
 * Updates the classpath containers of the target project.
 * <p/>
 * The update is triggered via {@link #update(IJavaProject, Optional, IProgressMonitor)}. The method
 * executes synchronously and unprotected, without thread synchronization or job scheduling.
 * <p/>
 * If an absent value was passed for the containers, then it will be treated as an empty list.
 * <p/>
 * The Gradle classpath container is always configured on the project.
 *
 * @author Donat Csikos
 */
final class ClasspathContainerUpdater {

    private static final String PROJECT_PROPERTY_KEY_GRADLE_CONTAINERS = "containers";

    private final IJavaProject project;
    private final List<OmniEclipseClasspathContainer> containers;
    private final Set<String> containerPaths;
    private final boolean containersContainJre;

    private ClasspathContainerUpdater(IJavaProject project, List<OmniEclipseClasspathContainer> containers) {
        this.project = project;
        this.containers = ImmutableList.copyOf(containers);
        this.containerPaths = new HashSet<String>();

        boolean containersContainJre = false;
        String defaultJrePath = JavaRuntime.newDefaultJREContainerPath().toPortableString();

        for (OmniEclipseClasspathContainer container : this.containers) {
            String containerPath = container.getPath();
            this.containerPaths.add(containerPath);
            if(containerPath.startsWith(defaultJrePath)) {
                containersContainJre = true;
            }
        }

        this.containersContainJre = containersContainJre;
    }

    private void updateContainers(IProgressMonitor monitor) throws CoreException {
        SubMonitor progress = SubMonitor.convert(monitor);
        progress.setWorkRemaining(3);

        LinkedHashSet<IPath> toRemove = collectContainersToRemove(progress.newChild(1));
        LinkedHashMap<IPath, IClasspathEntry> toAdd = collectContainersToAdd(progress.newChild(1));

        updateProjectClasspath(toRemove, toAdd, progress.newChild(1));
    }

    private LinkedHashSet<IPath> collectContainersToRemove(SubMonitor progress) throws JavaModelException {
        StringSetProjectProperty previousPaths = StringSetProjectProperty.from(this.project.getProject(), PROJECT_PROPERTY_KEY_GRADLE_CONTAINERS);
        LinkedHashSet<IPath> result = Sets.newLinkedHashSet();
        for (String previousPath : previousPaths.get()) {
            if (!this.containerPaths.contains(previousPath)) {
                result.add(new Path(previousPath));
            }
        }

        // if the model contains a JRE entry then remove user-defined JREs from the classpath
        if (this.containersContainJre) {
            IPath defaultJrePath = JavaRuntime.newDefaultJREContainerPath();
            for (IClasspathEntry entry : this.project.getRawClasspath()) {
                if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER && defaultJrePath.isPrefixOf(entry.getPath())) {
                    result.add(entry.getPath());
                }
            }
        }

        return result;
    }

    private LinkedHashMap<IPath, IClasspathEntry> collectContainersToAdd(SubMonitor progress) {
        LinkedHashMap<IPath, IClasspathEntry> result = Maps.newLinkedHashMap();
        for (OmniEclipseClasspathContainer container : this.containers) {
            result.put(new Path(container.getPath()), createContainerEntry(container));
        }

        if (!result.keySet().contains(GradleClasspathContainer.CONTAINER_PATH)) {
            result.put(GradleClasspathContainer.CONTAINER_PATH, JavaCore.newContainerEntry(GradleClasspathContainer.CONTAINER_PATH));
        }

        return result;
    }

    private void updateProjectClasspath(LinkedHashSet<IPath> containersToRemove, LinkedHashMap<IPath, IClasspathEntry> containersToAdd,
            SubMonitor progress) throws JavaModelException {
        StringSetProjectProperty containerPaths = StringSetProjectProperty.from(this.project.getProject(), PROJECT_PROPERTY_KEY_GRADLE_CONTAINERS);
        containerPaths.set(this.containerPaths);

        updateClasspathContainerEntries(containersToRemove, containersToAdd, progress);
    }

    private void updateClasspathContainerEntries(Set<IPath> containersToRemove, Map<IPath, IClasspathEntry> containersToAdd, SubMonitor progress) throws JavaModelException {
        List<IClasspathEntry> classpath = Lists.newArrayList(this.project.getRawClasspath());

        ListIterator<IClasspathEntry> iterator = classpath.listIterator();
        while (iterator.hasNext()) {
            IClasspathEntry entry = iterator.next();
            if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
                IPath entryPath = entry.getPath();
                if (containersToRemove.contains(entryPath)) {
                    containersToRemove.remove(entryPath);
                    iterator.remove();
                } else if (containersToAdd.containsKey(entryPath)) {
                    IClasspathEntry newEntry = containersToAdd.remove(entryPath);
                    iterator.set(newEntry);
                }
            }
        }
        classpath.addAll(containersToAdd.values());

        this.project.setRawClasspath(classpath.toArray(new IClasspathEntry[classpath.size()]), progress);
    }

    private static IClasspathEntry createContainerEntry(OmniEclipseClasspathContainer container) {
        IPath containerPath = new Path(container.getPath());
        boolean isExported = container.isExported();
        IAccessRule[] accessRules = ClasspathUtils.createAccessRules(container);
        IClasspathAttribute[] attributes = ClasspathUtils.createClasspathAttributes(container);
        return JavaCore.newContainerEntry(containerPath, accessRules, attributes, isExported);
    }

    public static void update(IJavaProject project, Optional<List<OmniEclipseClasspathContainer>> containers, IProgressMonitor monitor) throws CoreException {
        new ClasspathContainerUpdater(project, containers.or(Collections.<OmniEclipseClasspathContainer>emptyList())).updateContainers(monitor);
    }

}
