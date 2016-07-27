/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.workspace.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.gradle.internal.impldep.com.google.common.collect.Maps;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

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

    private ClasspathContainerUpdater(IJavaProject project, List<OmniEclipseClasspathContainer> containers) {
        this.project = project;
        this.containers = ImmutableList.copyOf(containers);
        this.containerPaths = new HashSet<String>();

        for (OmniEclipseClasspathContainer container : this.containers) {
            this.containerPaths.add(container.getPath());
        }
    }

    private void updateContainers(IProgressMonitor monitor) throws CoreException {
        SubMonitor progress = SubMonitor.convert(monitor);
        progress.setWorkRemaining(3);

        LinkedHashMap<ClasspathEntryId, IClasspathEntry> classpath = collectProjectClasspath();
        removeContainersRemovedFromGradleModel(classpath, progress.newChild(1));
        addContainersPresentInGradleModel(classpath, progress.newChild(1));
        updateProjectClasspath(classpath, progress.newChild(1));
    }

    private LinkedHashMap<ClasspathEntryId, IClasspathEntry> collectProjectClasspath() throws JavaModelException {
        LinkedHashMap<ClasspathEntryId, IClasspathEntry> classpath = Maps.newLinkedHashMap();
        for (IClasspathEntry entry : this.project.getRawClasspath()) {
            classpath.put(ClasspathEntryId.create(entry.getEntryKind(), entry.getPath().toPortableString()), entry);
        }
        return classpath;
    }

    private void removeContainersRemovedFromGradleModel(LinkedHashMap<ClasspathEntryId, IClasspathEntry> classpath, SubMonitor progress) throws CoreException {
        StringSetProjectProperty previousPaths = StringSetProjectProperty.from(this.project.getProject(), PROJECT_PROPERTY_KEY_GRADLE_CONTAINERS);
        for (String previousPath : previousPaths.get()) {
            if (!this.containerPaths.contains(previousPath)) {
                classpath.remove(ClasspathEntryId.create(IClasspathEntry.CPE_CONTAINER, previousPath));
            }
        }
    }

    private void addContainersPresentInGradleModel(LinkedHashMap<ClasspathEntryId, IClasspathEntry> classpath, SubMonitor progress) throws JavaModelException {
        for (OmniEclipseClasspathContainer container : this.containers) {
            classpath.put(ClasspathEntryId.create(IClasspathEntry.CPE_CONTAINER, container.getPath()), createContainerEntry(container));
        }

        if (!this.containerPaths.contains(GradleClasspathContainer.CONTAINER_PATH.toPortableString())) {
            ClasspathEntryId key = ClasspathEntryId.create(IClasspathEntry.CPE_CONTAINER, GradleClasspathContainer.CONTAINER_PATH.toPortableString());
            IClasspathEntry value = JavaCore.newContainerEntry(GradleClasspathContainer.CONTAINER_PATH);
            // linked hash map preserves ordering if existing key-value pair is replaced
            classpath.put(key, value);
        }
    }

    private void updateProjectClasspath(LinkedHashMap<ClasspathEntryId, IClasspathEntry> classpath, SubMonitor progress) throws JavaModelException {
        StringSetProjectProperty containerPaths = StringSetProjectProperty.from(this.project.getProject(), PROJECT_PROPERTY_KEY_GRADLE_CONTAINERS);
        containerPaths.set(this.containerPaths);

        Collection<IClasspathEntry> entries = classpath.values();
        this.project.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), progress.newChild(1));
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

    /**
     * Helper class to uniquely identify classpath entries based on their type and path.
     */
    private static final class ClasspathEntryId {

        private final int kind;
        private final String path;

        private ClasspathEntryId(int kind, String path) {
            this.kind = kind;
            this.path = path;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.kind, this.path);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ClasspathEntryId that = (ClasspathEntryId) o;
            return Objects.equal(this.kind, that.kind) && Objects.equal(this.path, that.path);
        }

        public static ClasspathEntryId create(int kind, String path) {
            return new ClasspathEntryId(kind, path);
        }
    }
}
