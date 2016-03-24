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

import org.gradle.tooling.ProgressListener;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.SimpleModelRepository;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.util.progress.AsyncHandler;
import org.eclipse.buildship.core.util.progress.DelegatingProgressListener;
import org.eclipse.buildship.core.util.progress.ToolingApiWorkspaceJob;
import org.eclipse.buildship.core.workspace.NewProjectHandler;

/**
 * Forces the reload of the given Gradle build and synchronizes it with the Eclipse workspace.
 */
public class SynchronizeGradleBuildJob extends ToolingApiWorkspaceJob {

    private final FixedRequestAttributes rootRequestAttributes;
    private final NewProjectHandler newProjectHandler;
    private final AsyncHandler initializer;

    public SynchronizeGradleBuildJob(FixedRequestAttributes rootRequestAttributes, NewProjectHandler newProjectHandler, AsyncHandler initializer) {
        this(rootRequestAttributes, newProjectHandler, initializer, false);
    }

    public SynchronizeGradleBuildJob(FixedRequestAttributes rootRequestAttributes, NewProjectHandler newProjectHandler, AsyncHandler initializer, boolean showUserNotifications) {
        super(String.format("Synchronize Gradle build at %s with workspace", Preconditions.checkNotNull(rootRequestAttributes).getProjectDir().getAbsolutePath()), showUserNotifications);

        this.rootRequestAttributes = Preconditions.checkNotNull(rootRequestAttributes);
        this.newProjectHandler = Preconditions.checkNotNull(newProjectHandler);
        this.initializer = Preconditions.checkNotNull(initializer);

        // explicitly show a dialog with the progress while the project synchronization is in process
        setUser(true);

        // guarantee sequential order of synchronize jobs
        setRule(ResourcesPlugin.getWorkspace().getRoot());
    }

    @Override
    protected void runToolingApiJobInWorkspace(IProgressMonitor monitor) {
        SubMonitor progress = SubMonitor.convert(monitor, 10);

        this.initializer.run(progress.newChild(1), getToken());
        OmniEclipseGradleBuild gradleBuild = forceReloadEclipseGradleBuild(this.rootRequestAttributes, progress.newChild(4));
        new SynchronizeGradleBuildOperation().synchronizeGradleBuildWithWorkspace(gradleBuild, this.rootRequestAttributes, this.newProjectHandler, progress.newChild(5));
    }

    private OmniEclipseGradleBuild forceReloadEclipseGradleBuild(FixedRequestAttributes requestAttributes, SubMonitor monitor) {
        ProcessStreams streams = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
        List<ProgressListener> listeners = ImmutableList.<ProgressListener>of(DelegatingProgressListener.withoutDuplicateLifecycleEvents(monitor));
        TransientRequestAttributes transientAttributes = new TransientRequestAttributes(false, streams.getOutput(), streams.getError(), streams.getInput(),
                listeners, ImmutableList.<org.gradle.tooling.events.ProgressListener >of(), getToken());
        SimpleModelRepository repository = CorePlugin.modelRepositoryProvider().getModelRepository(requestAttributes);
        return repository.fetchEclipseGradleBuild(transientAttributes, FetchStrategy.FORCE_RELOAD);
    }

    @Override
    public boolean belongsTo(Object family) {
        // associate with a family so we can cancel all builds of
        // this type at once through the Eclipse progress manager
        return super.belongsTo(family) || getJobFamily().equals(family);
    }

    private String getJobFamily() {
        return SynchronizeGradleBuildJob.class.getName();
    }

    /**
     * A {@link SynchronizeGradleBuildJob} is only scheduled if there is not already another one that fully covers it.
     * <p/>
     * A job A fully covers a job B if all of these conditions are met:
     * <ul>
     *  <li> A synchronizes the same Gradle build as B </li>
     *  <li> A and B have the same {@link AsyncHandler} or B's {@link AsyncHandler} is a no-op </li>
     *  <li> A and B have the same {@link NewProjectHandler} or B's {@link NewProjectHandler} is a no-op </li>
     * </ul>
     */
    @Override
    public boolean shouldSchedule() {
        for (Job job : Job.getJobManager().find(getJobFamily())) {
            if (job instanceof SynchronizeGradleBuildJob && isCoveredBy((SynchronizeGradleBuildJob) job)) {
                return false;
            }
        }
        return true;
    }

    private boolean isCoveredBy(SynchronizeGradleBuildJob other) {
        return Objects.equal(this.rootRequestAttributes, other.rootRequestAttributes)
            && (this.newProjectHandler == NewProjectHandler.NO_OP || Objects.equal(this.newProjectHandler, other.newProjectHandler))
            && (this.initializer == AsyncHandler.NO_OP || Objects.equal(this.initializer, other.initializer));
    }

}
