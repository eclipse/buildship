/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.workspace.impl;

import java.util.Iterator;
import java.util.Set;

import org.gradle.tooling.CancellationTokenSource;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.internal.configuration.BuildConfiguration;
import org.eclipse.buildship.core.internal.workspace.GradleBuild;
import org.eclipse.buildship.core.internal.workspace.GradleBuilds;
import org.eclipse.buildship.core.internal.workspace.NewProjectHandler;

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
    public void synchronize(NewProjectHandler newProjectHandler, CancellationTokenSource tokenSource, IProgressMonitor monitor) throws CoreException {
        for (GradleBuild gradleBuild : this.gradleBuilds) {
            gradleBuild.synchronize(newProjectHandler, tokenSource, monitor);
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
