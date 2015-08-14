/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 *     Ian Stewart-Binks (Red Hat Inc.) - Bug 473862 - F5 key shortcut doesn't refresh project folder contents
 *     Donát Csikós (Gradle Inc.) - Bug 471786
 */

package org.eclipse.buildship.ui.workspace;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.buildship.core.workspace.RefreshGradleProjectsJob;

/**
 * Collects all selected {@link IProject} instances and schedules a
 * {@link RefreshGradleProjectsJob} to refresh these projects.
 */
public final class GradleClasspathContainerRefresher {

    public static void execute(final ExecutionEvent event) {
        List<IProject> selectedProjects = collectSelectedProjects(event);
        RefreshGradleProjectsJob refreshJob = new RefreshGradleProjectsJob(selectedProjects);
        refreshJob.schedule();
    }

    private static List<IProject> collectSelectedProjects(ExecutionEvent event) {
        ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
        Builder<IProject> result = ImmutableList.builder();
        if (currentSelection instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) currentSelection;
            IAdapterManager adapterManager = Platform.getAdapterManager();
            for (Object selectionItem : selection.toList()) {
                @SuppressWarnings({ "cast", "RedundantCast" })
                IResource resource = (IResource) adapterManager.getAdapter(selectionItem, IResource.class);
                if (resource != null) {
                    IProject project = resource.getProject();
                    if (project != null) {
                        result.add(project);
                    }
                }
            }
        }
        return result.build();
    }

}
