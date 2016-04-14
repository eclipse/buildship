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

package org.eclipse.buildship.core.projectimport;

import java.util.List;

import org.gradle.tooling.ProgressListener;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;

import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.SingleBuildModelRepository;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;
import com.gradleware.tooling.toolingmodel.util.Pair;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.util.progress.AsyncHandler;
import org.eclipse.buildship.core.util.progress.ToolingApiWorkspaceJob;

/**
 * A job that fetches the models required for the project import preview.
 */
//TODO this job is a clear candidate to be turned into a synchronous operation instead
//then we could make use of our new ModelProvider very easily
public final class ProjectPreviewJob extends ToolingApiWorkspaceJob {

    private final FixedRequestAttributes fixedAttributes;
    private final TransientRequestAttributes transientAttributes;
    private final AsyncHandler initializer;

    private Pair<OmniBuildEnvironment, OmniGradleBuildStructure> result;

    public ProjectPreviewJob(ProjectImportConfiguration configuration, List<ProgressListener> listeners, AsyncHandler initializer,
                             final FutureCallback<Pair<OmniBuildEnvironment, OmniGradleBuildStructure>> resultHandler) {
        super("Loading Gradle project preview");

        this.fixedAttributes = configuration.toFixedAttributes();
        ProcessStreams stream = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
        this.transientAttributes = new TransientRequestAttributes(false, stream.getOutput(), stream.getError(), null, listeners,
                ImmutableList.<org.gradle.tooling.events.ProgressListener>of(), getToken());
        this.initializer = Preconditions.checkNotNull(initializer);

        this.result = null;

        addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(IJobChangeEvent event) {
                if (event.getResult().isOK()) {
                    resultHandler.onSuccess(ProjectPreviewJob.this.result);
                } else {
                    resultHandler.onFailure(event.getResult().getException());
                }
            }
        });
    }

    @Override
    protected void runToolingApiJobInWorkspace(IProgressMonitor monitor) throws Exception {
        SubMonitor progress = SubMonitor.convert(monitor, 20);
        this.initializer.run(progress.newChild(10), getToken());
        OmniBuildEnvironment buildEnvironment = fetchBuildEnvironment(progress.newChild(2));
        OmniGradleBuildStructure gradleBuildStructure = fetchGradleBuildStructure(progress.newChild(8));
        this.result = new Pair<OmniBuildEnvironment, OmniGradleBuildStructure>(buildEnvironment, gradleBuildStructure);
    }

    private OmniBuildEnvironment fetchBuildEnvironment(IProgressMonitor monitor) {
        SingleBuildModelRepository repository = CorePlugin.modelRepositoryProvider().getModelRepository(this.fixedAttributes);
        return repository.fetchBuildEnvironment(this.transientAttributes, FetchStrategy.FORCE_RELOAD);
    }

    private OmniGradleBuildStructure fetchGradleBuildStructure(IProgressMonitor monitor) {
        SingleBuildModelRepository repository = CorePlugin.modelRepositoryProvider().getModelRepository(this.fixedAttributes);
        return repository.fetchGradleBuildStructure(this.transientAttributes, FetchStrategy.FORCE_RELOAD);
    }

}
