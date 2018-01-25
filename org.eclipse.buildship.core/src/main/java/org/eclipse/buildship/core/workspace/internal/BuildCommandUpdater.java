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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.configuration.GradleProjectBuilder;
import org.eclipse.buildship.core.omnimodel.OmniEclipseBuildCommand;
import org.eclipse.buildship.core.preferences.PersistentModel;
import org.eclipse.buildship.core.workspace.internal.ManagedModelMergingStrategy.Result;

/**
 * Updates the build commands on the target project.
 */
final class BuildCommandUpdater {

    public static void update(IProject project, Optional<List<OmniEclipseBuildCommand>> buildCommands, PersistentModelBuilder persistentModel, IProgressMonitor monitor) throws CoreException {
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

    private static Set<ICommand> toCommands(Optional<List<OmniEclipseBuildCommand>> buildCommands, IProjectDescription description) {
        Set<ICommand> commands = Sets.newLinkedHashSet();
        if (buildCommands.isPresent()) {
            commands.addAll(toCommands(buildCommands.get(), description));
        }
        commands.add(toCommand(GradleProjectBuilder.ID, Collections.<String, String>emptyMap(), description));
        return commands;
    }

    private static Set<? extends ICommand> toCommands(List<OmniEclipseBuildCommand> buildCommands, IProjectDescription description) {
        Set<ICommand> result = Sets.newLinkedHashSet();
        for (OmniEclipseBuildCommand buildCommand : buildCommands) {
            result.add(toCommand(buildCommand.getName(), buildCommand.getArguments(), description));
        }
        return result;
    }

    private static ICommand toCommand(String name, Map<String, String> arguments, IProjectDescription description) {
        ICommand command = description.newCommand();
        command.setBuilderName(name);
        command.setArguments(arguments);
        return command;
    }
}
