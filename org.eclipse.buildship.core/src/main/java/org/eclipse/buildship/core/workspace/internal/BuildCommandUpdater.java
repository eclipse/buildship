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
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingmodel.OmniEclipseBuildCommand;
import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Updates the build commands on the target project.
 */
final class BuildCommandUpdater {

    private static final String PROJECT_PROPERTY_KEY_GRADLE_BUILD_COMMANDS = "GRADLE_BUILD_COMMANDS";

    private final IProject project;
    private final ImmutableList<OmniEclipseBuildCommand> buildCommands;

    public BuildCommandUpdater(IProject project, List<OmniEclipseBuildCommand> buildCommands) {
        this.project = Preconditions.checkNotNull(project);
        this.buildCommands = ImmutableList.copyOf(buildCommands);
    }

    private void updateBuildCommands(IProgressMonitor monitor) {
        monitor.beginTask("Updating build commands", 2);
        try {
            StringSetProjectProperty knownCommands = StringSetProjectProperty.from(project, PROJECT_PROPERTY_KEY_GRADLE_BUILD_COMMANDS);
            addBuildCommandsNewInGradleModel(knownCommands, new SubProgressMonitor(monitor, 1));
            removeBuildCommandsRemovedFromGradleModel(knownCommands, new SubProgressMonitor(monitor, 1));
        } catch (CoreException e) {
            CorePlugin.logger().error(String.format("Cannot update build commands on %s.", project.getName()), e);
        } finally {
            monitor.done();
        }
    }

    private void addBuildCommandsNewInGradleModel(StringSetProjectProperty knownCommands, IProgressMonitor monitor) {
        monitor.beginTask("Add new build commands", buildCommands.size());
        try {
            for (OmniEclipseBuildCommand buildCommand : buildCommands) {
                String name = buildCommand.getName();
                Map<String, String> arguments = buildCommand.getArguments();
                CorePlugin.workspaceOperations().addBuildCommand(project, name, arguments, new SubProgressMonitor(monitor, 1));
                knownCommands.add(name);
            }
        } finally {
            monitor.done();
        }
    }

    private void removeBuildCommandsRemovedFromGradleModel(StringSetProjectProperty knownCommands, IProgressMonitor monitor) throws CoreException {
        Set<String> buildCommands = knownCommands.get();
        monitor.beginTask("Remove old build commands", buildCommands.size());
        try {
            for (String buildCommand : buildCommands) {
                if (!buildCommandExistsInGradleModel(buildCommand)) {
                    CorePlugin.workspaceOperations().removeBuildCommand(project, buildCommand, new SubProgressMonitor(monitor, 1));
                    knownCommands.remove(buildCommand);
                } else {
                    monitor.worked(1);
                }
            }
        } finally {
            monitor.done();
        }
    }

    private boolean buildCommandExistsInGradleModel(final String buildCommandName) {
        return FluentIterable.from(buildCommands).firstMatch(new Predicate<OmniEclipseBuildCommand>() {

            @Override
            public boolean apply(OmniEclipseBuildCommand command) {
                return command.getName().equals(buildCommandName);
            }
        }).isPresent();
    }

    public static void update(IProject project, Optional<List<OmniEclipseBuildCommand>> buildCommands, IProgressMonitor monitor) throws CoreException {
        List<OmniEclipseBuildCommand> builderCommands = buildCommands.or(Collections.<OmniEclipseBuildCommand>emptyList());
        BuildCommandUpdater updater = new BuildCommandUpdater(project, builderCommands);
        updater.updateBuildCommands(monitor);
    }

}
