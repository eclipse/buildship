/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.workspace.internal;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Preconditions;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;

import org.eclipse.buildship.core.preferences.PersistentModel;

/**
 * Builder for {@link PersistentModel}.
 *
 * @author Donat Csikos
 */
public final class PersistentModelBuilder {

    private final PersistentModel previous;

    private IPath buildDir;
    private Collection<IPath> subprojectPaths;
    private List<IClasspathEntry> classpath;
    private Collection<IPath> derivedResources;
    private Collection<IPath> linkedResources;

    public PersistentModelBuilder(PersistentModel previous) {
        this.previous = Preconditions.checkNotNull(previous);
        this.buildDir = previous.getBuildDir();
        this.subprojectPaths = previous.getSubprojectPaths();
        this.classpath = previous.getClasspath();
        this.derivedResources = previous.getDerivedResources();
        this.linkedResources = previous.getLinkedResources();
    }

    public PersistentModelBuilder buildDir(IPath buildDir) {
        this.buildDir = buildDir;
        return this;
    }

    public PersistentModelBuilder subprojectPaths(Collection<IPath> subprojectPaths) {
        this.subprojectPaths = subprojectPaths;
        return this;
    }

    public PersistentModelBuilder classpath(List<IClasspathEntry> classpath) {
        this.classpath = classpath;
        return this;
    }

    public PersistentModelBuilder derivedResources(Collection<IPath> derivedResources) {
        this.derivedResources = derivedResources;
        return this;
    }

    public PersistentModelBuilder linkedResources(Collection<IPath> linkedResources) {
        this.linkedResources = linkedResources;
        return this;
    }

    public PersistentModel getPrevious() {
        return this.previous;
    }

    public PersistentModel build() {
        return PersistentModel.from(this.previous.getProject(), this.buildDir, this.subprojectPaths, this.classpath, this.derivedResources, this.linkedResources);
    }
}