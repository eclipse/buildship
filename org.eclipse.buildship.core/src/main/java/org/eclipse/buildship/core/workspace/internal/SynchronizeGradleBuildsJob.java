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

import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.util.progress.AsyncHandler;
import org.eclipse.buildship.core.util.progress.ToolingApiJob;
import org.eclipse.buildship.core.workspace.ModelProvider;
import org.eclipse.buildship.core.workspace.NewProjectHandler;

/**
 * Synchronizes each of the given Gradle builds with the workspace.
 */
public class SynchronizeGradleBuildsJob extends ToolingApiJob {

    private final ImmutableSet<FixedRequestAttributes> builds;
    private final NewProjectHandler newProjectHandler;
    private final AsyncHandler initializer;

    public SynchronizeGradleBuildsJob(Set<FixedRequestAttributes> builds, NewProjectHandler newProjectHandler, AsyncHandler initializer) {
        super("Synchronize Gradle builds with workspace", true);
        this.builds = ImmutableSet.copyOf(builds);
        this.newProjectHandler = Preconditions.checkNotNull(newProjectHandler);
        this.initializer = Preconditions.checkNotNull(initializer);

        // explicitly show a dialog with the progress while the project synchronization is in process
        setUser(true);

        // guarantee sequential order of synchronize jobs
        setRule(ResourcesPlugin.getWorkspace().getRoot());
    }

    @Override
    protected void runToolingApiJob(IProgressMonitor monitor) throws Exception {
        SubMonitor progress = SubMonitor.convert(monitor, this.builds.size() + 1);

        this.initializer.run(progress.newChild(1), getToken());

        for (FixedRequestAttributes build : this.builds) {
            synchronizeBuild(build, progress.newChild(1));
        }
    }

    private void synchronizeBuild(FixedRequestAttributes build, SubMonitor progress) throws CoreException {
        progress.setTaskName(String.format("Synchronizing Gradle build at %s with workspace", build.getProjectDir()));
        progress.setWorkRemaining(2);
        ModelProvider modelProvider = CorePlugin.gradleWorkspaceManager().getGradleBuild(build).getModelProvider();
        OmniEclipseGradleBuild gradleBuild = modelProvider.fetchEclipseGradleBuild(FetchStrategy.FORCE_RELOAD, getToken(), progress.newChild(1));
        new SynchronizeGradleBuildOperation(gradleBuild, build, this.newProjectHandler).run(progress.newChild(1));
    }

    /**
     * A {@link SynchronizeGradleBuildsJob} is only scheduled if there is not already another one that fully covers it.
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
            if (job instanceof SynchronizeGradleBuildsJob && isCoveredBy((SynchronizeGradleBuildsJob) job)) {
                return false;
            }
        }
        return true;
    }

    private boolean isCoveredBy(SynchronizeGradleBuildsJob other) {
        return Objects.equal(this.builds, other.builds)
            && (this.newProjectHandler == NewProjectHandler.NO_OP || Objects.equal(this.newProjectHandler, other.newProjectHandler))
            && (this.initializer == AsyncHandler.NO_OP || Objects.equal(this.initializer, other.initializer));
    }

}
