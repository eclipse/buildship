/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.workspace.internal;

import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.util.progress.AsyncHandler;
import org.eclipse.buildship.core.util.progress.ToolingApiJob;
import org.eclipse.buildship.core.workspace.GradleBuild;
import org.eclipse.buildship.core.workspace.GradleBuilds;
import org.eclipse.buildship.core.workspace.ModelProvider;
import org.eclipse.buildship.core.workspace.NewProjectHandler;

/**
 * Synchronizes each of the given Gradle builds with the workspace.
 */
public final class SynchronizeGradleBuildsJob extends ToolingApiJob {

    private final ImmutableSet<GradleBuild> builds;
    private final NewProjectHandler newProjectHandler;
    private final AsyncHandler initializer;

    private SynchronizeGradleBuildsJob(Set<GradleBuild> builds, NewProjectHandler newProjectHandler, AsyncHandler initializer) {
        super("Synchronize Gradle projects with workspace", true);
        this.builds = ImmutableSet.copyOf(builds);
        this.newProjectHandler = Preconditions.checkNotNull(newProjectHandler);
        this.initializer = Preconditions.checkNotNull(initializer);

        // explicitly show a dialog with the progress while the project synchronization is in
        // process
        setUser(true);

        // guarantee sequential order of synchronize jobs
        setRule(ResourcesPlugin.getWorkspace().getRoot());
    }

    Set<GradleBuild> getBuilds() {
        return this.builds;
    }

    @Override
    protected void runToolingApiJob(IProgressMonitor monitor) throws Exception {
        final SubMonitor progress = SubMonitor.convert(monitor, this.builds.size() + 1);

        this.initializer.run(progress.newChild(1), getToken());

        for (GradleBuild build : this.builds) {
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }
            synchronizeBuild(build, progress.newChild(1));
        }
    }

    private void synchronizeBuild(GradleBuild build, SubMonitor progress) throws CoreException {
        progress.setTaskName((String.format("Synchronizing Gradle build at %s with workspace", build.getRequestAttributes().getProjectDir())));
        progress.setWorkRemaining(3);
        Set<OmniEclipseProject> allProjects = fetchEclipseProjects(build, progress.newChild(1));
        new SynchronizeProjectConfigOperation(build.getRequestAttributes(), allProjects).run(progress.newChild(1), getToken());
        new RunOnImportTasksOperation(allProjects, build.getRequestAttributes()).run(progress.newChild(1), getToken());
        new SynchronizeGradleBuildOperation(allProjects, build.getRequestAttributes(), SynchronizeGradleBuildsJob.this.newProjectHandler).run(progress.newChild(1));
    }

    private Set<OmniEclipseProject> fetchEclipseProjects(GradleBuild build, SubMonitor progress) {
        progress.setTaskName("Loading Gradle project models");
        ModelProvider modelProvider = build.getModelProvider();
        return modelProvider.fetchEclipseGradleProjects(FetchStrategy.FORCE_RELOAD, getToken(), progress);
    }

    /**
     * A {@link SynchronizeGradleBuildsJob} is only scheduled if there is not already another one that
     * fully covers it.
     * <p/>
     * A job A fully covers a job B if all of these conditions are met:
     * <ul>
     * <li>A synchronizes the same Gradle builds as B</li>
     * <li>A and B have the same {@link NewProjectHandler} or B's {@link NewProjectHandler} is a
     * no-op</li>
     * <li>A and B have the same {@link AsyncHandler} or B's {@link AsyncHandler} is a no-op</li>
     * </ul>
     */
    @Override
    public boolean shouldSchedule() {
        for (Job job : Job.getJobManager().find(CorePlugin.GRADLE_JOB_FAMILY)) {
            if (job instanceof SynchronizeGradleBuildsJob && isCoveredBy((SynchronizeGradleBuildsJob) job)) {
                return false;
            }
        }
        return true;
    }

    private boolean isCoveredBy(SynchronizeGradleBuildsJob other) {
        return Objects.equal(this.builds, other.builds) && (this.newProjectHandler == NewProjectHandler.NO_OP || Objects.equal(this.newProjectHandler, other.newProjectHandler))
                && (this.initializer == AsyncHandler.NO_OP || Objects.equal(this.initializer, other.initializer));
    }

    public static SynchronizeGradleBuildsJob forSingleGradleBuild(GradleBuild build, NewProjectHandler newProjectHandler, AsyncHandler initializer) {
        return new SynchronizeGradleBuildsJob(ImmutableSet.of(build), newProjectHandler, initializer);
    }

    public static SynchronizeGradleBuildsJob forMultipleGradleBuilds(GradleBuilds builds, NewProjectHandler newProjectHandler, AsyncHandler initializer) {
        return new SynchronizeGradleBuildsJob(builds.getGradleBuilds(), newProjectHandler, initializer);
    }

}
