/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.core.util.progress.ToolingApiWorkspaceJob;

/**
 * {@link ToolingApiWorkspaceJob}, which converts an {@link IProject} to a Gradle project.
 *
 * @see ProjectService
 */
public class ConvertToGradleProjectJob extends ToolingApiWorkspaceJob {

    private IProject project;
    private GradleRunConfigurationAttributes configurationAttributes;

    public ConvertToGradleProjectJob(GradleRunConfigurationAttributes configurationAttributes, IProject project) {
        super("Convert to Gradle Project");
        this.configurationAttributes = configurationAttributes;
        this.project = project;
    }

    @Override
    protected void runToolingApiJobInWorkspace(IProgressMonitor monitor) throws Exception {
        CorePlugin.projectService().convertToGradleProject(monitor, this.configurationAttributes, this.project);
        monitor.done();
    }

}
