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
     * Synchronizes the given Gradle build with the Eclipse workspace. The algorithm is as follows:
     * <p/>
     * <ol>
     * <li>Uncouple all open workspace projects for which there is no corresponding Gradle project in the Gradle build anymore
     * <ul>
     * <li>As outlined in {@link #makeWorkspaceProjectGradleUnaware(IProject, IProgressMonitor)}</li>
     * </ul>
     * </li>
     * <li>Synchronize all Gradle projects of the Gradle build with the Eclipse workspace project counterparts:
     * <ul>
     * <li>As outlined in {@link #synchronizeGradleProjectWithWorkspaceProject(OmniEclipseProject, OmniEclipseGradleBuild, FixedRequestAttributes, List, IProgressMonitor)}</li>
     * </ul>
     * </li>
     * </ol>
     *
     * @param gradleBuild           the Gradle build to synchronize
     * @param rootRequestAttributes the preferences used to query the Gradle build
     * @param workingSets           the working set to assign the imported projects to
     * @param monitor               the monitor to report the progress on
     */
    void synchronizeGradleBuildWithWorkspaceProject(OmniEclipseGradleBuild gradleBuild, FixedRequestAttributes rootRequestAttributes, List<String> workingSets, IProgressMonitor monitor);

    /**
     * Synchronizes the given Gradle project with its Eclipse workspace project counterpart. The algorithm is as follows:
     * <p/>
     * <ol>
     * <li>
     * If there is a project in the workspace at the location of the Gradle project, the synchronization is as follows:
     * <ol>
     * <li>If the workspace project is closed, the project is left unchanged</li>
     * <li>If the workspace project is open: ????
     * <ul>
     * <li>the Gradle nature is set</li>
     * <li>the Gradle settings file is written</li>
     * <li>the Gradle resource filter is set</li>
     * <li>the linked resources are set</li>
     * <li>a Java project is set ?????</li>
     * </ul>
     * </li>
     * </ol>
     * </li>
     * <li>
     * If there is an Eclipse project at the location of the Gradle project, i.e. there is a .project file in that folder, the synchronization is as follows:
     * <ul>
     * <li>the Eclipse project is added to the workspace</li>
     * <li>the Gradle nature is set</li>
     * <li>the Gradle settings file is written</li>
     * </ul>
     * </li>
     * <li>If the there is no project in the workspace nor an Eclipse project at the location of the Gradle build, the synchronization is as follows:
     * <ul>
     * <li>an Eclipse project is created and added to the workspace</li>
     * <li>the Gradle nature is set</li>
     * <li>the Gradle settings file is written</li>
     * <li>the Gradle resource filter is set</li>
     * <li>the linked resources are set</li>
     * <li>a Java project is set in case of a Java Gradle project (which triggers a synchronize through the classpath container, handled by #1)</li>
     * </ul>
     * </li>
     * </ol>
     *
     * @param project               the backing Gradle project
     * @param gradleBuild           the Gradle build to which the Gradle project belongs
     * @param rootRequestAttributes the preferences used to query the Gradle build
     * @param workingSets           the working set to assign the imported projects to
     * @param monitor               the monitor to report the progress on
     */
    void synchronizeGradleProjectWithWorkspaceProject(OmniEclipseProject project, OmniEclipseGradleBuild gradleBuild, FixedRequestAttributes rootRequestAttributes, List<String> workingSets, IProgressMonitor monitor);

    /**
     * Removes all Gradle specific parts from the given project.
     *
     * @param workspaceProject the project from which to remove all Gradle specific parts
     * @param monitor          the monitor to report the progress on
     */
    void makeWorkspaceProjectGradleUnaware(IProject workspaceProject, IProgressMonitor monitor);

}
