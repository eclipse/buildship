/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.ui.view.task;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.omnimodel.OmniEclipseProject;
import org.eclipse.buildship.core.omnimodel.OmniEclipseProjectFactory;
import org.eclipse.buildship.core.operation.ToolingApiJob;
import org.eclipse.buildship.core.operation.ToolingApiJobResultHandler;
import org.eclipse.buildship.core.operation.ToolingApiStatus;
import org.eclipse.buildship.core.workspace.FetchStrategy;
import org.eclipse.buildship.core.workspace.GradleBuild;
import org.eclipse.buildship.core.workspace.ModelProvider;

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
    }

    @Override
    public TaskViewContent runInToolingApi(CancellationTokenSource tokenSource, IProgressMonitor monitor) throws Exception {
        return loadContent(tokenSource, monitor);
    }

    private TaskViewContent loadContent(CancellationTokenSource tokenSource, IProgressMonitor monitor) {
        List<OmniEclipseProject> projects = Lists.newArrayList();
        Map<String, IProject> faultyProjects = allGradleWorkspaceProjects();

        for (GradleBuild gradleBuild : CorePlugin.gradleWorkspaceManager().getGradleBuilds()) {
            try {
                Set<OmniEclipseProject> eclipseProjects = fetchEclipseGradleProjects(gradleBuild.getModelProvider(), tokenSource, monitor);
                for (OmniEclipseProject eclipseProject : eclipseProjects) {
                    faultyProjects.remove(eclipseProject.getName());
                }
                projects.addAll(eclipseProjects);
            } catch (RuntimeException e) {
                // faulty projects will be represented as empty nodes
                CorePlugin.logger().warn("Tasks can't be loaded for project located at " + gradleBuild.getBuildConfig().getRootProjectDirectory().getAbsolutePath(), e);
            }
        }
        return new TaskViewContent(projects, Lists.newArrayList(faultyProjects.values()));
    }

    private Map<String, IProject> allGradleWorkspaceProjects() {
        Map<String, IProject> result = Maps.newLinkedHashMap();
        for (IProject project : CorePlugin.workspaceOperations().getAllProjects()) {
            if (GradleProjectNature.isPresentOn(project)) {
                result.put(project.getName(), project);
            }
        }
        return result;
    }

    private Set<OmniEclipseProject> fetchEclipseGradleProjects(ModelProvider modelProvider, CancellationTokenSource tokenSource, IProgressMonitor monitor) {
        Collection<EclipseProject> models = modelProvider.fetchModels(EclipseProject.class, this.modelFetchStrategy, tokenSource, monitor);
        LinkedHashSet<OmniEclipseProject> projects = Sets.newLinkedHashSet();
        for (EclipseProject model : models) {
            projects.addAll(OmniEclipseProjectFactory.create(model).getAll());
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