/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.preferences.internal;

import com.google.common.base.Preconditions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.buildship.core.CorePlugin;

/**
 * An {@link IResourceChangeListener} implementation which notifies a target
 * {@link ProjectChangeHandler} about project move and project deletion events.
 *
 * @author Donat Csikos
 *
 */
final class ProjectChangeListener implements IResourceChangeListener {

    private final ProjectChangeHandler projectChangeHandler;

    public ProjectChangeListener(ProjectChangeHandler handler) {
        this.projectChangeHandler = Preconditions.checkNotNull(handler);
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        IResourceDelta delta = event.getDelta();
        if (delta != null) {
            try {
                visitDelta(delta);
            } catch (CoreException e) {
                CorePlugin.logger().warn("Failed to detect project changes", e);
            }
        }
    }

    private void visitDelta(IResourceDelta delta) throws CoreException {
        delta.accept(new IResourceDeltaVisitor() {

            @Override
            public boolean visit(IResourceDelta delta) throws CoreException {
                try {
                    return doVisitDelta(delta);
                } catch (Exception e) {
                    throw new CoreException(new Status(IStatus.WARNING, CorePlugin.PLUGIN_ID, "ProjectChangeListener failed", e));
                }
            }
        });
    }

    private boolean doVisitDelta(IResourceDelta delta) throws Exception {
        if (delta.getResource() instanceof IProject && delta.getKind() == IResourceDelta.REMOVED) {
            IProject project = (IProject) delta.getResource();
            IPath fromPath = delta.getMovedFromPath();

            if (fromPath == null) {
                ProjectChangeListener.this.projectChangeHandler.projectDeleted(project);
            } else {
                ProjectChangeListener.this.projectChangeHandler.projectMoved(fromPath, project);
            }

            return false;
        } else {
            // don't traverse deeper than the project level
            return delta.getResource() instanceof IWorkspaceRoot;
        }
    }
}
