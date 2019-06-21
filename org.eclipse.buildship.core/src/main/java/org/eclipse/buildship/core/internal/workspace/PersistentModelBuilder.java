/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.workspace;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Preconditions;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;

import org.eclipse.buildship.core.internal.preferences.DefaultPersistentModel;
import org.eclipse.buildship.core.internal.preferences.PersistentModel;
import org.eclipse.buildship.core.internal.util.gradle.GradleVersion;

/**
 * Builder for {@link PersistentModel}.
 *
 * @author Donat Csikos
 */
public final class PersistentModelBuilder {

    private final PersistentModel previous;

    private IPath buildDir;
    private IPath buildScriptPath;
    private Collection<IPath> subprojectPaths;
    private List<IClasspathEntry> classpath;
    private Collection<IPath> derivedResources;
    private Collection<IPath> linkedResources;
    private Collection<String> managedNatures;
    private Collection<ICommand> managedBuilders;
    private boolean hasAutoBuildTasks;
    private GradleVersion gradleVersion;

    public PersistentModelBuilder(PersistentModel previous) {
        this.previous = Preconditions.checkNotNull(previous);
        if (previous.isPresent()) {
            this.buildDir = previous.getBuildDir();
            this.buildScriptPath = previous.getbuildScriptPath();
            this.subprojectPaths = previous.getSubprojectPaths();
            this.classpath = previous.getClasspath();
            this.derivedResources = previous.getDerivedResources();
            this.linkedResources = previous.getLinkedResources();
            this.managedNatures = previous.getManagedNatures();
            this.managedBuilders = previous.getManagedBuilders();
            this.gradleVersion = previous.getGradleVersion();
        }
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

    public PersistentModelBuilder managedNatures(Collection<String> managedNatures) {
        this.managedNatures = managedNatures;
        return this;
    }

    public PersistentModelBuilder managedBuilders(Collection<ICommand> managedBuilders) {
        this.managedBuilders = managedBuilders;
        return this;
    }

    public PersistentModelBuilder buildScriptPath(IPath buildScriptPath) {
        this.buildScriptPath = buildScriptPath;
        return this;
    }

    public PersistentModelBuilder hasAutoBuildTasks(boolean hasAutoBuildTasks) {
        this.hasAutoBuildTasks = hasAutoBuildTasks;
        return this;
    }

    public PersistentModelBuilder gradleVersion(GradleVersion gradleVersion) {
        this.gradleVersion = gradleVersion;
        return this;
    }

    public PersistentModel getPrevious() {
        return this.previous;
    }

    public PersistentModel build() {
        return new DefaultPersistentModel(this.previous.getProject(), this.buildDir, this.buildScriptPath, this.subprojectPaths, this.classpath, this.derivedResources, this.linkedResources, this.managedNatures, this.managedBuilders, this.hasAutoBuildTasks, this.gradleVersion);
    }
}