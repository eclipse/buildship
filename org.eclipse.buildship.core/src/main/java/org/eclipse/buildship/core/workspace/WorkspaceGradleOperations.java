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
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.core.runtime.IProgressMonitor;

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
     * <ol>
     * <li>the Gradle resource filter is removed</li>
     * <li>the Gradle nature is removed</li>
     * <li>the Gradle settings file is removed</li>
     * </ol>
     * </li>
     * <li>Synchronize all Gradle projects of the Gradle build with the Eclipse workspace project counterparts:
     * <ul>
     * <li>
     * If there is a project in the workspace at the location of the Gradle project, the synchronization is as follows:
     * <ol>
     * <li>If the workspace project is closed, the project is left unchanged</li>
     * <li>If the workspace project is open:
     * <ul>
     * <li>the project name is updated</li>
     * <li>the Gradle nature is set</li>
     * <li>the Gradle settings file is written</li>
     * <li>the Gradle resource filter is set</li>
     * <li>the linked resources are set</li>
     * <li>the project natures and build commands are set</li>
     * <li>if the Gradle project is a Java project
     * <ul>
     * <li>the Java nature is added </li>
     * <li>the source compatibility settings are updated</li>
     * <li>the set of source folders is updated</li>
     * <li>the Gradle classpath container is updated</li>
     * </ul>
     * </li>
     * </ul>
     * </li>
     * </ol>
     * </li>
     * <li>
     * If there is an Eclipse project at the location of the Gradle project, i.e. there is a .project file in that folder, then
     * the {@link NewProjectHandler} decides whether to import it and whether to keep or overwrite that existing .project file.
     * The imported project is then synchronized as specified above.
     * </li>
     * <li>If there is no project in the workspace, nor an Eclipse project at the location of the Gradle build, then
     * the {@link NewProjectHandler} decides whether to import it.
     * The imported project is then synchronized as specified above.
     * </li>
     * </ul>
     * </li>
     * </ol>
     *
     * <p/>
     * This method changes resources. It will acquire the workspace scheduling rule to ensure an atomic operation.
     *
     * @param gradleBuild               the Gradle build to synchronize
     * @param rootRequestAttributes     the preferences used to query the Gradle build
     * @param newProjectHandler whether to keep or delete existing .project files
     * @param monitor                   the monitor to report the progress on
     */
    void synchronizeGradleBuildWithWorkspace(OmniEclipseGradleBuild gradleBuild, FixedRequestAttributes rootRequestAttributes, NewProjectHandler newProjectHandler, IProgressMonitor monitor);

}
