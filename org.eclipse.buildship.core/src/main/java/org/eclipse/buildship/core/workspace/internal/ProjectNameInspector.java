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

package org.eclipse.buildship.core.workspace.internal;

import com.google.common.base.Optional;
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.gradle.Specs;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * Inspects and tries to ensure certain constraints related to project names.
 */
final class ProjectNameInspector {

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
     * @param gradleBuild the build to which the project belongs
     * @param monitor     the monitor to report progress on
     */
    static void ensureProjectNameIsFree(OmniEclipseProject project, OmniEclipseGradleBuild gradleBuild, IProgressMonitor monitor) {
        String name = normalizeProjectName(project);
        ensureProjectNameIsFree(name, gradleBuild, monitor);
    }

    /**
     * If there is already a project with the desired name in the workspace, we will try to move it out of the way.
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
     * @param normalizedProjectName the normalized name of the project
     * @param gradleBuild           the build to which the project belongs
     * @param monitor               the monitor to report progress on
     */
    static void ensureProjectNameIsFree(String normalizedProjectName, OmniEclipseGradleBuild gradleBuild, IProgressMonitor monitor) {
        monitor.beginTask(String.format("Ensure project name '%s' is free", normalizedProjectName), 1);
        try {
            Optional<IProject> possibleDuplicate = CorePlugin.workspaceOperations().findProjectByName(normalizedProjectName);
            if (possibleDuplicate.isPresent()) {
                IProject duplicate = possibleDuplicate.get();
                if (isScheduledForRenaming(duplicate, gradleBuild)) {
                    renameTemporarily(duplicate, new SubProgressMonitor(monitor, 1));
                } else {
                    String message = String.format("A project with the name %s already exists.", normalizedProjectName);
                    throw new GradlePluginsRuntimeException(message);
                }
            }
        } finally {
            monitor.done();
        }
    }

    private static boolean isScheduledForRenaming(IProject duplicate, OmniEclipseGradleBuild gradleBuild) {
        if (!duplicate.isOpen()) {
            return false;
        }

        Optional<OmniEclipseProject> duplicateEclipseProject = gradleBuild.getRootEclipseProject().tryFind(Specs.eclipseProjectMatchesProjectDir(duplicate.getLocation().toFile()));
        if (!duplicateEclipseProject.isPresent()) {
            return false;
        }

        String newName = normalizeProjectName(duplicateEclipseProject.get());
        return !newName.equals(duplicate.getName());
    }

    private static void renameTemporarily(IProject duplicate, IProgressMonitor monitor) {
        CorePlugin.workspaceOperations().renameProject(duplicate, duplicate.getName() + "-" + duplicate.getName().hashCode(), monitor);
    }

    private static String normalizeProjectName(OmniEclipseProject project) {
        return CorePlugin.workspaceOperations().normalizeProjectName(project.getName(), project.getProjectDirectory());
    }

}
