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
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingmodel.OmniEclipseBuildCommand;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.buildship.core.CorePlugin;

/**
 * Updates the build commands on the target project.
 */
public final class BuildCommandUpdater {

    private static final String PROJECT_PROPERTY_KEY_GRADLE_BUILD_COMMANDS = "GRADLE_BUILD_COMMANDS";

    private final IProject project;
    private final List<OmniEclipseBuildCommand> buildCommands;

    public BuildCommandUpdater(IProject project, List<OmniEclipseBuildCommand> buildCommands) {
        this.project = Preconditions.checkNotNull(project);
        this.buildCommands = ImmutableList.copyOf(buildCommands);
    }

    private void updateBuildCommands(IProgressMonitor monitor) {
        monitor.beginTask("Updating build commands", 2);
        try {
            StringSetProjectProperty knownCommands = StringSetProjectProperty.from(project, PROJECT_PROPERTY_KEY_GRADLE_BUILD_COMMANDS);
            addNewBuildCommandsNewInGradleModel(knownCommands, new SubProgressMonitor(monitor, 1));
            removeBuildCommandsRemovedFromGradleModel(knownCommands, new SubProgressMonitor(monitor, 1));
        } catch (CoreException e) {
            CorePlugin.logger().error(String.format("Can't update build commands on %s.", project.getName()), e);
        } finally {
            monitor.done();
        }
    }

    private void addNewBuildCommandsNewInGradleModel(StringSetProjectProperty knownCommands, IProgressMonitor monitor) {
        monitor.beginTask("Add new build commands", buildCommands.size());
        try {
            for (OmniEclipseBuildCommand buildCommand : buildCommands) {
                CorePlugin.workspaceOperations().addBuildCommand(project, buildCommand.getName(), buildCommand.getArguments(), new SubProgressMonitor(monitor, 1));
                knownCommands.add(buildCommand.getName());
            }
        } finally {
            monitor.done();
        }
    }

    private void removeBuildCommandsRemovedFromGradleModel(StringSetProjectProperty knownCommands, IProgressMonitor monitor) throws CoreException {
        Set<String> buildCommands = knownCommands.get();
        monitor.beginTask("Remove old build commands", buildCommands.size());
        try {
            // iterate through build commands which were previously created by Buildship
            for (String buildCommand : buildCommands) {
                // check if the build command currently exists on the model
                if (!buildCommandExistsInGradleModel(buildCommand)) {
                    // if the build command was removed from the model then remove it from the
                    // project too
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
        List<OmniEclipseBuildCommand> builderCommands = buildCommands.isPresent() ? buildCommands.get() : Collections.<OmniEclipseBuildCommand>emptyList();
        BuildCommandUpdater updater = new BuildCommandUpdater(project, builderCommands);
        updater.updateBuildCommands(monitor);
    }

}
