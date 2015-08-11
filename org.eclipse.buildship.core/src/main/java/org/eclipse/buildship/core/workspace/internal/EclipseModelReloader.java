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

package org.eclipse.buildship.core.workspace.internal;

import java.util.List;
import java.util.Set;

import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.ProgressListener;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.console.ProcessStreams;

/**
 * Finds the root workspace project for the target projects and reloads their corresponding
 * {@link OmniEclipseGradleBuild} model.
 */
public final class EclipseModelReloader {

    private final List<IProject> projects;
    private final CancellationToken token;

    private EclipseModelReloader(List<IProject> projects, CancellationToken token) {
        this.projects = Preconditions.checkNotNull(projects);
        this.token = Preconditions.checkNotNull(token);
    }

    public ImmutableSet<OmniEclipseGradleBuild> reloadRootEclipseModels(IProgressMonitor monitor) {
        monitor.beginTask("Reload Eclipse Gradle projects", IProgressMonitor.UNKNOWN);
        try {
            ImmutableSet.Builder<OmniEclipseGradleBuild> result = ImmutableSet.builder();
            for (IProject project : rootProjectsOf(this.projects)) {
                result.add(loadEclipseGradleBuild(project, FetchStrategy.FORCE_RELOAD));
            }
            return result.build();
        } finally {
            monitor.done();
        }
    }

    private Set<IProject> rootProjectsOf(List<IProject> projects) {
        ImmutableSet.Builder<IProject> result = ImmutableSet.builder();
        for (OmniEclipseGradleBuild gradleBuild : findRootModelProjectsOf(projects)) {
            Optional<IProject> project = CorePlugin.workspaceOperations().findProjectByLocation(gradleBuild.getRootEclipseProject().getProjectDirectory());
            if (project.isPresent()) {
                result.add(project.get());
            }
        }
        return result.build();
    }

    private Set<OmniEclipseGradleBuild> findRootModelProjectsOf(List<IProject> projects) {
        ImmutableSet.Builder<OmniEclipseGradleBuild> result = ImmutableSet.builder();
        for (IProject project : projects) {
            if (project.isAccessible() && GradleProjectNature.INSTANCE.isPresentOn(project)) {
                result.add(loadEclipseGradleBuild(project, FetchStrategy.LOAD_IF_NOT_CACHED));
            }
        }
        return result.build();
    }

    private OmniEclipseGradleBuild loadEclipseGradleBuild(IProject project, FetchStrategy strategy) {
        FixedRequestAttributes requestAttributes = CorePlugin.projectConfigurationManager().readProjectConfiguration(project).getRequestAttributes();
        ProcessStreams stream = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
        return CorePlugin.modelRepositoryProvider().getModelRepository(requestAttributes).fetchEclipseGradleBuild(new TransientRequestAttributes(false, stream.getOutput(),
                stream.getError(), stream.getInput(), ImmutableList.<ProgressListener>of(), ImmutableList.<org.gradle.tooling.events.ProgressListener>of(), this.token), strategy);
    }

    public static EclipseModelReloader from(List<IProject> projects, CancellationToken token) {
        return new EclipseModelReloader(projects, token);
    }
}
