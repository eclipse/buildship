/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.preferences;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;

/**
 * Interface to load and store Gradle model elements stored in the workspace plugin state area.
 * <p/>
 * Each getter may return {@code null} values if the model element is not available. If a
 * {@code null} value is passed to a setter then the model element is deleted.
 *
 * @author Donat Csikos
 */
public interface PersistentModel {

    IPath getBuildDir();

    void setBuildDir(IPath buildDir);

    Collection<IPath> getSubprojectPaths();

    void setSubprojectPaths(Collection<IPath> subprojectPaths);

    List<IClasspathEntry> getClasspath();

    void setClasspath(List<IClasspathEntry> classpath);

    Collection<IResource> getDerivedResources();

    void setDerivedResources(Collection<IResource> derivedResources);

    Collection<IFolder> getLinkedResources();

    void setLinkedResources(Collection<IFolder> linkedResources);
}
