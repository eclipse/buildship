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

import org.gradle.tooling.BuildCancelledException;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.events.test.TestProgressListener;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.ModelRepository;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;
import com.gradleware.tooling.toolingmodel.util.Pair;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.i18n.CoreMessages;
import org.eclipse.buildship.core.util.progress.ToolingApiJob;

/**
 * A job that fetches the models required for the project import preview.
 */
public final class ProjectPreviewJob extends ToolingApiJob {

    private final FixedRequestAttributes fixedAttributes;
    private final TransientRequestAttributes transientAttributes;

    private Optional<Pair<OmniBuildEnvironment, OmniGradleBuildStructure>> result;

    public ProjectPreviewJob(ProjectImportConfiguration configuration, List<ProgressListener> listeners,
            final FutureCallback<Optional<Pair<OmniBuildEnvironment, OmniGradleBuildStructure>>> resultHandler) {
        super(CoreMessages.ProjectPreviewJob_LoadingProjectPreview);

        this.fixedAttributes = configuration.toFixedAttributes();
        ProcessStreams stream = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
        this.transientAttributes = new TransientRequestAttributes(false, stream.getOutput(), stream.getError(), null, listeners, ImmutableList.<TestProgressListener> of(), getToken());

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
    protected IStatus run(IProgressMonitor monitor) {
        try {
            this.result = previewProject(monitor);
            return Status.OK_STATUS;
        } catch (BuildCancelledException e) {
            // if the job was cancelled by the user, do not show an error dialog
            CorePlugin.logger().info(e.getMessage());
            this.result = Optional.absent();
            return Status.CANCEL_STATUS;
        } catch (Exception e) {
            this.result = null;
            return new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, CoreMessages.ProjectPreviewJob_ErrorMessage_LoadingPreviewFailed, e);
        } finally {
            monitor.done();
        }
    }

    public Optional<Pair<OmniBuildEnvironment, OmniGradleBuildStructure>> previewProject(IProgressMonitor monitor) {
        monitor.beginTask(CoreMessages.ProjectPreviewJob_LoadProjectPreview, 10);

        OmniBuildEnvironment buildEnvironment = fetchBuildEnvironment(new SubProgressMonitor(monitor, 2));
        OmniGradleBuildStructure gradleBuildStructure = fetchGradleBuildStructure(new SubProgressMonitor(monitor, 8));
        return Optional.of(new Pair<OmniBuildEnvironment, OmniGradleBuildStructure>(buildEnvironment, gradleBuildStructure));
    }

    private OmniBuildEnvironment fetchBuildEnvironment(IProgressMonitor monitor) {
        monitor.beginTask(CoreMessages.ProjectPreviewJob_LoadGradleBuildEnviroment, IProgressMonitor.UNKNOWN);
        try {
            ModelRepository repository = CorePlugin.modelRepositoryProvider().getModelRepository(this.fixedAttributes);
            return repository.fetchBuildEnvironment(this.transientAttributes, FetchStrategy.FORCE_RELOAD);
        } finally {
            monitor.done();
        }
    }

    private OmniGradleBuildStructure fetchGradleBuildStructure(IProgressMonitor monitor) {
        monitor.beginTask(CoreMessages.ProjectPreviewJob_LoadGradleProjectStructure, IProgressMonitor.UNKNOWN);
        try {
            ModelRepository repository = CorePlugin.modelRepositoryProvider().getModelRepository(this.fixedAttributes);
            return repository.fetchGradleBuildStructure(this.transientAttributes, FetchStrategy.FORCE_RELOAD);
        } finally {
            monitor.done();
        }
    }

}
