/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.core.workspace.internal;

import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

import com.gradleware.tooling.toolingmodel.repository.CompositeBuildModelRepository;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.util.progress.AsyncHandler;
import org.eclipse.buildship.core.workspace.CompositeGradleBuild;
import org.eclipse.buildship.core.workspace.CompositeModelProvider;
import org.eclipse.buildship.core.workspace.NewProjectHandler;

/**
 * Default implementation of {@link CompositeGradleBuild}.
 *
 * @author Stefan Oehme
 */
public class DefaultCompositeGradleBuild implements CompositeGradleBuild {

    private final ImmutableSet<FixedRequestAttributes> builds;

    public DefaultCompositeGradleBuild(Set<FixedRequestAttributes> builds) {
        this.builds = ImmutableSet.copyOf(builds);
    }

    @Override
    public void synchronize() {
        synchronize(NewProjectHandler.NO_OP);
    }
    @Override
    public void synchronize(NewProjectHandler newProjectHandler) {
        synchronize(newProjectHandler, AsyncHandler.NO_OP);
    }
    @Override
    public void synchronize(NewProjectHandler newProjectHandler, AsyncHandler initializer) {
        new SynchronizeCompositeBuildJob(this, newProjectHandler, initializer).schedule();
    }

    @Override
    public CompositeModelProvider getModelProvider() {
        CompositeBuildModelRepository modelRepository = CorePlugin.modelRepositoryProvider().getCompositeModelRepository(this.builds);
        return new DefaultCompositeModelprovider(modelRepository);
    }

    @Override
    public CompositeGradleBuild withBuild(FixedRequestAttributes build) {
        ImmutableSet.Builder<FixedRequestAttributes> builds = ImmutableSet.builder();
        builds.addAll(this.builds);
        builds.add(build);
        return new DefaultCompositeGradleBuild(builds.build());
    }

    ImmutableSet<FixedRequestAttributes> getBuilds() {
        return this.builds;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DefaultCompositeGradleBuild) {
            DefaultCompositeGradleBuild other = (DefaultCompositeGradleBuild) obj;
            return Objects.equal(this.builds, other.builds);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.builds);
    }
}
