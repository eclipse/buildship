/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.preferences;

import java.util.Collection;

import com.google.common.base.Optional;

import org.eclipse.core.runtime.IPath;

/**
 * Interface to load and store Gradle model elements stored in the workspace plugin state area. The
 * model elements can be stored and retrieved in either a structured or in a map-like fashion.
 *
 * @author Donat Csikos
 */
public interface PersistentModel {

    Optional<IPath> getBuildDir();

    void setBuildDir(Optional<IPath> buildDirPath);

    Collection<IPath> getSubprojectPaths();

    void setSubprojectPaths(Collection<IPath> subprojectPaths);

    String getClasspath();

    void setClasspath(String classpath);

    Collection<String> getDerivedResources();

    void setDerivedResources(Collection<String> derivedResources);

    Collection<String> getLinkedResources();

    void setLinkedResources(Collection<String> linkedResources);
}
