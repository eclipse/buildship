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

package org.eclipse.buildship.core.gradle.model.internal;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.buildship.core.gradle.model.Dependency;
import org.eclipse.buildship.core.gradle.model.GradleModel;
import org.eclipse.buildship.core.gradle.model.Plugin;
import org.eclipse.buildship.core.gradle.model.Repository;
import org.eclipse.buildship.core.gradle.model.SourceSet;

import com.google.common.collect.ImmutableList;

/**
 * Implementation of the {@link GradleModel}.
 *
 */
public class GradleModelImpl implements GradleModel {

	private String rootProjectName;
	private Collection<Dependency> dependencies = new ArrayList<Dependency>();
	private Collection<Plugin> plugins = new ArrayList<Plugin>();
	private Collection<Repository> repositories = new ArrayList<Repository>();
	private Collection<SourceSet> sourceSets = new ArrayList<SourceSet>();

	@Override
	public String getRootProjectName() {
		return this.rootProjectName;
	}

	@Override
	public void setRootProjectName(String rootProjectName) {
		this.rootProjectName = rootProjectName;
	}

	@Override
	public void addDependencies(Collection<Dependency> dependencies) {
		this.dependencies.addAll(dependencies);
	}

	@Override
	public void removeDependencies(Collection<Dependency> dependencies) {
		this.dependencies.removeAll(dependencies);
	}

	@Override
	public Collection<Dependency> getDependencies() {
		return ImmutableList.copyOf(this.dependencies);
	}

	@Override
	public void addPlugins(Collection<Plugin> plugins) {
		this.plugins.addAll(plugins);
	}

	@Override
	public void removePlugins(Collection<Plugin> plugins) {
		this.plugins.removeAll(plugins);
	}

	@Override
	public Collection<Plugin> getPlugins() {
		return ImmutableList.copyOf(this.plugins);
	}

	@Override
	public void addRepositories(Collection<Repository> repositories) {
		this.repositories.addAll(repositories);

	}

	@Override
	public void removeRepositories(Collection<Repository> repositories) {
		this.repositories.removeAll(repositories);
	}

	@Override
	public Collection<Repository> getRepositories() {
		return ImmutableList.copyOf(this.repositories);
	}

	@Override
	public void addSourceSets(Collection<SourceSet> sourceSets) {
		this.sourceSets.addAll(sourceSets);
	}

	@Override
	public void removeSourceSets(Collection<SourceSet> sourceSets) {
		this.sourceSets.removeAll(sourceSets);
	}

	@Override
	public Collection<SourceSet> getSourceSets() {
		return ImmutableList.copyOf(this.sourceSets);
	}

}
