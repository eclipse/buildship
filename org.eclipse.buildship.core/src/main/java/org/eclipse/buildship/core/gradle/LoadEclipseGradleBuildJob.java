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

package org.eclipse.buildship.core.gradle;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;

import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.util.progress.ToolingApiJob;
import org.eclipse.buildship.core.workspace.ModelProvider;

/**
 * Loads the {@link OmniEclipseGradleBuild} model for a given {@link org.eclipse.buildship.core.configuration.ProjectConfiguration} instance.
 */
public final class LoadEclipseGradleBuildJob extends ToolingApiJob {

    private final FetchStrategy modelFetchStrategy;
    private final ProjectConfiguration configuration;

    private OmniEclipseGradleBuild result;

    public LoadEclipseGradleBuildJob(FetchStrategy modelFetchStrategy, ProjectConfiguration configuration, final FutureCallback<OmniEclipseGradleBuild> resultHandler) {
        super(String.format("Loading tasks of project located at %s", configuration.getRequestAttributes().getProjectDir().getName()));

        this.modelFetchStrategy = Preconditions.checkNotNull(modelFetchStrategy);
        this.configuration = Preconditions.checkNotNull(configuration);

        this.result = null;

        addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(IJobChangeEvent event) {
                if (event.getResult().isOK()) {
                    resultHandler.onSuccess(LoadEclipseGradleBuildJob.this.result);
                } else {
                    resultHandler.onFailure(event.getResult().getException());
                }
            }
        });
    }

    @Override
    protected void runToolingApiJob(IProgressMonitor monitor) throws Exception {
        ModelProvider modelProvider = CorePlugin.gradleWorkspaceManager().getGradleBuild(this.configuration.getRequestAttributes()).getModelProvider();
        this.result = modelProvider.fetchEclipseGradleBuild(this.modelFetchStrategy, monitor, getToken());
    }

    @Override
    public boolean belongsTo(Object family) {
        // associate with a family so we can cancel all builds of
        // this type at once through the Eclipse progress manager
        return LoadEclipseGradleBuildJob.class.getName().equals(family);
    }

}
