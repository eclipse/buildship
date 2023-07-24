/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view.task;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.base.Preconditions;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.operation.ToolingApiJob;
import org.eclipse.buildship.core.internal.operation.ToolingApiJobResultHandler;
import org.eclipse.buildship.core.internal.operation.ToolingApiStatus;
import org.eclipse.buildship.core.internal.workspace.FetchStrategy;
import org.eclipse.buildship.core.internal.workspace.InternalGradleBuild;

/**
 * Loads the tasks for all projects into the cache and refreshes the task view afterwards.
 */
final class ReloadTaskViewJob extends ToolingApiJob<TaskViewContent> {

    private final TaskView taskView;
    private final FetchStrategy modelFetchStrategy;

    public ReloadTaskViewJob(TaskView taskView, FetchStrategy modelFetchStrategy) {
        super("Loading tasks of all Gradle projects");
        this.taskView = Preconditions.checkNotNull(taskView);
        this.modelFetchStrategy = Preconditions.checkNotNull(modelFetchStrategy);
        setResultHandler(new ResultHandler());
        setRule(ResourcesPlugin.getWorkspace().getRoot());
    }

    @Override
    public TaskViewContent runInToolingApi(CancellationTokenSource tokenSource, IProgressMonitor monitor) throws Exception {
        return loadContent(tokenSource, monitor);
    }

    private TaskViewContent loadContent(CancellationTokenSource tokenSource, IProgressMonitor monitor) {
        Map<File, Map<String, EclipseProject>> allModels = new LinkedHashMap<>();
        Map<File, BuildEnvironment> environments = new LinkedHashMap<>();
        List<InternalGradleBuild> faultyBuilds = new ArrayList<>();
        for (InternalGradleBuild gradleBuild : CorePlugin.internalGradleWorkspace().getGradleBuilds()) {
            try {
                BuildEnvironment buildEnvironment = gradleBuild.getModelProvider().fetchModel(BuildEnvironment.class, this.modelFetchStrategy, tokenSource, monitor);
                Map<String, EclipseProject> models = gradleBuild.getModelProvider().fetchModels(EclipseProject.class, this.modelFetchStrategy, tokenSource, monitor);
                if (buildEnvironment != null && models != null) {
                    allModels.put(gradleBuild.getBuildConfig().getRootProjectDirectory(), models);
                    environments.put(gradleBuild.getBuildConfig().getRootProjectDirectory(), buildEnvironment);
                } else {
                    faultyBuilds.add(gradleBuild);
                }
            } catch (RuntimeException e) {
                CorePlugin.logger().warn("Tasks can't be loaded for project located at " + gradleBuild.getBuildConfig().getRootProjectDirectory().getAbsolutePath(), e);
                faultyBuilds.add(gradleBuild);
            }
        }

        return TaskViewContent.from(allModels, environments, faultyBuilds);
    }

    private void refreshTaskView(final TaskViewContent content) {
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

            @Override
            public void run() {
                ReloadTaskViewJob.this.taskView.setContent(content);
            }
        });
    }

    @Override
    public boolean shouldSchedule() {
        Job[] jobs = Job.getJobManager().find(CorePlugin.GRADLE_JOB_FAMILY);
        for (Job job : jobs) {
            if (job instanceof ReloadTaskViewJob) {
                return false;
            }
        }
        return true;
    }

    /**
     * Custom result handler to present the results in the view.
     */
    private class ResultHandler implements ToolingApiJobResultHandler<TaskViewContent> {

        @Override
        public void onSuccess(TaskViewContent content) {
            refreshTaskView(content);
        }

        @Override
        public void onFailure(ToolingApiStatus status) {
            CorePlugin.getInstance().getLog().log(status);
        }
    }
}
