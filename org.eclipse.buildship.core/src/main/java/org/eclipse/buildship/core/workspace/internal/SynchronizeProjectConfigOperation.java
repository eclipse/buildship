/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.buildship.core.workspace.internal;

import java.io.File;
import java.util.Set;

import org.gradle.tooling.CancellationToken;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;

import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.configuration.ProjectConfigurationManager;

/**
 * Synchronizes the project configuration.
 *
 * @author Donat Csikos
 */
public final class SynchronizeProjectConfigOperation {

    private final FixedRequestAttributes requestAttributes;
    private final Set<File> projectDirs;

    public SynchronizeProjectConfigOperation(FixedRequestAttributes requestAttributes, Set<OmniEclipseProject> projects) {
        this.requestAttributes = Preconditions.checkNotNull(requestAttributes);
        this.projectDirs = FluentIterable.from(projects).transform(new Function<OmniEclipseProject, File>() {

            @Override
            public File apply(OmniEclipseProject project) {
                return project.getProjectDirectory();
            }
        }).toSet();
    }

    public void run(IProgressMonitor monitor, CancellationToken token) {
        File rootDir = this.requestAttributes.getProjectDir();
        ProjectConfigurationManager projectConfigManager = CorePlugin.projectConfigurationManager();
        Optional<IProject> rootProjectCandidate = CorePlugin.workspaceOperations().findProjectByLocation(rootDir);

        Optional<ProjectConfiguration> existingConfig;
        if (rootProjectCandidate.isPresent()) {
            existingConfig = projectConfigManager.tryReadProjectConfiguration(rootProjectCandidate.get());
        } else {
            existingConfig = projectConfigManager.tryReadProjectConfiguration(rootDir);
        }
        ProjectConfiguration updatedConfig = createUpdatedConfig(rootDir, existingConfig);
        projectConfigManager.saveProjectConfiguration(updatedConfig);
        projectConfigManager.attachProjectsToConfiguration(this.projectDirs, updatedConfig);
    }

    private ProjectConfiguration createUpdatedConfig(File rootDir, Optional<ProjectConfiguration> existingConfig) {
        if (existingConfig.isPresent()) {
            return ProjectConfiguration.fromProjectConfig(rootDir, existingConfig.get(), this.requestAttributes.getGradleDistribution());
        } else {
            return ProjectConfiguration.fromWorkspaceConfig(rootDir, this.requestAttributes.getGradleDistribution());
        }
    }

}
