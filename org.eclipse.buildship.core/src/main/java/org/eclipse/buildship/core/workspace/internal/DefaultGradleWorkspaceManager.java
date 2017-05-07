/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.buildship.core.workspace.internal;

import java.util.Collection;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.FluentIterable;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.BuildConfiguration;
import org.eclipse.buildship.core.configuration.ConfigurationManager;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.configuration.RunConfiguration;
import org.eclipse.buildship.core.workspace.GradleBuild;
import org.eclipse.buildship.core.workspace.GradleBuilds;
import org.eclipse.buildship.core.workspace.GradleWorkspaceManager;

/**
 * Default implementation of {@link GradleWorkspaceManager}.
 *
 * @author Stefan Oehme
 */
public class DefaultGradleWorkspaceManager implements GradleWorkspaceManager {

    private final LoadingCache<RunConfiguration, GradleBuild> cache = CacheBuilder.newBuilder().build(new CacheLoader<RunConfiguration, GradleBuild>() {

        @Override
        public GradleBuild load(RunConfiguration runConfiguration) {
            return new DefaultGradleBuild(runConfiguration);
        }});

    @Override
    public GradleBuild getGradleBuild(RunConfiguration runConfiguration) {
        return this.cache.getUnchecked(runConfiguration);
    }

    @Override
    public Optional<GradleBuild> getGradleBuild(IProject project) {
        if (GradleProjectNature.isPresentOn(project)) {
            ConfigurationManager configurationManager = CorePlugin.configurationManager();
            BuildConfiguration buildConfig = configurationManager.loadProjectConfiguration(project).getBuildConfiguration();
            return Optional.<GradleBuild>of(new DefaultGradleBuild(buildConfig.toDefaultRunConfiguration()));
        } else {
            return Optional.absent();
        }
    }

    @Override
    public GradleBuilds getGradleBuilds() {
        return new DefaultGradleBuilds(getRunConfigurations(CorePlugin.workspaceOperations().getAllProjects()));
    }

    @Override
    public GradleBuilds getGradleBuilds(Set<IProject> projects) {
        return new DefaultGradleBuilds(getRunConfigurations(projects));
    }

    private Set<RunConfiguration> getRunConfigurations(Collection<IProject> projects) {
        return FluentIterable.from(projects).filter(GradleProjectNature.isPresentOn()).transform(new Function<IProject, RunConfiguration>() {

            @Override
            public RunConfiguration apply(IProject project) {
                BuildConfiguration buildConfiguration = CorePlugin.configurationManager().loadProjectConfiguration(project).getBuildConfiguration();
                return buildConfiguration.toDefaultRunConfiguration();
            }
        }).filter(Predicates.notNull()).toSet();
    }

}
