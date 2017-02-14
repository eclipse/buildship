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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;

import org.eclipse.buildship.core.preferences.PersistentModel;
import org.eclipse.buildship.core.preferences.PersistentModelBuilder;

/**
 * Default implementation for {@link PersistentModelBuilder}.
 *
 * @author Donat Csikos
 */
public final class DefaultPersistentModelBuilder implements PersistentModelBuilder {

    private final IProject project;

    private IPath buildDir;
    private Collection<IPath> subprojectPaths;
    private List<IClasspathEntry> classpath;
    private Collection<IPath> derivedResources;
    private Collection<IPath> linkedResources;

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
        this.buildDir = buildDir;
        return this;
    }

    @Override
    public PersistentModelBuilder subprojectPaths(Collection<IPath> subprojectPaths) {
        this.subprojectPaths = subprojectPaths;
        return this;
    }

    @Override
    public PersistentModelBuilder classpath(List<IClasspathEntry> classpath) {
        this.classpath = classpath;
        return this;
    }

    @Override
    public PersistentModelBuilder derivedResources(Collection<IPath> derivedResources) {
        this.derivedResources = derivedResources;
        return this;
    }

    @Override
    public PersistentModelBuilder linkedResources(Collection<IPath> linkedResources) {
        this.linkedResources = linkedResources;
        return this;
    }

    @Override
    public PersistentModel build() {
        return new DefaultPersistentModel(this.project, this.buildDir, this.subprojectPaths, this.classpath, this.derivedResources, this.linkedResources);
    }
}