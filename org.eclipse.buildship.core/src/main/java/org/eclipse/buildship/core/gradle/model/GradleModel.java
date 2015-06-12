/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.gradle.model;

import java.util.Collection;

/**
 * This class contains all Gradle Project specific data.
 */
public interface GradleModel {

	String getRootProjectName();

	void setRootProjectName(String rootProjectName);

	void addDependencies(Collection<Dependency> dependencies);

	void removeDependencies(Collection<Dependency> dependencies);

	Collection<Dependency> getDependencies();

	void addPlugins(Collection<Plugin> plugins);

	void removePlugins(Collection<Plugin> plugin);

	Collection<Plugin> getPlugins();

	void addRepositories(Collection<Repository> repositories);

	void removeRepositories(Collection<Repository> repositories);

	Collection<Repository> getRepositories();

	void addSourceSets(Collection<SourceSet> sourceSets);

	void removeSourceSets(Collection<SourceSet> sourceSets);

	Collection<SourceSet> getSourceSets();
}
