/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.gradleprop.test;

import java.io.File;

import org.junit.Before;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

public abstract class WorkspaceSpecification {

    @Before
    public void deleteAllProjects() {
        for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
             try {
                 project.delete(true, true, null);
            } catch(CoreException e) {
               throw new RuntimeException("Cannot delete test project " + project.getName(), e);
            }
        }
    }

    public static IProject createProject(String name) {
        return createProject(name, null);
    }

    public static IProject createProject(String name, File location) {
        try {
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IProjectDescription projectDescription = workspace.newProjectDescription(name);
            projectDescription.setLocation(location == null ? null : new Path(location.getAbsolutePath()));
            IProject project = workspace.getRoot().getProject(name);
            project.create(projectDescription, null);
            project.open(null);
            return project;
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }
}
