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

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;

import org.eclipse.buildship.core.internal.util.gradle.GradleVersion;

/**
 * Contract how to read Gradle model elements stored in the workspace plugin state area.
 *
 * @author Donat Csikos
 */
public interface PersistentModel {
    boolean isPresent();

    IProject getProject();

    IPath getBuildDir();

    IPath getbuildScriptPath();

    Collection<IPath> getSubprojectPaths();

    List<IClasspathEntry> getClasspath();

    Collection<IPath> getDerivedResources();

    Collection<IPath> getLinkedResources();

    List<String> getManagedNatures();

    List<ICommand> getManagedBuilders();

    boolean hasAutoBuildTasks();

    GradleVersion getGradleVersion();
}
