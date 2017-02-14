/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.preferences.internal;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;

import org.eclipse.buildship.core.preferences.PersistentModel;

/**
 * Default implementation for {@link MutablePersistentModel}.
 *
 * @author Donat Csikos
 */
final class DefaultPersistentModel extends PersistentModel {

    private final IProject project;
    private final IPath buildDir;
    private final Collection<IPath> subprojectPaths;
    private final List<IClasspathEntry> classpath;
    private final Collection<IPath> derivedResources;
    private final Collection<IPath> linkedResources;

    DefaultPersistentModel(IProject project, IPath buildDir, Collection<IPath> subprojectPaths, List<IClasspathEntry> classpath, Collection<IPath> derivedResources,
            Collection<IPath> linkedResources) {
        this.project = Preconditions.checkNotNull(project);
        this.buildDir = Preconditions.checkNotNull(buildDir);
        this.subprojectPaths = ImmutableList.copyOf(subprojectPaths);
        this.classpath = ImmutableList.copyOf(classpath);
        this.derivedResources = ImmutableList.copyOf(derivedResources);
        this.linkedResources = ImmutableList.copyOf(linkedResources);
    }

    @Override
    public IProject getProject() {
        return this.project;
    }

    @Override
    public IPath getBuildDir() {
        return this.buildDir;
    }

    @Override
    public Collection<IPath> getSubprojectPaths() {
        return this.subprojectPaths;
    }

    @Override
    public List<IClasspathEntry> getClasspath() {
        return this.classpath;
    }

    @Override
    public Collection<IPath> getDerivedResources() {
        return this.derivedResources;
    }

    @Override
    public Collection<IPath> getLinkedResources() {
        return this.linkedResources;
    }
}
