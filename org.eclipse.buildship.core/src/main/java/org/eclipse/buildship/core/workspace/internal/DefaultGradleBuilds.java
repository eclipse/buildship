/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.workspace.internal;

import java.util.Iterator;
import java.util.Set;

import org.gradle.tooling.CancellationToken;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.configuration.BuildConfiguration;
import org.eclipse.buildship.core.util.progress.AsyncHandler;
import org.eclipse.buildship.core.util.progress.ToolingApiStatus;
import org.eclipse.buildship.core.workspace.GradleBuild;
import org.eclipse.buildship.core.workspace.GradleBuilds;
import org.eclipse.buildship.core.workspace.NewProjectHandler;

/**
 * Default implementation of {@link GradleBuilds}.
 */
public class DefaultGradleBuilds implements GradleBuilds {

    private final ImmutableSet<GradleBuild> gradleBuilds;

    public DefaultGradleBuilds(Set<BuildConfiguration> buildConfigs) {
        Builder<GradleBuild> builds = ImmutableSet.builder();
        for (BuildConfiguration buildConfig : buildConfigs) {
            builds.add(new DefaultGradleBuild(buildConfig));
        }
        this.gradleBuilds = builds.build();
    }

    @Override
    public void synchronize(NewProjectHandler newProjectHandler, CancellationToken token, IProgressMonitor monitor) throws CoreException {
        SynchronizeGradleBuildsJob syncJob = SynchronizeGradleBuildsJob.forMultipleGradleBuilds(this, newProjectHandler, AsyncHandler.NO_OP);

        try {
            syncJob.runInJob(monitor);
        } catch (Exception e) {
            throw new CoreException(ToolingApiStatus.from("Project synchronization", e));
        }
    }

    @Override
    public Iterator<GradleBuild> iterator() {
        return ImmutableSet.<GradleBuild>copyOf(this.getGradleBuilds()).iterator();
    }

    @Override
    public ImmutableSet<GradleBuild> getGradleBuilds() {
        return this.gradleBuilds;
    }

}
