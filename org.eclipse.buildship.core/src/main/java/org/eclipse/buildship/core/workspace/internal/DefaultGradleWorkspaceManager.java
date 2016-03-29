/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.buildship.core.workspace.internal;

import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;

import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.util.progress.AsyncHandler;
import org.eclipse.buildship.core.workspace.GradleWorkspaceManager;
import org.eclipse.buildship.core.workspace.NewProjectHandler;

/**
 * Default implementation of {@link GradleWorkspaceManager}.
 *
 * @author Stefan Oehme
 */
public class DefaultGradleWorkspaceManager implements GradleWorkspaceManager {

    @Override
    public void synchronizeGradleBuild(FixedRequestAttributes attributes, NewProjectHandler newProjectHandler) {
        new SynchronizeGradleBuildJob(attributes, newProjectHandler, AsyncHandler.NO_OP, true).schedule();
    }

    @Override
    public void createGradleBuild(FixedRequestAttributes attributes, NewProjectHandler newProjectHandler, AsyncHandler initializer) {
        Preconditions.checkArgument(initializer != AsyncHandler.NO_OP, "Can't create projects with a no-op initializer");
        Preconditions.checkArgument(newProjectHandler != NewProjectHandler.NO_OP, "Can't import projects with a no-op handler");
        new SynchronizeGradleBuildJob(attributes, newProjectHandler, initializer, true).schedule();
    }

    @Override
    public void synchronizeProjects(Set<IProject> projects, NewProjectHandler newProjectHandler) {
        new SynchronizeGradleBuildsJob(getBuilds(projects), newProjectHandler).schedule();
    }

    private Set<FixedRequestAttributes> getBuilds(Set<IProject> projects) {
        return FluentIterable.from(projects).filter(GradleProjectNature.isPresentOn()).transform(new Function<IProject, FixedRequestAttributes>() {

            @Override
            public FixedRequestAttributes apply(IProject project) {
                return CorePlugin.projectConfigurationManager().readProjectConfiguration(project).getRequestAttributes();
            }
        }).toSet();
    }

}
