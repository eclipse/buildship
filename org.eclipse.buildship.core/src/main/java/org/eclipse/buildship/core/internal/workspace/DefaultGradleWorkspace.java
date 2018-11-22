/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.buildship.core.internal.workspace;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.DefaultGradleBuild;
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration;
import org.eclipse.buildship.core.internal.configuration.GradleProjectNature;
import org.eclipse.buildship.core.internal.configuration.ProjectConfiguration;

/**
 * Default implementation of {@link InternalGradleWorkspace}.
 *
 * @author Stefan Oehme
 */
public final class DefaultGradleWorkspace implements InternalGradleWorkspace {

    private final LoadingCache<BuildConfiguration, InternalGradleBuild> cache = CacheBuilder.newBuilder().build(new CacheLoader<BuildConfiguration, InternalGradleBuild>() {

        @Override
        public InternalGradleBuild load(BuildConfiguration buildConfiguration) {
            return new DefaultGradleBuild(buildConfiguration);
        }
    });

    @Override
    public InternalGradleBuild getGradleBuild(BuildConfiguration buildConfig) {
        return this.cache.getUnchecked(buildConfig);
    }

    @Override
    public Set<InternalGradleBuild> getGradleBuilds() {
        return CorePlugin.workspaceOperations().getAllProjects().stream()
                .filter(GradleProjectNature.isPresentOn())
                .map(project -> toBuildConfigurationOrNull(project))
                .filter(Objects::nonNull)
                .map(buildConfig -> new DefaultGradleBuild(buildConfig))
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<GradleBuild> getBuild(IProject project) {
        if (GradleProjectNature.isPresentOn(project)) {
            ProjectConfiguration projectConfiguration = CorePlugin.configurationManager().tryLoadProjectConfiguration(project);
            if (projectConfiguration != null) {
                BuildConfiguration buildConfiguration = projectConfiguration.getBuildConfiguration();
                return Optional.<GradleBuild> of(getGradleBuild(buildConfiguration));
            }
        }
        return Optional.empty();
    }

    @Override
    public GradleBuild createBuild(org.eclipse.buildship.core.BuildConfiguration configuration) {
        Preconditions.checkNotNull(configuration);
        return getGradleBuild(CorePlugin.configurationManager().createBuildConfiguration(
                configuration.getRootProjectDirectory(),
                configuration.isOverrideWorkspaceConfiguration(),
                configuration.getGradleDistribution(),
                configuration.getGradleUserHome().orElse(null),
                configuration.getJavaHome().orElse(null),
                configuration.isBuildScansEnabled(),
                configuration.isOfflineMode(),
                configuration.isAutoSync()));
    }

    private static BuildConfiguration toBuildConfigurationOrNull(IProject project) {
        ProjectConfiguration projectConfiguration = CorePlugin.configurationManager().tryLoadProjectConfiguration(project);
        return projectConfiguration != null ? projectConfiguration.getBuildConfiguration() : null;
    }
}
