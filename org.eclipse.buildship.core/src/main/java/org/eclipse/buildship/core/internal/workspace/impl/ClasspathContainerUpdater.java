/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.workspace.impl;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.gradle.tooling.model.eclipse.EclipseClasspathContainer;
import org.gradle.tooling.model.eclipse.EclipseJavaSourceSettings;
import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;

import org.eclipse.buildship.core.internal.util.classpath.ClasspathUtils;
import org.eclipse.buildship.core.internal.util.gradle.CompatEclipseProject;
import org.eclipse.buildship.core.internal.util.gradle.JavaVersionUtil;
import org.eclipse.buildship.core.internal.workspace.GradleClasspathContainer;

/**
 * Updates the classpath containers of the target project.
 * <p/>
 * The update is triggered via {@link #update(IJavaProject, Optional, IProgressMonitor)}. The method
 * executes synchronously and unprotected, without thread synchronization or job scheduling.
 * <p/>
 * If the connected Gradle version supports containers, all user-defined ones will be overwritten. If Gradle
 * does not support containers, only the JRE will be updated. The Gradle classpath container will be added to the end
 * of the container list in all cases.
 *
 * Containers are added after the last source folder entry.
 * <p/>
 * The Gradle classpath container is always configured on the project.
 *
 * @author Donat Csikos
 */
final class ClasspathContainerUpdater {

    private static final IPath DEFAULT_JRE_PATH = JavaRuntime.newDefaultJREContainerPath();

    private final IJavaProject project;
    private final boolean gradleSupportsContainers;
    private final Iterable<? extends EclipseClasspathContainer> containers;
    private final EclipseJavaSourceSettings sourceSettings;

    private ClasspathContainerUpdater(IJavaProject project, EclipseProject eclipseProject) {
        this.project = project;
        this.gradleSupportsContainers = CompatEclipseProject.supportsClasspathContainers(eclipseProject);
        this.containers = eclipseProject.getClasspathContainers();
        this.sourceSettings = eclipseProject.getJavaSourceSettings();
    }

    private void updateContainers(IProgressMonitor monitor) throws CoreException {
        List<IClasspathEntry> classpath = Lists.newArrayList(this.project.getRawClasspath());
        updateContainers(classpath);
        this.project.setRawClasspath(classpath.toArray(new IClasspathEntry[classpath.size()]), monitor);
    }

    private void updateContainers(List<IClasspathEntry> classpath) throws JavaModelException {
        if (this.gradleSupportsContainers) {
            overWriteContainers(classpath);
        } else {
            updateJre(classpath);
        }
    }

    private void overWriteContainers(List<IClasspathEntry> classpath) {
        removeOldContainers(classpath);

        LinkedHashMap<IPath, IClasspathEntry> containersToAdd = Maps.newLinkedHashMap();
        for (EclipseClasspathContainer container : this.containers) {
            IClasspathEntry entry = createContainerEntry(container);
            containersToAdd.put(entry.getPath(), entry);
        }

        ensureGradleContainerIsPresent(containersToAdd);
        classpath.addAll(indexOfNewContainers(classpath), containersToAdd.values());
    }

    private void updateJre(List<IClasspathEntry> classpath) {
        Map<IPath, IClasspathEntry> oldContainers = removeOldContainers(classpath);

        LinkedHashMap<IPath, IClasspathEntry> containersToAdd = Maps.newLinkedHashMap();
        IClasspathEntry jreEntry = createContainerEntry(getJrePathFromSourceSettings());
        containersToAdd.put(jreEntry.getPath(), jreEntry);
        containersToAdd.putAll(oldContainers);

        ensureGradleContainerIsPresent(containersToAdd);
        classpath.addAll(indexOfNewContainers(classpath), containersToAdd.values());
    }

    private Map<IPath, IClasspathEntry> removeOldContainers(List<IClasspathEntry> classpath) {
        Map<IPath, IClasspathEntry> retainedEntries = Maps.newLinkedHashMap();
        ListIterator<IClasspathEntry> iterator = classpath.listIterator();
        while (iterator.hasNext()) {
            IClasspathEntry entry = iterator.next();
            if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
                if (shouldRetainContainer(entry)) {
                    retainedEntries.put(entry.getPath(), entry);
                }
                iterator.remove();
            }
        }
        return retainedEntries;
    }

    private boolean shouldRetainContainer(IClasspathEntry entry) {
        return !DEFAULT_JRE_PATH.isPrefixOf(entry.getPath());
    }

    private void ensureGradleContainerIsPresent(LinkedHashMap<IPath, IClasspathEntry> containersToAdd) {
        if (!containersToAdd.containsKey(GradleClasspathContainer.CONTAINER_PATH)) {
            containersToAdd.put(GradleClasspathContainer.CONTAINER_PATH, createContainerEntry(GradleClasspathContainer.CONTAINER_PATH));
        }
    }

    private IPath getJrePathFromSourceSettings() {
        String targetVersion = JavaVersionUtil.adaptVersionToEclipseNamingConversions(this.sourceSettings.getTargetBytecodeVersion());
        File vmLocation = this.sourceSettings.getJdk().getJavaHome();
        IVMInstall vm = EclipseVmUtil.findOrRegisterStandardVM(targetVersion, vmLocation);
        Optional<IExecutionEnvironment> executionEnvironment = EclipseVmUtil.findExecutionEnvironment(targetVersion);
        return executionEnvironment.isPresent() ? JavaRuntime.newJREContainerPath(executionEnvironment.get()) : JavaRuntime.newJREContainerPath(vm);
    }

    private int indexOfNewContainers(List<IClasspathEntry> classpath) {
        int index = 0;
        for (int i = 0; i < classpath.size(); i++) {
            if (classpath.get(i).getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                index = i + 1;
            }
        }
        return index;
    }

    private static IClasspathEntry createContainerEntry(EclipseClasspathContainer container) {
        IPath containerPath = new Path(container.getPath());
        boolean isExported = container.isExported();
        IAccessRule[] accessRules = ClasspathUtils.createAccessRules(container);
        IClasspathAttribute[] attributes = ClasspathUtils.createClasspathAttributes(container);
        return JavaCore.newContainerEntry(containerPath, accessRules, attributes, isExported);
    }

    private static IClasspathEntry createContainerEntry(IPath path) {
        return JavaCore.newContainerEntry(path);
    }

    public static void update(IJavaProject project, EclipseProject eclipseProject,
            IProgressMonitor monitor) throws CoreException {
        new ClasspathContainerUpdater(project, eclipseProject).updateContainers(monitor);
    }

}
