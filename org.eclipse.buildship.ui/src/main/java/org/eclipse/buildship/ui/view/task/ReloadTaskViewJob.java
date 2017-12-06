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

import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.internal.DefaultOmniEclipseProject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.util.progress.ToolingApiJob;
import org.eclipse.buildship.core.util.progress.ToolingApiOperation;
import org.eclipse.buildship.core.util.progress.ToolingApiOperationResultHandler;
import org.eclipse.buildship.core.util.progress.ToolingApiStatus;
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
    }

    @Override
    public ToolingApiOperation<TaskViewContent> getOperation() {
        return new ToolingApiOperation<TaskViewContent>() {

            @Override
            public TaskViewContent run(IProgressMonitor monitor) throws Exception {
               return loadContent(monitor);
            }
        };
    }

    @Override
    public ToolingApiOperationResultHandler<TaskViewContent> getResultHandler() {
        return new ToolingApiOperationResultHandler<TaskViewContent>() {

            @Override
            public void onSuccess(TaskViewContent content) {
                refreshTaskView(content);
            }

            @Override
            public void onFailure(ToolingApiStatus status) {
                CorePlugin.getInstance().getLog().log(status);
            }
        };
    }

    private TaskViewContent loadContent(IProgressMonitor monitor) {
        List<OmniEclipseProject> projects = Lists.newArrayList();
        Map<String, IProject> faultyProjects = allGradleWorkspaceProjects();

         for (GradleBuild gradleBuild : CorePlugin.gradleWorkspaceManager().getGradleBuilds()) {
             try {
                 Set<OmniEclipseProject> eclipseProjects = fetchEclipseGradleProjects(gradleBuild.getModelProvider(), monitor);
                 for (OmniEclipseProject eclipseProject : eclipseProjects) {
                     faultyProjects.remove(eclipseProject.getName());
                 }
                 projects.addAll(eclipseProjects);
             } catch (RuntimeException ignore) {
                 // faulty projects will be represented as empty nodes
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

    private Set<OmniEclipseProject> fetchEclipseGradleProjects(ModelProvider modelProvider, IProgressMonitor monitor) {
        Collection<EclipseProject> models = modelProvider.fetchModels(EclipseProject.class, this.modelFetchStrategy, getTokenSource(), monitor);
        LinkedHashSet<OmniEclipseProject> projects = Sets.newLinkedHashSet();
        for (EclipseProject model : models) {
            projects.addAll(DefaultOmniEclipseProject.from(model).getAll());
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