/*
 * Copyright (c) 2017 the original author or authors.
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
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;

import org.eclipse.buildship.core.preferences.PersistentModel;
import org.eclipse.buildship.core.preferences.PersistentModelBuilder;

/**
 * Default implementation for {@link PersistentModelBuilder}.
 *
 * @author Donat Csikos
 */
public final class DefaultPersistentModelBuilder implements PersistentModelBuilder {

    private static final IPath DEFAULT_BUILD_DIR = new Path("build");
    private static final Collection<IPath> DEFAULT_SUBPROJECTS = ImmutableList.<IPath>of();
    private static final List<IClasspathEntry> DEFAULT_CLASSPATH = ImmutableList.<IClasspathEntry>of();
    private static final Collection<IPath> DEFAULT_DERIVED_RESOURCES = ImmutableList.<IPath>of();
    private static final Collection<IPath> DEFAULT_LINKED_RESOURCES = ImmutableList.<IPath>of();

    private final IProject project;

    private IPath buildDir = DEFAULT_BUILD_DIR;
    private Collection<IPath> subprojectPaths = DEFAULT_SUBPROJECTS;
    private List<IClasspathEntry> classpath = DEFAULT_CLASSPATH;
    private Collection<IPath> derivedResources = DEFAULT_DERIVED_RESOURCES;
    private Collection<IPath> linkedResources = DEFAULT_LINKED_RESOURCES;

    public DefaultPersistentModelBuilder(IProject project) {
        this.project = Preconditions.checkNotNull(project);
    }

    public DefaultPersistentModelBuilder(PersistentModel model) {
        this(model.getProject());
        this.buildDir = model.getBuildDir();
        this.subprojectPaths = model.getSubprojectPaths();
        this.classpath = model.getClasspath();
        this.derivedResources = model.getDerivedResources();
        this.linkedResources = model.getLinkedResources();
    }

    @Override
    public PersistentModelBuilder buildDir(IPath buildDir) {
        this.buildDir = buildDir == null ? DEFAULT_BUILD_DIR : buildDir;
        return this;
    }

    @Override
    public PersistentModelBuilder subprojectPaths(Collection<IPath> subprojectPaths) {
        this.subprojectPaths = subprojectPaths == null ? DEFAULT_SUBPROJECTS : ImmutableList.copyOf(subprojectPaths);
        return this;
    }

    @Override
    public PersistentModelBuilder classpath(List<IClasspathEntry> classpath) {
        this.classpath = classpath == null ? DEFAULT_CLASSPATH : ImmutableList.copyOf(classpath);
        return this;
    }

    @Override
    public PersistentModelBuilder derivedResources(Collection<IPath> derivedResources) {
        this.derivedResources = derivedResources == null ? DEFAULT_DERIVED_RESOURCES : ImmutableList.copyOf(derivedResources);
        return this;
    }

    @Override
    public PersistentModelBuilder linkedResources(Collection<IPath> linkedResources) {
        this.linkedResources = linkedResources == null ? DEFAULT_LINKED_RESOURCES : ImmutableList.copyOf(linkedResources);
        return this;
    }

    @Override
    public PersistentModel build() {
        return new DefaultPersistentModel(this.project, false, this.buildDir, this.subprojectPaths, this.classpath, this.derivedResources, this.linkedResources);
    }

    public static PersistentModel empty(IProject project) {
        return new DefaultPersistentModel(project, true, DEFAULT_BUILD_DIR, DEFAULT_SUBPROJECTS, DEFAULT_CLASSPATH, DEFAULT_DERIVED_RESOURCES, DEFAULT_LINKED_RESOURCES);
    }
}