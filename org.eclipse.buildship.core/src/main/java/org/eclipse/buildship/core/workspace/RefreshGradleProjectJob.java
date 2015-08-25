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

import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProgressListener;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.ModelRepositoryProvider;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.util.predicate.Predicates;
import org.eclipse.buildship.core.util.progress.DelegatingProgressListener;

/**
 * Job forcing Gradle model reload for a Gradle project and requests a refresh for all contained
 * workspace projects.
 */
public final class RefreshGradleProjectJob extends Job {

    private final FixedRequestAttributes attributes;
    private final CancellationTokenSource tokenSource;

    public RefreshGradleProjectJob(FixedRequestAttributes attributes) {
        super("Reload project from " + Preconditions.checkNotNull(attributes).getProjectDir().getAbsolutePath());
        this.attributes = attributes;
        this.tokenSource = GradleConnector.newCancellationTokenSource();
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask("Reload projects and request project update", IProgressMonitor.UNKNOWN);
        try {
            OmniEclipseGradleBuild result = forceReloadEclipseGradleBuild(this.attributes, monitor);
            requestUpdateOnAllWorkspaceProject(result.getRootEclipseProject().getAll());
            return Status.OK_STATUS;
        } catch (Exception e) {
            return new Status(IStatus.WARNING, CorePlugin.PLUGIN_ID, "refresh failed", e);
        } finally {
            monitor.done();
        }
    }

    private OmniEclipseGradleBuild forceReloadEclipseGradleBuild(FixedRequestAttributes requestAttributes, final IProgressMonitor monitor) {
        ProcessStreams streams = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
        ImmutableList<ProgressListener> listeners = ImmutableList.<ProgressListener>of(new DelegatingProgressListener(monitor));
        TransientRequestAttributes transientAttributes = new TransientRequestAttributes(false, streams.getOutput(), streams.getError(), streams.getInput(), listeners,
                ImmutableList.<org.gradle.tooling.events.ProgressListener>of(), this.tokenSource.token());
        ModelRepositoryProvider repository = CorePlugin.modelRepositoryProvider();
        return repository.getModelRepository(requestAttributes).fetchEclipseGradleBuild(transientAttributes, FetchStrategy.FORCE_RELOAD);
    }

    private void requestUpdateOnAllWorkspaceProject(List<OmniEclipseProject> gradleProjects) {
        for (OmniEclipseProject gradleProject : gradleProjects) {
            requestUpdateOnWorkspaceProject(gradleProject);
        }
    }

    private void requestUpdateOnWorkspaceProject(OmniEclipseProject gradleProject) {
        // todo (donat) the update mechanism should be extended to non-java projects too
        Optional<IProject> workspaceProject = CorePlugin.workspaceOperations().findProjectByLocation(gradleProject.getProjectDirectory());
        if (workspaceProject.isPresent()) {
            if (Predicates.accessibleGradleJavaProject().apply(workspaceProject.get())) {
                IJavaProject javaProject = JavaCore.create(workspaceProject.get());
                GradleClasspathContainer.requestUpdateOf(javaProject);
            }
        }
    }

    @Override
    public boolean belongsTo(Object family) {
        // associate with a family so we can cancel all builds of
        // this type at once through the Eclipse progress manager
        return RefreshGradleProjectJob.class.getName().equals(family);
    }

    @Override
    protected void canceling() {
        this.tokenSource.cancel();
    }

}
