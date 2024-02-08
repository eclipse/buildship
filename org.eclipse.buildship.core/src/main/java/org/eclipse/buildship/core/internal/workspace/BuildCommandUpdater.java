/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gradle.tooling.model.eclipse.EclipseBuildCommand;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.internal.configuration.GradleProjectBuilder;
import org.eclipse.buildship.core.internal.preferences.PersistentModel;
import org.eclipse.buildship.core.internal.workspace.ManagedModelMergingStrategy.Result;

/**
 * Updates the build commands on the target project.
 */
final class BuildCommandUpdater {

    public static void update(IProject project, List<EclipseBuildCommand> buildCommands, PersistentModelBuilder persistentModel, IProgressMonitor monitor) throws CoreException {
        IProjectDescription description = project.getDescription();

        Set<ICommand> current = ImmutableSet.copyOf(description.getBuildSpec());
        Set<ICommand> model = toCommands(buildCommands, description);
        PersistentModel previousModel = persistentModel.getPrevious();
        Set<ICommand> managed = previousModel.isPresent() ? Sets.newLinkedHashSet(previousModel.getManagedBuilders()) : Sets.<ICommand>newLinkedHashSet();

        Result<ICommand> result = ManagedModelMergingStrategy.calculate(current, model, managed);
        description.setBuildSpec(result.getNextElements().toArray(new ICommand[0]));
        project.setDescription(description, monitor);
        persistentModel.managedBuilders(result.getNextManaged());
    }

    private static Set<ICommand> toCommands(List<EclipseBuildCommand> buildCommands, IProjectDescription description) {
        Set<ICommand> commands = Sets.newLinkedHashSet();
        for (EclipseBuildCommand buildCommand : buildCommands) {
            commands.add(toCommand(buildCommand.getName(), buildCommand.getArguments(), description));
        }
        commands.add(toCommand(GradleProjectBuilder.ID, Collections.<String, String>emptyMap(), description));
        return commands;
    }

    private static ICommand toCommand(String name, Map<String, String> arguments, IProjectDescription description) {
        ICommand command = description.newCommand();
        command.setBuilderName(name);
        command.setArguments(arguments);
        return command;
    }
}
