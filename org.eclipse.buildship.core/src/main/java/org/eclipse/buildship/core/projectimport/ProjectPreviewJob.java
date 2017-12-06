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
import com.google.common.util.concurrent.FutureCallback;

import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.OmniGradleBuild;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.util.Pair;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.BuildConfiguration;
import org.eclipse.buildship.core.util.progress.AsyncHandler;
import org.eclipse.buildship.core.util.progress.ToolingApiJob;
import org.eclipse.buildship.core.util.progress.ToolingApiOperation;
import org.eclipse.buildship.core.util.progress.ToolingApiOperationResultHandler;
import org.eclipse.buildship.core.util.progress.ToolingApiStatus;
import org.eclipse.buildship.core.workspace.ModelProvider;

/**
 * A job that fetches the models required for the project import preview.
 */
public final class ProjectPreviewJob extends ToolingApiJob<Pair<OmniBuildEnvironment, OmniGradleBuild>> {

    private final BuildConfiguration buildConfig;
    private final AsyncHandler initializer;
    // TODO (donat) do we need this callback?
    private final FutureCallback<Pair<OmniBuildEnvironment, OmniGradleBuild>> resultHandler;

    public ProjectPreviewJob(ProjectImportConfiguration configuration, List<ProgressListener> listeners, AsyncHandler initializer,
                             final FutureCallback<Pair<OmniBuildEnvironment, OmniGradleBuild>> resultHandler) {
        super("Loading Gradle project preview");
        this.resultHandler = resultHandler;

        this.buildConfig = configuration.toBuildConfig();
        this.initializer = Preconditions.checkNotNull(initializer);
    }

    @Override
    public ToolingApiOperation<Pair<OmniBuildEnvironment, OmniGradleBuild>> getOperation() {
        return new ToolingApiOperation<Pair<OmniBuildEnvironment, OmniGradleBuild>>() {

            @Override
            public Pair<OmniBuildEnvironment, OmniGradleBuild> run(IProgressMonitor monitor) throws Exception {
                return loadPreview(monitor);
            }
        };
    }

    @Override
    public ToolingApiOperationResultHandler<Pair<OmniBuildEnvironment, OmniGradleBuild>> getResultHandler() {
        return new ToolingApiOperationResultHandler<Pair<OmniBuildEnvironment, OmniGradleBuild>>() {

            @Override
            public void onSuccess(Pair<OmniBuildEnvironment, OmniGradleBuild> result) {
                ProjectPreviewJob.this.resultHandler.onSuccess(result);
            }

            @Override
            public void onFailure(ToolingApiStatus status) {
                ProjectPreviewJob.this.resultHandler.onFailure(status.getException());
            }
        };
    }

    protected Pair<OmniBuildEnvironment, OmniGradleBuild> loadPreview(IProgressMonitor monitor) throws Exception {
        SubMonitor progress = SubMonitor.convert(monitor, 20);
        this.initializer.run(progress.newChild(10), getToken());
        OmniBuildEnvironment buildEnvironment = fetchBuildEnvironment(progress.newChild(2));
        OmniGradleBuild gradleBuild = fetchGradleBuildStructure(progress.newChild(8));
        return new Pair<OmniBuildEnvironment, OmniGradleBuild>(buildEnvironment, gradleBuild);
    }

    private OmniBuildEnvironment fetchBuildEnvironment(IProgressMonitor monitor) {
        ModelProvider modelProvider = CorePlugin.gradleWorkspaceManager().getGradleBuild(this.buildConfig).getModelProvider();
        return modelProvider.fetchBuildEnvironment(FetchStrategy.FORCE_RELOAD, getTokenSource(), monitor);
    }

    private OmniGradleBuild fetchGradleBuildStructure(IProgressMonitor monitor) {
        ModelProvider modelProvider = CorePlugin.gradleWorkspaceManager().getGradleBuild(this.buildConfig).getModelProvider();
        return modelProvider.fetchGradleBuild(FetchStrategy.FORCE_RELOAD, getTokenSource(), monitor);
    }
}
