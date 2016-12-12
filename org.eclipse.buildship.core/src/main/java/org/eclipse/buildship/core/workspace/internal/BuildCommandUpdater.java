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

import java.util.List;

import com.google.common.base.Optional;

import com.gradleware.tooling.toolingmodel.OmniEclipseBuildCommand;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Updates the build commands on the target project.
 */
final class BuildCommandUpdater {

    public static void update(IProject project, Optional<List<OmniEclipseBuildCommand>> buildCommands, IProgressMonitor monitor) throws CoreException {
        if (buildCommands.isPresent()) {
            update(project, buildCommands.get(), monitor);
        }
    }

    private static void update(IProject project, List<OmniEclipseBuildCommand> buildCommands, IProgressMonitor monitor) throws CoreException {
        IProjectDescription description = project.getDescription();
        description.setBuildSpec(toCommands(buildCommands, description));
        project.setDescription(description, monitor);
    }

    private static ICommand[] toCommands(List<OmniEclipseBuildCommand> buildCommands, IProjectDescription description) {
        ICommand[] commands = new ICommand[buildCommands.size()];
        for (int i = 0; i < buildCommands.size(); i++) {
            OmniEclipseBuildCommand buildCommand = buildCommands.get(i);
            commands[i] = toCommand(buildCommand, description);
        }
        return commands;
    }

    private static ICommand toCommand(OmniEclipseBuildCommand buildCommand, IProjectDescription description) {
        ICommand command = description.newCommand();
        command.setBuilderName(buildCommand.getName());
        command.setArguments(buildCommand.getArguments());
        return command;
    }

}
