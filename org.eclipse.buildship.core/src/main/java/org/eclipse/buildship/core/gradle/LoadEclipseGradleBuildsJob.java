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

import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.util.progress.ToolingApiJob;
import org.eclipse.buildship.core.workspace.ModelProvider;

/**
 * Loads the gradle build models for all given projects into the cache. It is ensured
 * that only one instance of this job can run at any given time.
 */
public final class LoadEclipseGradleBuildsJob extends ToolingApiJob {

    private final FetchStrategy modelFetchStrategy;
    private final ImmutableSet<ProjectConfiguration> configurations;

    public LoadEclipseGradleBuildsJob(FetchStrategy modelFetchStrategy, Set<ProjectConfiguration> configurations) {
        super("Loading tasks of all projects");
        this.modelFetchStrategy = Preconditions.checkNotNull(modelFetchStrategy);
        this.configurations = ImmutableSet.copyOf(configurations);
    }

    @Override
    protected void runToolingApiJob(IProgressMonitor monitor) throws Exception {
        SubMonitor progress = SubMonitor.convert(monitor, this.configurations.size());
        for (ProjectConfiguration configuration : this.configurations) {
            ModelProvider modelProvider = CorePlugin.gradleWorkspaceManager().getGradleBuild(configuration.getRequestAttributes()).getModelProvider();
            modelProvider.fetchEclipseGradleBuild(this.modelFetchStrategy, progress.newChild(1), getToken());
        }
    }

    @Override
    public boolean shouldSchedule() {
        Job[] jobs = Job.getJobManager().find(CorePlugin.GRADLE_JOB_FAMILY);
        for (Job job : jobs) {
            if (job instanceof LoadEclipseGradleBuildsJob) {
                return false;
            }
        }
        return true;
    }
}
