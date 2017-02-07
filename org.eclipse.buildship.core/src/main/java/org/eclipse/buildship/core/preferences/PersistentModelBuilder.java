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

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;

/**
 * Describes how to create a new {@link PersistentModel} instance.
 *
 * @author Donat Csikos
 */
public interface PersistentModelBuilder {

    PersistentModelBuilder buildDir(IPath buildDir);

    PersistentModelBuilder subprojectPaths(Collection<IPath> subprojectPaths);

    PersistentModelBuilder classpath(List<IClasspathEntry> classpath);

    PersistentModelBuilder derivedResources(Collection<IPath> derivedResources);

    PersistentModelBuilder linkedResources(Collection<IPath> linkedResources);

    PersistentModel build();
}
