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

package org.eclipse.buildship.core.workspace;

import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import java.util.List;

/**
 * Provides operations related to querying and modifying the Gradle specific parts of
 * Eclipse elements that exist in a workspace.
 */
public interface WorkspaceGradleOperations {

    /**
     * Either creates a new, Gradle-aware project and attaches it to the workspace or, if an Eclipse project already
     * exists at the given location, attaches the found project to the workspace. An existing Eclipse project is
     * attached unchanged, only the {@link org.eclipse.buildship.core.configuration.GradleProjectNature} is assigned to it and some resources filters are
     * applied. Otherwise the project is fully populated from the model.
     *
     * @param project         the Gradle project to attach as an Eclipse project
     * @param gradleBuild     the Gradle build to which the Gradle project belongs
     * @param fixedAttributes the preferences used to query the models
     * @param workingSets     the working set to assign the imported projects to
     * @param monitor         the monitor to report the progress on
     * @throws IllegalStateException thrown if there is a project at the given location that is already attached to the workspace
     */
    void attachNewGradleAwareProjectOrExistingProjectToWorkspace(OmniEclipseProject project, OmniEclipseGradleBuild gradleBuild, FixedRequestAttributes fixedAttributes, List<String> workingSets, IProgressMonitor monitor);

    /**
     * Updates the Gradle specific parts of the given project.
     *
     * @param project               the backing Gradle project
     * @param gradleBuild           the Gradle build to which the Gradle project belongs
     * @param rootRequestAttributes the request attributes of the root project
     * @param monitor               the monitor to report the progress on
     */
    void updateProjectInWorkspace(OmniEclipseProject project, OmniEclipseGradleBuild gradleBuild, FixedRequestAttributes rootRequestAttributes, IProgressMonitor monitor);

    /**
     * Removes all Gradle specific parts from the given project.
     *
     * @param workspaceProject the project from which to remove all Gradle specific parts
     * @param monitor          the monitor to report the progress on
     */
    void makeProjectGradleUnaware(IProject workspaceProject, IProgressMonitor monitor);

}
