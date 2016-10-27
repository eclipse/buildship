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

import com.google.common.base.Preconditions;

import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;

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

    public void startListeningTo(IWorkspace workspace) {
        workspace.addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
    }

    public void stopListeningTo(IWorkspace workspace) {
        workspace.removeResourceChangeListener(this);
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        if (hasListOfProjectsChanged(event.getDelta())) {
            this.taskView.reload(FetchStrategy.LOAD_IF_NOT_CACHED);
        }
    }

    private boolean hasListOfProjectsChanged(IResourceDelta delta) {
        if (delta.getResource() instanceof IProject) {
            int kind = delta.getKind();
            return kind == IResourceDelta.ADDED || kind == IResourceDelta.REMOVED;
        }

        for (IResourceDelta child : delta.getAffectedChildren()) {
            if (hasListOfProjectsChanged(child)) {
                return true;
            }
        }
        return false;
    }

}
