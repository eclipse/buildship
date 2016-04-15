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
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.workspace.CompositeGradleBuild;
import org.eclipse.buildship.core.workspace.GradleBuild;
import org.eclipse.buildship.core.workspace.GradleWorkspaceManager;

/**
 * Default implementation of {@link GradleWorkspaceManager}.
 *
 * @author Stefan Oehme
 */
public class DefaultGradleWorkspaceManager implements GradleWorkspaceManager, IResourceChangeListener {

    @Override
    public GradleBuild getGradleBuild(FixedRequestAttributes attributes) {
        return new DefaultGradleBuild(attributes);
    }

    @Override
    public Optional<GradleBuild> getGradleBuild(IProject project) {
        Set<FixedRequestAttributes> builds = getBuilds(ImmutableSet.of(project));
        if (builds.isEmpty()) {
            return Optional.absent();
        } else {
            return Optional.of(getGradleBuild(builds.iterator().next()));
        }
    }

    @Override
    public CompositeGradleBuild getCompositeBuild() {
        Set<IProject> allProjects = Sets.newHashSet(ResourcesPlugin.getWorkspace().getRoot().getProjects());
        return new DefaultCompositeGradleBuild(getBuilds(allProjects));
    }

    private Set<FixedRequestAttributes> getBuilds(Set<IProject> projects) {
        return FluentIterable.from(projects).filter(GradleProjectNature.isPresentOn()).transform(new Function<IProject, FixedRequestAttributes>() {

            @Override
            public FixedRequestAttributes apply(IProject project) {
                return CorePlugin.projectConfigurationManager().readProjectConfiguration(project).getRequestAttributes();
            }
        }).toSet();
    }

    public void startListeningTo(IWorkspace workspace) {
        workspace.addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
    }

    public void stopListeningTo(IWorkspace workspace) {
        workspace.removeResourceChangeListener(this);
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        if (containsProjectChanges(event.getDelta())) {
            getCompositeBuild().synchronize();
        }
    }

    private boolean containsProjectChanges(IResourceDelta delta) {
        IResource resource = delta.getResource();
        if (resource instanceof IProject) {
            int kind = delta.getKind();
            boolean addedOrRemoved = kind == IResourceDelta.ADDED || kind == IResourceDelta.REMOVED;
            boolean openedOrClosed = (delta.getFlags() & IResourceDelta.OPEN) != 0;
            return addedOrRemoved || openedOrClosed;
        }

        for (IResourceDelta child : delta.getAffectedChildren()) {
            if (containsProjectChanges(child)) {
                return true;
            }
        }
        return false;
    }

}
