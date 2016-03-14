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

package org.eclipse.buildship.ui.view.task;

import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.ui.PlatformUI;

/**
 * Tracks the creation/deletion of projects in the workspace and updates the {@link TaskView}
 * accordingly.
 * <p>
 * Every time a project is added or removed from the workspace, the listener updates the content of
 * the task view.
 */
public final class WorkspaceProjectsChangeListener implements IResourceChangeListener {

    private final TaskView taskView;

    public WorkspaceProjectsChangeListener(TaskView taskView) {
        this.taskView = Preconditions.checkNotNull(taskView);
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        // resource creation/deletion events are bundled in the POST_CHANGE type
        if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
            Set<IProject> addedProjects = Sets.newHashSet();
            Set<IProject> deletedProjects = Sets.newHashSet();
            collectChanges(event.getDelta(), addedProjects, deletedProjects);
            if (!addedProjects.isEmpty()) {
                notifyAboutProjectAdditions(addedProjects);
            }
            if (!deletedProjects.isEmpty()) {
                notifyAboutProjectRemovals(deletedProjects);
            }
        }
    }

    private void collectChanges(IResourceDelta delta, Set<IProject> addedProjects, Set<IProject> deletedProjects) {
        IResource resource = delta.getResource();
        if (resource instanceof IProject) {
            int kind = delta.getKind();
            IProject project = (IProject) resource;
            if (kind == IResourceDelta.ADDED) {
                addedProjects.add(project);
                return;
            } else if (kind == IResourceDelta.REMOVED) {
                deletedProjects.add(project);
                return;
            }
        }

        // the resource delta object is hierarchical, thus we have to traverse its children to find
        // the project instances
        for (IResourceDelta child : delta.getAffectedChildren()) {
            collectChanges(child, addedProjects, deletedProjects);
        }
    }

    private void notifyAboutProjectAdditions(final Set<IProject> addedProjects) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                WorkspaceProjectsChangeListener.this.taskView.handleProjectAdditions(addedProjects);
            }
        });
    }

    private void notifyAboutProjectRemovals(final Set<IProject> deletedProjects) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                WorkspaceProjectsChangeListener.this.taskView.handleProjectRemovasl(deletedProjects);
            }
        });
    }

}
