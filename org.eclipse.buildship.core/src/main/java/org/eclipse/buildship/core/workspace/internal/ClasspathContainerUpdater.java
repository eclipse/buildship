/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.workspace.internal;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.gradleware.tooling.toolingmodel.OmniEclipseClasspathContainer;
import com.gradleware.tooling.toolingmodel.OmniJavaSourceSettings;

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
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;

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

    private static final IPath DEFAULT_JRE_PATH = JavaRuntime.newDefaultJREContainerPath();

    private final IJavaProject project;
    private final boolean gradleSupportsContainers;
    private final List<OmniEclipseClasspathContainer> containers;
    private final Set<String> containerPaths;
    private final OmniJavaSourceSettings sourceSettings;

    private ClasspathContainerUpdater(IJavaProject project, Optional<List<OmniEclipseClasspathContainer>> containers, OmniJavaSourceSettings sourceSettings) {
        this.project = project;
        this.gradleSupportsContainers = containers.isPresent();
        this.containers = containers.or(Collections.<OmniEclipseClasspathContainer>emptyList());
        this.containerPaths = new HashSet<String>();
        this.sourceSettings = sourceSettings;
        for (OmniEclipseClasspathContainer container : this.containers) {
            this.containerPaths.add(container.getPath());
        }
    }

    private void updateContainers(IProgressMonitor monitor) throws CoreException {
        SubMonitor progress = SubMonitor.convert(monitor);
        progress.setWorkRemaining(3);

        LinkedHashMap<IPath, IClasspathEntry> containersToAdd = collectContainersToAdd(progress.newChild(1));
        boolean containersToAddHasJreDefinition = containsJrePath(containersToAdd.keySet());
        // if the model contains a JRE entry then remove other JREs from the classpath
        LinkedHashSet<IPath> containersToRemove = collectContainersToRemove(containersToAddHasJreDefinition, progress.newChild(1));

        updateProjectClasspath(containersToRemove, containersToAdd, progress.newChild(1));
    }

    private LinkedHashMap<IPath, IClasspathEntry> collectContainersToAdd(SubMonitor progress) throws JavaModelException {
        LinkedHashMap<IPath, IClasspathEntry> result = Maps.newLinkedHashMap();

        if (!this.gradleSupportsContainers) {
            IPath path = getJrePathFromSourceSettings();
            IClasspathEntry entry = createContainerEntry(path);
            result.put(path, entry);
        } else {
            for (OmniEclipseClasspathContainer container : this.containers) {
                IClasspathEntry entry = createContainerEntry(container);
                result.put(entry.getPath(), entry);
            }
        }

        if (!result.keySet().contains(GradleClasspathContainer.CONTAINER_PATH)) {
            result.put(GradleClasspathContainer.CONTAINER_PATH, createContainerEntry(GradleClasspathContainer.CONTAINER_PATH));
        }

        return result;
    }

    private IPath getJrePathFromSourceSettings() {
        String targetVersion = this.sourceSettings.getTargetBytecodeLevel().getName();
        File vmLocation = this.sourceSettings.getTargetRuntime().getHomeDirectory();
        IVMInstall vm = EclipseVmUtil.findOrRegisterStandardVM(targetVersion, vmLocation);
        Optional<IExecutionEnvironment> executionEnvironment = EclipseVmUtil.findExecutionEnvironment(targetVersion);
        return executionEnvironment.isPresent() ? JavaRuntime.newJREContainerPath(executionEnvironment.get()) : JavaRuntime.newJREContainerPath(vm);
    }

    private static boolean containsJrePath(Collection<IPath> containers) {
        for (IPath container : containers) {
            if(DEFAULT_JRE_PATH.isPrefixOf(container)) {
                return true;
            }
        }

        return false;
    }


    private LinkedHashSet<IPath> collectContainersToRemove(boolean includeJreContainers, SubMonitor progress) throws JavaModelException {
        StringSetProjectProperty previousPaths = StringSetProjectProperty.from(this.project.getProject(), PROJECT_PROPERTY_KEY_GRADLE_CONTAINERS);
        LinkedHashSet<IPath> result = Sets.newLinkedHashSet();
        for (String previousPath : previousPaths.get()) {
            if (!this.containerPaths.contains(previousPath)) {
                result.add(new Path(previousPath));
            }
        }

        if (includeJreContainers) {
            for (IClasspathEntry entry : this.project.getRawClasspath()) {
                if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER && DEFAULT_JRE_PATH.isPrefixOf(entry.getPath())) {
                    result.add(entry.getPath());
                }
            }
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
                    iterator.remove();
                }
            }
        }

        classpath.addAll(indexOfNewContainers(classpath), containersToAdd.values());
        this.project.setRawClasspath(classpath.toArray(new IClasspathEntry[classpath.size()]), progress);
    }

    private int indexOfNewContainers(List<IClasspathEntry> classpath) {
        // containers are added after the last source folder
        int index = 0;
        for (int i = 0; i < classpath.size(); i++) {
            if (classpath.get(i).getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                index = i + 1;
            }
        }
        return index;
    }

    private static IClasspathEntry createContainerEntry(OmniEclipseClasspathContainer container) {
        IPath containerPath = new Path(container.getPath());
        boolean isExported = container.isExported();
        IAccessRule[] accessRules = ClasspathUtils.createAccessRules(container);
        IClasspathAttribute[] attributes = ClasspathUtils.createClasspathAttributes(container);
        return JavaCore.newContainerEntry(containerPath, accessRules, attributes, isExported);
    }

    private static IClasspathEntry createContainerEntry(IPath path) {
        return JavaCore.newContainerEntry(path);
    }

    public static void update(IJavaProject project, Optional<List<OmniEclipseClasspathContainer>> containers, OmniJavaSourceSettings omniJavaSourceSettings, IProgressMonitor monitor) throws CoreException {
        new ClasspathContainerUpdater(project, containers, omniJavaSourceSettings).updateContainers(monitor);
    }

}
