/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal;

import java.util.Optional;

import com.google.common.base.Preconditions;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.BuildConfiguration;
import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.GradleWorkspace;
import org.eclipse.buildship.core.internal.configuration.GradleProjectNature;
import org.eclipse.buildship.core.internal.configuration.ProjectConfiguration;

/**
 * Default implementation for {@link GradleWorkspace}.
 *
 * @author Donat Csikos
 */
public final class DefaultGradleWorkspace implements GradleWorkspace {

    @Override
    public Optional<GradleBuild> getBuild(IProject project) {
        if (GradleProjectNature.isPresentOn(project)) {
            ProjectConfiguration projectConfiguration = CorePlugin.configurationManager().tryLoadProjectConfiguration(project);
            if (projectConfiguration != null) {
                return Optional.of(new DefaultGradleBuild(projectConfiguration.getBuildConfiguration()));
            }
        }
        return Optional.empty();
    }

    @Override
    public GradleBuild createBuild(BuildConfiguration configuration) {
        // TODO merge this class and GradleWorkspaceManager
        Preconditions.checkNotNull(configuration);
        return CorePlugin.gradleWorkspaceManager().getGradleBuild(CorePlugin.configurationManager().createBuildConfiguration(
            configuration.getRootProjectDirectory(),
            configuration.isOverrideWorkspaceConfiguration(),
            configuration.getGradleDistribution(),
            configuration.getGradleUserHome().orElse(null),
            configuration.isBuildScansEnabled(),
            configuration.isOfflineMode(),
            configuration.isAutoSync()));
    }
}
