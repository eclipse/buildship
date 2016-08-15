/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.ui.view.task;

import java.util.Collections;
import java.util.List;

import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.connection.ModelResult;
import org.gradle.tooling.connection.ModelResults;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.util.progress.ToolingApiJob;
import org.eclipse.buildship.core.workspace.GradleBuild;

/**
 * Loads the tasks for all projects into the cache and refreshes the task view afterwards.
 */
final class ReloadTaskViewJob extends ToolingApiJob {

    private final TaskView taskView;
    private final FetchStrategy modelFetchStrategy;

    public ReloadTaskViewJob(TaskView taskView, FetchStrategy modelFetchStrategy) {
        super("Loading tasks of all Gradle projects");
        this.taskView = Preconditions.checkNotNull(taskView);
        this.modelFetchStrategy = Preconditions.checkNotNull(modelFetchStrategy);
    }

    @Override
    protected void runToolingApiJob(IProgressMonitor monitor) throws Exception {
        TaskViewContent content = loadContent(monitor);
        refreshTaskView(content);
    }

    private TaskViewContent loadContent(IProgressMonitor monitor) {
        try {
            List<OmniEclipseProject> projects = loadProjects(monitor);
            return new TaskViewContent(projects, null);
        } catch (GradleConnectionException e) {
            return new TaskViewContent(Collections.<OmniEclipseProject> emptyList(), e);
        }
    }

    private List<OmniEclipseProject> loadProjects(IProgressMonitor monitor) {
        List<OmniEclipseProject> projects = Lists.newArrayList();

        for (ProjectConfiguration configuration : CorePlugin.projectConfigurationManager().getRootProjectConfigurations()) {
            GradleBuild gradleBuild = CorePlugin.gradleWorkspaceManager().getGradleBuild(configuration.toRequestAttributes());
            ModelResults<OmniEclipseProject> results = gradleBuild.getModelProvider().fetchEclipseProjects(this.modelFetchStrategy, getToken(), monitor);
            for (ModelResult<OmniEclipseProject> result : results) {
                if (result.getFailure() == null) {
                    projects.add(result.getModel());
                }
            }
        }

        return projects;
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
}