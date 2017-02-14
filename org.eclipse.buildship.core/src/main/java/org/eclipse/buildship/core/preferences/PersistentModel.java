/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.preferences;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;

import org.eclipse.buildship.core.preferences.internal.DefaultPersistentModelBuilder;

/**
 * Contract how to read Gradle model elements stored in the workspace plugin state area.
 *
 * @author Donat Csikos
 */
public abstract class PersistentModel {

    public abstract IProject getProject();

    public abstract IPath getBuildDir();

    public abstract Collection<IPath> getSubprojectPaths();

    public abstract  List<IClasspathEntry> getClasspath();

    public abstract Collection<IPath> getDerivedResources();

    public abstract Collection<IPath> getLinkedResources();

    public static PersistentModelBuilder builder(IProject project) {
        return new DefaultPersistentModelBuilder(project);
    }

    public static PersistentModelBuilder builder(PersistentModel model) {
        return new DefaultPersistentModelBuilder(model);
    }
}
