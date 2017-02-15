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

import org.eclipse.buildship.core.preferences.internal.DefaultPersistentModel;

/**
 * Factory to create {@link PersistentModel} instances.
 *
 * @author Donat Csikos
 */
public final class PersistentModelFactory {

    private PersistentModelFactory() {
    }

    public static PersistentModel from(IProject project, IPath buildDir, Collection<IPath> subprojectPaths, List<IClasspathEntry> classpath, Collection<IPath> derivedResources,
            Collection<IPath> linkedResources) {
        return new DefaultPersistentModel(project, buildDir, subprojectPaths, classpath, derivedResources, linkedResources);
    }
}
