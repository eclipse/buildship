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

package org.eclipse.buildship.core.internal.workspace.impl;

import java.io.File;
import java.util.Set;

import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.UnsupportedConfigurationException;

/**
 * Updates project names to match the Gradle model. Moves other projects out of the way if necessary.
 */
final class ProjectNameUpdater {

    /**
     * Updates the name of the Eclipse project to match the name of the corresponding Gradle project.
     *
     * @param workspaceProject the Eclipse project whose name to update
     * @param project          the Gradle project corresponding to the Eclipse project
     * @param workspaceModel the workspace model to which the project belongs
     * @param monitor          the monitor to report progress on
     * @return the new project reference in case the project name has changed, the incoming project instance otherwise
     */
    static IProject updateProjectName(IProject workspaceProject, EclipseProject project, Set<? extends EclipseProject> allProjects, IProgressMonitor monitor) {
        String newName = checkProjectName(project);
        SubMonitor progress = SubMonitor.convert(monitor, 2);
        if (newName.equals(workspaceProject.getName())) {
            return workspaceProject;
        } else {
            ensureProjectNameIsFree(newName, allProjects, progress.newChild(1));
            return CorePlugin.workspaceOperations().renameProject(workspaceProject, newName, progress.newChild(1));
        }
    }

    /**
     * If there is already a project with the name of the given project in the workspace, we will try to move it out of the way.
     * <p/>
     * Moving the other project is possible if:
     * <p/>
     * - it is part of the same synchronize operation
     * - it has a different name in the Gradle model, so it would be renamed anyway
     * - it is not in the default location (otherwise it can't be renamed)
     * - it is open
     * <p/>
     * If any of these conditions are not met, we fail because of a name conflict.
     *
     * @param project     the project whose name is to be verified
     * @param workspaceModel the workspace model to which the project belongs
     * @param monitor     the monitor to report progress on
     */
    static void ensureProjectNameIsFree(EclipseProject project, Set<? extends EclipseProject> allProjects, IProgressMonitor monitor) {
        String name = checkProjectName(project);
        ensureProjectNameIsFree(name, allProjects, monitor);
    }

    private static void ensureProjectNameIsFree(String normalizedProjectName, Set<? extends EclipseProject> allProjects, IProgressMonitor monitor) {
        Optional<IProject> possibleDuplicate = CorePlugin.workspaceOperations().findProjectByName(normalizedProjectName);
        if (possibleDuplicate.isPresent()) {
            IProject duplicate = possibleDuplicate.get();
            if (isScheduledForRenaming(duplicate, allProjects)) {
                renameTemporarily(duplicate, monitor);
            } else {
                String message = String.format("A project with the name %s already exists.", normalizedProjectName);
                throw new UnsupportedConfigurationException(message);
            }
        }
    }

    private static boolean isScheduledForRenaming(IProject duplicate, Set<? extends EclipseProject> allProjects) {
        if (!duplicate.isOpen()) {
            return false;
        }

        Optional<? extends EclipseProject> duplicateEclipseProject = Iterables.tryFind(allProjects, eclipseProjectMatchesProjectDir(duplicate.getLocation().toFile()));
        if (!duplicateEclipseProject.isPresent()) {
            return false;
        }

        String newName = checkProjectName(duplicateEclipseProject.get());
        return !newName.equals(duplicate.getName());
    }

    private static void renameTemporarily(IProject duplicate, IProgressMonitor monitor) {
        CorePlugin.workspaceOperations().renameProject(duplicate, duplicate.getName() + "-" + duplicate.getName().hashCode(), monitor);
    }

    private static String checkProjectName(EclipseProject project) {
        CorePlugin.workspaceOperations().validateProjectName(project.getName(), project.getProjectDirectory());
        return project.getName();
    }

    private static Predicate<EclipseProject> eclipseProjectMatchesProjectDir(final File projectDir) {
        return new Predicate<EclipseProject>() {

            @Override
            public boolean apply(EclipseProject candidate) {
                return candidate.getProjectDirectory().equals(projectDir);
            }
        };
    }

}
