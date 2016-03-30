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

import java.util.Set;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.util.collections.AdapterFunction;
import org.eclipse.buildship.core.workspace.GradleBuild;
import org.eclipse.buildship.core.workspace.NewProjectHandler;
import org.eclipse.buildship.ui.util.predicate.Predicates;

/**
 * Collects all selected, Gradle-aware {@link IProject} instances and schedules a
 * {@link SynchronizeGradleProjectsJob} to refresh these projects.
 *
 * @see SynchronizeGradleProjectsJob
 */
public final class ProjectSynchronizer {

    public static void execute(final ExecutionEvent event) {
        Set<IProject> selectedProjects = collectSelectedProjects(event);
        if (selectedProjects.isEmpty()) {
            return;
        }
        Set<GradleBuild> gradleBuilds = CorePlugin.gradleWorkspaceManager().getGradleBuilds(selectedProjects);
        for (GradleBuild gradleBuild : gradleBuilds) {
            gradleBuild.synchronize(NewProjectHandler.IMPORT_AND_MERGE);
        }
    }

    private static Set<IProject> collectSelectedProjects(ExecutionEvent event) {
        ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
        if (currentSelection instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) currentSelection;
            @SuppressWarnings("unchecked")
            ImmutableSet<IProject> selectedProjects = FluentIterable.from(selection.toList())
                    .transform(new AdapterFunction<IProject>(IProject.class, Platform.getAdapterManager()))
                    .filter(com.google.common.base.Predicates.notNull())
                    .filter(Predicates.hasGradleNature()).toSet();
            return selectedProjects;
        } else {
            return ImmutableSet.of();
        }
    }

}
