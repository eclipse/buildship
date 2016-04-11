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

import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.buildship.core.AggregateException;
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
        super("Loading model of all Gradle builds");
        this.modelFetchStrategy = Preconditions.checkNotNull(modelFetchStrategy);
        this.configurations = ImmutableSet.copyOf(configurations);
    }

    @Override
    protected void runToolingApiJob(IProgressMonitor monitor) throws Exception {
        SubMonitor progress = SubMonitor.convert(monitor, this.configurations.size());
        List<Exception> exceptions = Lists.newArrayList();
        for (ProjectConfiguration configuration : this.configurations) {
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }
            try {
                fetchEclipseGradleBuildModel(configuration, progress);
            } catch (Exception e) {
                exceptions.add(e);
            }
        }
        if (!exceptions.isEmpty()) {
            throw new AggregateException(exceptions);
        }
    }

    private void fetchEclipseGradleBuildModel(ProjectConfiguration configuration, SubMonitor progress) throws Exception {
        FixedRequestAttributes build = configuration.getRequestAttributes();
        progress.setTaskName(String.format("Loading model of Gradle build at %s", build.getProjectDir()));
        ModelProvider modelProvider = CorePlugin.gradleWorkspaceManager().getGradleBuild(build).getModelProvider();
        modelProvider.fetchEclipseGradleBuild(this.modelFetchStrategy, getToken(), progress.newChild(1));
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
