/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.buildship.core.workspace.internal;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.ProgressListener;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;

import org.eclipse.buildship.core.util.gradle.TransientRequestAttributes;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.BuildConfiguration;
import org.eclipse.buildship.core.configuration.RunConfiguration;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.omnimodel.OmniEclipseProject;
import org.eclipse.buildship.core.omnimodel.OmniEclipseProjectNature;
import org.eclipse.buildship.core.omnimodel.OmniProjectTask;
import org.eclipse.buildship.core.util.progress.DelegatingProgressListener;

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
    private final Set<OmniEclipseProject> allprojects;

    public RunOnImportTasksOperation(Set<OmniEclipseProject> allProjects, BuildConfiguration buildConfig) {
        this.allprojects = ImmutableSet.copyOf(allProjects);
        this.buildConfig = Preconditions.checkNotNull(buildConfig);
    }

    public void run(IProgressMonitor monitor, CancellationToken token) throws CoreException {
        List<String> tasksToRun = findWtpTasks();
        if (!tasksToRun.isEmpty()) {
            runTasks(tasksToRun, monitor, token);
        }
    }

    private List<String> findWtpTasks() {
        if (!CorePlugin.workspaceOperations().isNatureRecognizedByEclipse(WTP_COMPONENT_NATURE)) {
            return Collections.emptyList();
        }
        Set<String> cleanWtpTasks = Sets.newHashSet();
        Set<String> wtpTasks = Sets.newHashSet();

        for (OmniEclipseProject eclipseProject : this.allprojects) {
            if (isGradle30(eclipseProject) && isWtpProject(eclipseProject) && !isIncludedProject(eclipseProject)) {
                List<OmniProjectTask> tasks = eclipseProject.getGradleProject().getProjectTasks();
                for (OmniProjectTask task : tasks) {
                    if (WTP_TASK.equals(task.getName())) {
                        wtpTasks.add(task.getPath().getPath());
                    } else if (CLEAN_WTP_TASK.equals(task.getName())) {
                       cleanWtpTasks.add(task.getPath().getPath());
                    }
                }
            }
        }
        return ImmutableList.<String>builder().addAll(cleanWtpTasks).addAll(wtpTasks).build();
    }

    private boolean isGradle30(OmniEclipseProject eclipseProject) {
        return eclipseProject.getClasspathContainers().isPresent();
    }

    private boolean isWtpProject(OmniEclipseProject eclipseProject) {
        Optional<List<OmniEclipseProjectNature>> natures = eclipseProject.getProjectNatures();
        if (natures.isPresent()) {
            for (OmniEclipseProjectNature nature : natures.get()) {
                if (nature.getId().equals(WTP_COMPONENT_NATURE)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isIncludedProject(OmniEclipseProject eclipseProject) {
        File buildRoot = this.buildConfig.getRootProjectDirectory();
        File projectRoot = eclipseProject.getProjectIdentifier().getBuildIdentifier().getRootDir();
        return !buildRoot.equals(projectRoot);
    }

    private void runTasks(final List<String> tasksToRun, IProgressMonitor monitor, CancellationToken token) {
        RunConfiguration runConfiguration = CorePlugin.configurationManager().createDefaultRunConfiguration(this.buildConfig);

        BuildLauncher launcher = CorePlugin.gradleWorkspaceManager().getGradleBuild(this.buildConfig).newBuildLauncher(runConfiguration, CharStreams.nullWriter(), getTransientRequestAttributes(token, monitor));
        launcher.forTasks(tasksToRun.toArray(new String[tasksToRun.size()])).run();
    }

    private TransientRequestAttributes getTransientRequestAttributes(CancellationToken token, IProgressMonitor monitor) {
        ProcessStreams streams = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
        List<ProgressListener> progressListeners = ImmutableList.<ProgressListener> of(DelegatingProgressListener.withoutDuplicateLifecycleEvents(monitor));
        ImmutableList<org.gradle.tooling.events.ProgressListener> noEventListeners = ImmutableList.<org.gradle.tooling.events.ProgressListener> of();
        return new TransientRequestAttributes(false, streams.getOutput(), streams.getError(), streams.getInput(), progressListeners, noEventListeners, token);
    }

}
