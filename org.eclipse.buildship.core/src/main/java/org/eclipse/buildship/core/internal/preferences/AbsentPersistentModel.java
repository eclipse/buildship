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

import com.google.common.base.Preconditions;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;

import org.eclipse.buildship.core.internal.util.gradle.GradleVersion;

/**
 * Marker PersistentModel implementation for missing models.
 *
 * @author Donat Csikos
 */
final class AbsentPersistentModel implements PersistentModel {

    private final IProject project;

    public AbsentPersistentModel(IProject project) {
        this.project = Preconditions.checkNotNull(project);
    }

    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public IProject getProject() {
        return this.project;
    }

    @Override
    public IPath getBuildDir() {
        throw new IllegalStateException("Absent persistent model");
    }

    @Override
    public IPath getbuildScriptPath() {
        throw new IllegalStateException("Absent persistent model");
    }

    @Override
    public Collection<IPath> getSubprojectPaths() {
        throw new IllegalStateException("Absent persistent model");
    }

    @Override
    public List<IClasspathEntry> getClasspath() {
        throw new IllegalStateException("Absent persistent model");
    }

    @Override
    public Collection<IPath> getDerivedResources() {
        throw new IllegalStateException("Absent persistent model");
    }

    @Override
    public Collection<IPath> getLinkedResources() {
        throw new IllegalStateException("Absent persistent model");
    }

    @Override
    public List<String> getManagedNatures() {
        throw new IllegalStateException("Absent persistent model");
    }

    @Override
    public List<ICommand> getManagedBuilders() {
        throw new IllegalStateException("Absent persistent model");
    }

    @Override
    public boolean hasAutoBuildTasks() {
        throw new IllegalStateException("Absent persistent model");
    }

    @Override
    public GradleVersion getGradleVersion() {
        throw new IllegalStateException("Absent persistent model");
    }
}
