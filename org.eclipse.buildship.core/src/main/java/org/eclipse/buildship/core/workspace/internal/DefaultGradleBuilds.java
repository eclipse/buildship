/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.core.workspace.internal;

import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.buildship.core.util.progress.AsyncHandler;
import org.eclipse.buildship.core.workspace.GradleBuild;
import org.eclipse.buildship.core.workspace.GradleBuilds;
import org.eclipse.buildship.core.workspace.NewProjectHandler;

/**
 * Default implementation of {@link GradleBuilds}.
 *
 * @author Stefan Oehme
 */
public class DefaultGradleBuilds implements GradleBuilds {

    private final ImmutableSet<FixedRequestAttributes> attributes;
    private final ImmutableSet<GradleBuild> builds;

    public DefaultGradleBuilds(Set<FixedRequestAttributes> attributes) {
        this.attributes = ImmutableSet.copyOf(attributes);
        Builder<GradleBuild> builds = ImmutableSet.builder();
        for (FixedRequestAttributes attribute : attributes) {
            builds.add(new DefaultGradleBuild(attribute));
        }
        this.builds = builds.build();
    }

    @Override
    public void synchronize(NewProjectHandler newProjectHandler) {
        new SynchronizeGradleBuildsJob(this.attributes, newProjectHandler, AsyncHandler.NO_OP).schedule();
    }

    @Override
    public Iterator<GradleBuild> iterator() {
        return this.builds.iterator();
    }

}
