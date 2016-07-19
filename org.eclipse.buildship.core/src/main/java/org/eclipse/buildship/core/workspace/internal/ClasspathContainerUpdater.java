/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.workspace.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingmodel.OmniAccessRule;
import com.gradleware.tooling.toolingmodel.OmniClasspathAttribute;
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

    private static final OmniEclipseClasspathContainer DEFAULT_GRADLE_CLASSPATH_CONTAINER = new OmniEclipseClasspathContainer() {

        @Override
        public Optional<List<OmniClasspathAttribute>> getClasspathAttributes() {
            return Optional.of(Collections.<OmniClasspathAttribute>emptyList());
        }

        @Override
        public Optional<List<OmniAccessRule>> getAccessRules() {
            return Optional.of(Collections.<OmniAccessRule>emptyList());
        }

        @Override
        public String getPath() {
            return GradleClasspathContainer.CONTAINER_PATH.toPortableString();
        }

        @Override
        public boolean isExported() {
            return false;
        }
    };

    private final IJavaProject project;
    private final List<OmniEclipseClasspathContainer> containers;

    private ClasspathContainerUpdater(IJavaProject project, List<OmniEclipseClasspathContainer> containers) {
        this.project = project;
        this.containers = ImmutableList.copyOf(containers);
    }

    private void updateContainers(IProgressMonitor monitor) throws CoreException {
        SubMonitor progress = SubMonitor.convert(monitor);
        progress.setWorkRemaining(2);

        StringSetProjectProperty knownContainers = StringSetProjectProperty.from(this.project.getProject(), PROJECT_PROPERTY_KEY_GRADLE_CONTAINERS);

        List<IClasspathEntry> classpath = new ArrayList<IClasspathEntry>();
        for (IClasspathEntry entry : this.project.getRawClasspath()) {
            classpath.add(entry);
        }

        removeContainersRemovedFromGradleModel(knownContainers, classpath, progress.newChild(1));
        addContainersInGradleModel(knownContainers, classpath, progress.newChild(1));
    }

    private void removeContainersRemovedFromGradleModel(StringSetProjectProperty knownContainers, List<IClasspathEntry> classpath, SubMonitor progress) throws CoreException {
        Set<String> knownContainerPaths = knownContainers.get();
        for (String knownContainerPath : knownContainerPaths) {
            if (!existsInGradleModel(knownContainerPath)) {
                removeContainer(classpath, knownContainerPath);
            }
        }
    }

    private boolean existsInGradleModel(String containerPath) {
        for (OmniEclipseClasspathContainer container : this.containers) {
            if (container.getPath().equals(containerPath)) {
                return true;
            }
        }
        return false;
    }

    private void removeContainer(List<IClasspathEntry> classpath, String pathToRemove) {
        Iterator<IClasspathEntry> iterator = classpath.iterator();
        while(iterator.hasNext()) {
            IClasspathEntry entry = iterator.next();
            if(entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER && entry.getPath().toPortableString().equals(pathToRemove)) {
                iterator.remove();
            }
        }
    }

    private void addContainersInGradleModel(StringSetProjectProperty knownContainers, List<IClasspathEntry> classpath, SubMonitor progress) throws JavaModelException {
        Set<String> paths = new HashSet<String>();
        IPath defaultJreContainerPath = JavaRuntime.newDefaultJREContainerPath();

        for(OmniEclipseClasspathContainer container : this.containers) {
            Path containerPath = new Path(container.getPath());
            // the JRE container is handled in JavaSourceSettingsUpdater
            if (!defaultJreContainerPath.isPrefixOf(containerPath)) {
                addContainer(container, classpath);
                paths.add(container.getPath());
            }
        }

        if (!paths.contains(GradleClasspathContainer.CONTAINER_PATH.toPortableString())) {
            addContainer(DEFAULT_GRADLE_CLASSPATH_CONTAINER, classpath);
        }

        knownContainers.set(paths);
        this.project.setRawClasspath(classpath.toArray(new IClasspathEntry[classpath.size()]), progress);
    }

    private void addContainer(OmniEclipseClasspathContainer container, List<IClasspathEntry> classpath) {
        ListIterator<IClasspathEntry> iterator = classpath.listIterator();
        while (iterator.hasNext()) {
            IClasspathEntry entry = iterator.next();
            if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER && entry.getPath().equals(new Path(container.getPath()))) {
                iterator.set(createContainerEntry(container));
                return;
            }
        }
        classpath.add(createContainerEntry(container));
    }

    private static IClasspathEntry createContainerEntry(OmniEclipseClasspathContainer container) {
        IPath containerPath = new Path(container.getPath());
        boolean isExported = container.isExported();
        IAccessRule[] accessRules = new IAccessRule[0];
        if (container.getAccessRules().isPresent()) {
            List<OmniAccessRule> containerRules = container.getAccessRules().get();
            accessRules = new IAccessRule[containerRules.size()];
            for (int i = 0; i < containerRules.size(); i++) {
                OmniAccessRule rule = containerRules.get(i);
                accessRules[i] = JavaCore.newAccessRule(new Path(rule.getPattern()), rule.getKind());
            }
        }
        IClasspathAttribute[] attributes = new IClasspathAttribute[0];
        if (container.getClasspathAttributes().isPresent()) {
            List<OmniClasspathAttribute> containerAttributes = container.getClasspathAttributes().get();
            attributes = new IClasspathAttribute[containerAttributes.size()];
            for (int i = 0; i < containerAttributes.size(); i++) {
                OmniClasspathAttribute attribute = containerAttributes.get(i);
                attributes[i] = JavaCore.newClasspathAttribute(attribute.getName(), attribute.getValue());
            }
        }

        return JavaCore.newContainerEntry(containerPath, accessRules, attributes, isExported);
    }

    public static void update(IJavaProject project, Optional<List<OmniEclipseClasspathContainer>> containers, IProgressMonitor monitor) throws CoreException {
        new ClasspathContainerUpdater(project, containers.or(Collections.<OmniEclipseClasspathContainer>emptyList())).updateContainers(monitor);
    }
}
