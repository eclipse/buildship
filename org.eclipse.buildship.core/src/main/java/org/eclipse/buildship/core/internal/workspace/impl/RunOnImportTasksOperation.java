/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.buildship.core.internal.workspace.impl;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.model.GradleTask;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.eclipse.EclipseProjectNature;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration;
import org.eclipse.buildship.core.internal.configuration.RunConfiguration;
import org.eclipse.buildship.core.internal.gradle.GradleProgressAttributes;
import org.eclipse.buildship.core.internal.util.gradle.CompatEclipseProject;

/**
 * Runs extra tasks that set up the project so it can be used in Eclipse.
 *
 * At some point users will be able to specify these tasks directly in their Gradle build. For now
 * we only have hard coded support for detecting WTP projects, for which we run the 'eclipseWtp'
 * task. That task only behaves correctly on Gradle >= 3.0, so we don't run it on older versions.
 */
public class RunOnImportTasksOperation {

    private static final String WTP_TASK = "eclipseWtp";
    private static final String CLEAN_WTP_TASK = "cleanEclipseWtp";
    private static final String WTP_COMPONENT_NATURE = "org.eclipse.wst.common.modulecore.ModuleCoreNature";

    private final BuildConfiguration buildConfig;
    private final Set<EclipseProject> allprojects;

    public RunOnImportTasksOperation(Set<? extends EclipseProject> allProjects, BuildConfiguration buildConfig) {
        this.allprojects = ImmutableSet.copyOf(allProjects);
        this.buildConfig = Preconditions.checkNotNull(buildConfig);
    }

    public void run(IProgressMonitor monitor, CancellationTokenSource tokenSource) throws CoreException {
        List<String> tasksToRun = findWtpTasks();
        if (!tasksToRun.isEmpty()) {
            runTasks(tasksToRun, monitor, tokenSource);
        }
    }

    private List<String> findWtpTasks() {
        if (!CorePlugin.workspaceOperations().isNatureRecognizedByEclipse(WTP_COMPONENT_NATURE)) {
            return Collections.emptyList();
        }
        Set<String> cleanWtpTasks = Sets.newHashSet();
        Set<String> wtpTasks = Sets.newHashSet();

        for (EclipseProject eclipseProject : this.allprojects) {
            if (isGradle30(eclipseProject) && isWtpProject(eclipseProject) && !isIncludedProject(eclipseProject)) {
                Collection<? extends GradleTask> tasks = eclipseProject.getGradleProject().getTasks();
                for (GradleTask task : tasks) {
                    if (WTP_TASK.equals(task.getName())) {
                        wtpTasks.add(task.getPath());
                    } else if (CLEAN_WTP_TASK.equals(task.getName())) {
                       cleanWtpTasks.add(task.getPath());
                    }
                }
            }
        }
        return ImmutableList.<String>builder().addAll(cleanWtpTasks).addAll(wtpTasks).build();
    }

    private boolean isGradle30(EclipseProject eclipseProject) {
        return CompatEclipseProject.supportsClasspathContainers(eclipseProject);
    }

    private boolean isWtpProject(EclipseProject eclipseProject) {
        for (EclipseProjectNature nature : eclipseProject.getProjectNatures()) {
            if (nature.getId().equals(WTP_COMPONENT_NATURE)) {
                return true;
            }
        }
        return false;
    }

    private boolean isIncludedProject(EclipseProject eclipseProject) {
        File buildRoot = this.buildConfig.getRootProjectDirectory();
        File projectRoot = eclipseProject.getProjectIdentifier().getBuildIdentifier().getRootDir();
        return !buildRoot.equals(projectRoot);
    }

    private void runTasks(final List<String> tasksToRun, IProgressMonitor monitor, CancellationTokenSource tokenSource) {
        RunConfiguration runConfiguration = CorePlugin.configurationManager().createDefaultRunConfiguration(this.buildConfig);
        GradleProgressAttributes progressAttributes = GradleProgressAttributes.builder(tokenSource, monitor)
                .forBackgroundProcess()
                .withFilteredProgress()
                .build();
        BuildLauncher launcher = CorePlugin.gradleWorkspaceManager().getGradleBuild(this.buildConfig).newBuildLauncher(runConfiguration, progressAttributes);
        launcher.forTasks(tasksToRun.toArray(new String[tasksToRun.size()])).run();
    }
}
