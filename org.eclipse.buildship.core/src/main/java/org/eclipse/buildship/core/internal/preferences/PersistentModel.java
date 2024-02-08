/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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
