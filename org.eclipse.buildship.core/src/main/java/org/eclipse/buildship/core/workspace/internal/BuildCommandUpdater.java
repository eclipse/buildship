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
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import com.gradleware.tooling.toolingmodel.OmniEclipseBuildCommand;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import org.eclipse.buildship.core.CorePlugin;

/**
 * Updates the build commands on the target project.
 */
final class BuildCommandUpdater {

    private static final String PROJECT_PROPERTY_KEY_GRADLE_BUILD_COMMANDS = "build.commands";

    private final IProject project;
    private final ImmutableList<OmniEclipseBuildCommand> buildCommands;

    public BuildCommandUpdater(IProject project, List<OmniEclipseBuildCommand> buildCommands) {
        this.project = Preconditions.checkNotNull(project);
        this.buildCommands = ImmutableList.copyOf(buildCommands);
    }

    private void updateBuildCommands(IProgressMonitor monitor) {
        SubMonitor progress = SubMonitor.convert(monitor, 2);
        StringSetProjectProperty knownCommands = StringSetProjectProperty.from(this.project, PROJECT_PROPERTY_KEY_GRADLE_BUILD_COMMANDS);
        removeBuildCommandsRemovedFromGradleModel(knownCommands, progress.newChild(1));
        addBuildCommandsNewInGradleModel(knownCommands, progress.newChild(1));
    }

    private void addBuildCommandsNewInGradleModel(StringSetProjectProperty knownCommands, SubMonitor progress) {
        progress.setWorkRemaining(this.buildCommands.size());
        Set<String> newCommandNames = Sets.newLinkedHashSet();
        for (OmniEclipseBuildCommand buildCommand : this.buildCommands) {
            String name = buildCommand.getName();
            Map<String, String> arguments = buildCommand.getArguments();
            CorePlugin.workspaceOperations().addBuildCommand(this.project, name, arguments, progress.newChild(1));
            newCommandNames.add(name);
        }
        knownCommands.set(newCommandNames);
    }

    private void removeBuildCommandsRemovedFromGradleModel(StringSetProjectProperty knownCommands, SubMonitor progress) {
        Set<String> buildCommands = knownCommands.get();
        progress.setWorkRemaining(buildCommands.size());
        for (String buildCommand : buildCommands) {
            SubMonitor childProgress = progress.newChild(1);
            if (!buildCommandExistsInGradleModel(buildCommand)) {
                CorePlugin.workspaceOperations().removeBuildCommand(this.project, buildCommand, childProgress);
            }
        }
    }

    private boolean buildCommandExistsInGradleModel(final String buildCommandName) {
        return FluentIterable.from(this.buildCommands).firstMatch(new Predicate<OmniEclipseBuildCommand>() {

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
