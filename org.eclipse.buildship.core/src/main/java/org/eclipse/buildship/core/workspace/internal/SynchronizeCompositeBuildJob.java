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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import com.gradleware.tooling.toolingmodel.OmniEclipseWorkspace;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.util.progress.AsyncHandler;
import org.eclipse.buildship.core.util.progress.ToolingApiJob;
import org.eclipse.buildship.core.workspace.CompositeModelProvider;
import org.eclipse.buildship.core.workspace.NewProjectHandler;

/**
 * Synchronizes the given composite build with the workspace.
 */
final class SynchronizeCompositeBuildJob extends ToolingApiJob {

    private final DefaultCompositeGradleBuild build;
    private final NewProjectHandler newProjectHandler;
    private final AsyncHandler initializer;

    public SynchronizeCompositeBuildJob(DefaultCompositeGradleBuild build, NewProjectHandler newProjectHandler, AsyncHandler initializer) {
        super("Synchronize Gradle projects with workspace", true);
        this.build = Preconditions.checkNotNull(build);
        this.newProjectHandler = Preconditions.checkNotNull(newProjectHandler);
        this.initializer = Preconditions.checkNotNull(initializer);

        // explicitly show a dialog with the progress while the project synchronization is in process
        setUser(true);

        // guarantee sequential order of synchronize jobs
        setRule(ResourcesPlugin.getWorkspace().getRoot());
    }

    @Override
    protected void runToolingApiJob(IProgressMonitor monitor) throws Exception {
        SubMonitor progress = SubMonitor.convert(monitor, 3);

        this.initializer.run(progress.newChild(1), getToken());
        OmniEclipseWorkspace workspaceModel = fetchWorkspaceModel(progress.newChild(1));
        new SynchronizeCompositeBuildOperation(workspaceModel, this.build.getBuilds(), this.newProjectHandler).run(progress.newChild(1));
        CorePlugin.listenerRegistry().dispatch(new DefaultCompositeBuildSynchronizedEvent(this.build));
    }

    private OmniEclipseWorkspace fetchWorkspaceModel(SubMonitor progress) {
        progress.setTaskName("Loading Gradle project models");
        CompositeModelProvider modelProvider = this.build.getModelProvider();
        return modelProvider.fetchEclipseWorkspace(FetchStrategy.FORCE_RELOAD, getToken(), progress);
    }

    /**
     * A {@link SynchronizeCompositeBuildJob} is only scheduled if there is not already another one that fully covers it.
     * <p/>
     * A job A fully covers a job B if all of these conditions are met:
     * <ul>
     *  <li> A synchronizes the same Gradle builds as B </li>
     *  <li> A and B have the same {@link NewProjectHandler} or B's {@link NewProjectHandler} is a no-op </li>
     *  <li> A and B have the same {@link AsyncHandler} or B's {@link AsyncHandler} is a no-op </li>
     * </ul>
     */
    @Override
    public boolean shouldSchedule() {
        for (Job job : Job.getJobManager().find(CorePlugin.GRADLE_JOB_FAMILY)) {
            if (job instanceof SynchronizeCompositeBuildJob && isCoveredBy((SynchronizeCompositeBuildJob) job)) {
                return false;
            }
        }
        return true;
    }

    private boolean isCoveredBy(SynchronizeCompositeBuildJob other) {
        return Objects.equal(this.build, other.build)
            && (this.newProjectHandler == NewProjectHandler.NO_OP || Objects.equal(this.newProjectHandler, other.newProjectHandler))
            && (this.initializer == AsyncHandler.NO_OP || Objects.equal(this.initializer, other.initializer));
    }

}
