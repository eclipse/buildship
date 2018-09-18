/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.buildship.core.internal.workspace;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration;
import org.eclipse.buildship.core.internal.configuration.GradleProjectNature;
import org.eclipse.buildship.core.internal.configuration.ProjectConfiguration;

/**
 * Default implementation of {@link GradleWorkspaceManager}.
 *
 * @author Stefan Oehme
 */
public final class DefaultGradleWorkspaceManager implements GradleWorkspaceManager {

    private final LoadingCache<BuildConfiguration, GradleBuild> cache = CacheBuilder.newBuilder().build(new CacheLoader<BuildConfiguration, GradleBuild>() {

        @Override
        public GradleBuild load(BuildConfiguration buildConfiguration) {
            return new DefaultGradleBuild(buildConfiguration);
        }});

    @Override
    public GradleBuild getGradleBuild(BuildConfiguration buildConfig) {
        return this.cache.getUnchecked(buildConfig);
    }

    @Override
    public Optional<GradleBuild> getGradleBuild(IProject project) {
        if (GradleProjectNature.isPresentOn(project)) {
            ProjectConfiguration projectConfiguration = CorePlugin.configurationManager().tryLoadProjectConfiguration(project);
            if (projectConfiguration != null) {
                return Optional.<GradleBuild>of(new DefaultGradleBuild(projectConfiguration.getBuildConfiguration()));
            } else {
                return Optional.absent();
            }
        } else {
            return  Optional.absent();
        }
    }

    @Override
    public Set<GradleBuild> getGradleBuilds() {
        return CorePlugin.workspaceOperations().getAllProjects().stream()
                .filter(GradleProjectNature.isPresentOn())
                .map(project -> toBuildConfigurationOrNull(project))
                .filter(Objects::nonNull)
                .map(buildConfig -> new DefaultGradleBuild(buildConfig))
                .collect(Collectors.toSet());
    }

    private static BuildConfiguration toBuildConfigurationOrNull(IProject project) {
        ProjectConfiguration projectConfiguration = CorePlugin.configurationManager().tryLoadProjectConfiguration(project);
        return projectConfiguration != null ? projectConfiguration.getBuildConfiguration() : null;
    }
}
