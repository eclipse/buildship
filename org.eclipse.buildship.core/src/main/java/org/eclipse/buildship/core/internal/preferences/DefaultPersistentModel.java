/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.preferences;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;

import org.eclipse.buildship.core.internal.util.gradle.GradleVersion;

/**
 * Default PersistentModel implementation.
 *
 * @author Donat Csikos
 */
public final class DefaultPersistentModel implements PersistentModel {

    private final IProject project;
    private final IPath buildDir;
    private final IPath buildScriptPath;
    private final Collection<IPath> subprojectPaths;
    private final List<IClasspathEntry> classpath;
    private final Collection<IPath> derivedResources;
    private final Collection<IPath> linkedResources;
    private final List<String> managedNatures;
    private final List<ICommand> managedBuilders;
    private final boolean hasAutoBuildTasks;
    private final GradleVersion gradleVersion;

    public DefaultPersistentModel(IProject project, IPath buildDir, IPath buildScriptPath,
                                  Collection<IPath> subprojectPaths, List<IClasspathEntry> classpath,
                                  Collection<IPath> derivedResources, Collection<IPath> linkedResources,
                                  Collection<String> managedNatures, Collection<ICommand> managedBuilders,
                                  boolean hasAutoBuildTasks, GradleVersion gradleVersion) {
        this.project = Preconditions.checkNotNull(project);
        this.buildDir = Preconditions.checkNotNull(buildDir);
        this.buildScriptPath = Preconditions.checkNotNull(buildScriptPath);
        this.subprojectPaths = ImmutableList.copyOf(subprojectPaths);
        this.classpath = ImmutableList.copyOf(classpath);
        this.derivedResources = ImmutableList.copyOf(derivedResources);
        this.linkedResources = ImmutableList.copyOf(linkedResources);
        this.managedNatures = ImmutableList.copyOf(managedNatures);
        this.managedBuilders = ImmutableList.copyOf(managedBuilders);
        this.hasAutoBuildTasks = hasAutoBuildTasks;
        this.gradleVersion = gradleVersion;
    }

    @Override
    public boolean isPresent() {
        return true;
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
    public IPath getbuildScriptPath() {
        return this.buildScriptPath;
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

    @Override
    public List<String> getManagedNatures() {
        return this.managedNatures;
    }

    @Override
    public List<ICommand> getManagedBuilders() {
        return this.managedBuilders;
    }

    @Override
    public boolean hasAutoBuildTasks() {
        return this.hasAutoBuildTasks;
    }

    @Override
    public GradleVersion getGradleVersion() {
        return this.gradleVersion;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof DefaultPersistentModel)) {
            return false;
        }
        DefaultPersistentModel that = (DefaultPersistentModel) obj;
        return Objects.equal(this.project, that.project)
                && Objects.equal(this.buildDir, that.buildDir)
                && Objects.equal(this.subprojectPaths, that.subprojectPaths)
                && Objects.equal(this.classpath, that.classpath)
                && Objects.equal(this.derivedResources, that.derivedResources)
                && Objects.equal(this.linkedResources, that.linkedResources)
                && Objects.equal(this.managedNatures, that.managedNatures)
                && Objects.equal(this.managedBuilders, that.managedBuilders)
                && Objects.equal(this.hasAutoBuildTasks, that.hasAutoBuildTasks)
                && Objects.equal(this.gradleVersion, that.gradleVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.project, this.buildDir, this.subprojectPaths, this.classpath, this.derivedResources, this.linkedResources, this.managedNatures, this.managedBuilders, this.hasAutoBuildTasks, this.gradleVersion);
    }

}
