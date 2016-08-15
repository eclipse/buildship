/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.workspace.internal;

import java.util.List;
import java.util.Set;

import org.gradle.internal.impldep.com.google.common.collect.Lists;

import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.buildship.core.util.progress.AsyncHandler;
import org.eclipse.buildship.core.workspace.MultipleGradleBuilds;
import org.eclipse.buildship.core.workspace.NewProjectHandler;

/**
 * Default implementation of {@link MultipleGradleBuilds}.
 */
public class DefaultMultipleGradleBuilds implements MultipleGradleBuilds {

    private final List<DefaultGradleBuild> gradleBuilds;

    public DefaultMultipleGradleBuilds(Set<FixedRequestAttributes> attributes) {
        List<DefaultGradleBuild> builds = Lists.newArrayList();
        for (FixedRequestAttributes attribute : attributes) {
            builds.add(new DefaultGradleBuild(attribute));
        }
        this.gradleBuilds = builds;
    }

    @Override
    public void synchronize(NewProjectHandler newProjectHandler) {
        SynchronizeGradleBuildJob.forMultipleGradleBuilds(this.gradleBuilds, newProjectHandler, AsyncHandler.NO_OP).schedule();
    }

}
