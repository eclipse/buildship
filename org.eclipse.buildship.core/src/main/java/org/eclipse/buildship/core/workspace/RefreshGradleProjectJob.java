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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.ModelRepository;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;
import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.util.progress.DelegatingProgressListener;
import org.eclipse.buildship.core.util.progress.ToolingApiWorkspaceJob;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.gradle.tooling.ProgressListener;

import java.util.List;

/**
 * Forces the reload of the given Gradle (multi-)project and refreshes all affected workspace projects accordingly.
 */
public final class RefreshGradleProjectJob extends ToolingApiWorkspaceJob {

    private final FixedRequestAttributes rootRequestAttributes;

    public RefreshGradleProjectJob(FixedRequestAttributes rootRequestAttributes) {
        super("Reload root project at " + Preconditions.checkNotNull(rootRequestAttributes).getProjectDir().getAbsolutePath(), false);
        this.rootRequestAttributes = rootRequestAttributes;

        // explicitly show a dialog with the progress while the project synchronization is in process
        setUser(true);
    }

    @Override
    protected void runToolingApiJobInWorkspace(IProgressMonitor monitor) {
        monitor.beginTask("Refresh Gradle project and Eclipse workspace", 2);

        // all Java operations use the workspace root as a scheduling rule
        // see org.eclipse.jdt.internal.core.JavaModelOperation#getSchedulingRule()
        // if this rule ends during the import then other projects jobs see an
        // inconsistent workspace state, consequently we keep the rule for the whole import
        IJobManager manager = Job.getJobManager();
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        manager.beginRule(workspaceRoot, monitor);
        try {
            OmniEclipseGradleBuild gradleBuild = forceReloadEclipseGradleBuild(this.rootRequestAttributes, new SubProgressMonitor(monitor, 1));
            CorePlugin.workspaceGradleOperations().synchronizeGradleBuildWithWorkspaceProject(gradleBuild, this.rootRequestAttributes, ImmutableList.<String>of(), new SubProgressMonitor(monitor, 1));
        } finally {
            manager.endRule(workspaceRoot);
        }

        // monitor is closed by caller in super class
    }

    private OmniEclipseGradleBuild forceReloadEclipseGradleBuild(FixedRequestAttributes requestAttributes, IProgressMonitor monitor) {
        monitor.beginTask(String.format("Force reload of Gradle build located at %s", requestAttributes.getProjectDir().getAbsolutePath()), IProgressMonitor.UNKNOWN);
        try {
            ProcessStreams streams = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
            List<ProgressListener> listeners = ImmutableList.<ProgressListener>of(new DelegatingProgressListener(monitor));
            TransientRequestAttributes transientAttributes = new TransientRequestAttributes(false, streams.getOutput(), streams.getError(), streams.getInput(), listeners,
                    ImmutableList.<org.gradle.tooling.events.ProgressListener>of(), getToken());
            ModelRepository repository = CorePlugin.modelRepositoryProvider().getModelRepository(requestAttributes);
            return repository.fetchEclipseGradleBuild(transientAttributes, FetchStrategy.FORCE_RELOAD);
        } finally {
            monitor.done();
        }
    }

    @Override
    public boolean belongsTo(Object family) {
        // associate with a family so we can cancel all builds of
        // this type at once through the Eclipse progress manager
        return RefreshGradleProjectJob.class.getName().equals(family);
    }

}
