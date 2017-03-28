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
import java.util.Map;
import java.util.Set;

import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.internal.DefaultOmniEclipseProject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.util.progress.ToolingApiJob;
import org.eclipse.buildship.core.workspace.GradleBuild;
import org.eclipse.buildship.ui.view.task.TaskView.ReloadStrategy;

/**
 * Loads the tasks for all projects into the cache and refreshes the task view afterwards.
 */
final class ReloadTaskViewJob extends ToolingApiJob {

    private final TaskView taskView;
    private final ReloadStrategy reloadStrategy;

    public ReloadTaskViewJob(TaskView taskView, ReloadStrategy fetchStrategy) {
        super("Loading tasks of all Gradle projects");
        this.taskView = Preconditions.checkNotNull(taskView);
        this.reloadStrategy = Preconditions.checkNotNull(fetchStrategy);
    }

    @Override
    protected void runToolingApiJob(IProgressMonitor monitor) throws Exception {
        TaskViewContent content = loadContent(monitor);
        refreshTaskView(content);
    }

    private TaskViewContent loadContent(IProgressMonitor monitor) {
        Map<FixedRequestAttributes, Set<OmniEclipseProject>> resultProjects = Maps.newHashMap();
        Map<String, IProject> faultyProjects = allGradleWorkspaceProjects();

         for (GradleBuild gradleBuild : CorePlugin.gradleWorkspaceManager().getGradleBuilds()) {
             try {
                 Set<OmniEclipseProject> eclipseProjects = fetchEclipseGradleProjects(gradleBuild, monitor);
                 for (OmniEclipseProject eclipseProject : eclipseProjects) {
                     faultyProjects.remove(eclipseProject.getName());
                 }
                 resultProjects.put(gradleBuild.getRequestAttributes(), eclipseProjects);
             } catch (RuntimeException e) {
                 CorePlugin.logger().warn("Tasks can't be loaded for project located at " + gradleBuild.getRequestAttributes().getProjectDir().getAbsolutePath(), e);
             }
         }

        return new TaskViewContent(resultProjects, Lists.newArrayList(faultyProjects.values()));
    }

    private Set<OmniEclipseProject> fetchEclipseGradleProjects(GradleBuild gradleBuild, IProgressMonitor monitor) {
        if (this.reloadStrategy == ReloadStrategy.LOAD_IF_NOT_CACHED) {
            TaskViewContent input = (TaskViewContent) this.taskView.getTreeViewer().getInput();
            if (input != null) {
                Set<OmniEclipseProject> cached = input.findProjectsFor(gradleBuild.getRequestAttributes());
                if (cached != null) {
                    return cached;
                }
            }
        }
        Collection<EclipseProject> models = gradleBuild.queryCompositeModel(EclipseProject.class, getToken(), monitor);
        ImmutableSet.Builder<OmniEclipseProject> projects = ImmutableSet.builder();
        for (EclipseProject model : models) {
            projects.addAll(DefaultOmniEclipseProject.from(model).getAll());
        }
        return projects.build();
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