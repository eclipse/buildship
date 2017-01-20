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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;

import com.gradleware.tooling.toolingmodel.OmniEclipseBuildCommand;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.configuration.GradleProjectBuilder;

/**
 * Updates the build commands on the target project.
 */
final class BuildCommandUpdater {

    public static void update(IProject project, Optional<List<OmniEclipseBuildCommand>> buildCommands, IProgressMonitor monitor) throws CoreException {
        IProjectDescription description = project.getDescription();
        Set<ICommand> commands = toCommands(buildCommands, description);
        description.setBuildSpec(commands.toArray(new ICommand[0]));
        project.setDescription(description, monitor);
    }

    private static Set<ICommand> toCommands(Optional<List<OmniEclipseBuildCommand>> buildCommands, IProjectDescription description) {
        Set<ICommand> commands = Sets.newLinkedHashSet();
        if (buildCommands.isPresent()) {
            commands.addAll(toCommands(buildCommands.get(), description));
        } else {
            commands.addAll(Arrays.asList(description.getBuildSpec()));
        }
        commands.add(toCommand(description, GradleProjectBuilder.ID, Collections.<String, String>emptyMap()));
        return commands;
    }

    private static Set<? extends ICommand> toCommands(List<OmniEclipseBuildCommand> buildCommands, IProjectDescription description) {
        Set<ICommand> result = Sets.newLinkedHashSet();
        for (OmniEclipseBuildCommand buildCommand : buildCommands) {
            result.add(toCommand(description, buildCommand.getName(), buildCommand.getArguments()));
        }
        return result;
    }

    private static ICommand toCommand(IProjectDescription description, String name, Map<String, String> arguments) {
        ICommand command = description.newCommand();
        command.setBuilderName(name);
        command.setArguments(arguments);
        return command;
    }
}
