/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.workspace;

import java.util.List;

import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingmodel.util.Pair;

import org.eclipse.core.runtime.IPath;

/**
 * Value holder for defining the sources and classpath of a Java project.
 */
public final class ClasspathDefinition {

    /**
     * Id and string representation of the path where all Gradle projects store their external
     * dependencies. This path is added during the project import and the
     * {@code org.eclipse.jdt.core.classpathContainerInitializer} extension populates it with the
     * actual external (source and binary) jars.
     */
    public static final String GRADLE_CLASSPATH_CONTAINER_ID = "org.eclipse.buildship.core.gradleclasspathcontainer"; //$NON-NLS-1$

    private final ImmutableList<Pair<IPath, IPath>> externalDependencies;
    private final ImmutableList<IPath> projectDependencies;
    private final ImmutableList<String> sourceDirectories;
    private final IPath jrePath;

    /**
     * Creates a new instance.
     *
     * @param externalDependencies the path for the jar files to have on the classpath, the first
     *            item of each {@link Pair} points to the binary, the second to the sources jar
     * @param projectDependencies the paths to the dependent local projects
     * @param sourceDirectories the paths of the source folders relative ot the project
     * @param jrePath the path to the Java runtime to include as a library in the project
     */
    public ClasspathDefinition(List<Pair<IPath, IPath>> externalDependencies, List<IPath> projectDependencies, List<String> sourceDirectories, IPath jrePath) {
        this.externalDependencies = ImmutableList.copyOf(externalDependencies);
        this.projectDependencies = ImmutableList.copyOf(projectDependencies);
        this.sourceDirectories = ImmutableList.copyOf(sourceDirectories);
        this.jrePath = jrePath;
    }

    /**
     * Returns the external dependencies of a given project.
     *
     * @return the path for the jar files to have on the classpath, the first item of each
     *         {@link Pair} points to the binary, the second to the sources jar
     */
    public List<Pair<IPath, IPath>> getExternalDependencies() {
        return this.externalDependencies;
    }

    /**
     * Returns the project dependencies of a given project.
     *
     * @return the paths to the dependent local projects
     */
    public List<IPath> getProjectDependencies() {
        return this.projectDependencies;
    }

    /**
     * Returns the source directories of a given project.
     *
     * @return the paths of the source folders relative ot the project
     */
    public List<String> getSourceDirectories() {
        return this.sourceDirectories;
    }

    /**
     * Returns the path to the JRE to include as a library of a given project.
     *
     * @return the path to the Java runtime to include as a library in the project
     */
    public IPath getJrePath() {
        return this.jrePath;
    }

}
