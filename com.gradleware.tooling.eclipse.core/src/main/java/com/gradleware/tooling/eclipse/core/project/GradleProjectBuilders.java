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

package com.gradleware.tooling.eclipse.core.project;

import java.util.Arrays;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.eclipse.core.CorePlugin;

/**
 * Builder definitions which can be associated with Gradle projects.
 * <p/>
 * All entries are defined as extensions of the <code>org.eclipse.core.resources.builders</code>
 * point in the plugin.xml.
 *
 */
public enum GradleProjectBuilders {

    /**
     * The default builder for a Gradle project. Initiates project validation upon resource change.
     */
    DEFAULT_BUILDER("builder");

    private final String builderId;

    private GradleProjectBuilders(String builderId) {
        // the builder ID has to be in the following format: ${PLUGIN_ID}.${BUILDER_ID}
        this.builderId = CorePlugin.PLUGIN_ID + "." + builderId;
    }

    /**
     * Configures the builder on the target project if it was not added previously.
     *
     * @param project the target project
     */
    public void configureOnProject(IProject project) {
        try {
            Preconditions.checkState(project.isOpen());
            IProjectDescription description = project.getDescription();
            ICommand[] buildSpecs = description.getBuildSpec();
            Optional<ICommand> existingGradleBuilder = FluentIterable.from(Arrays.asList(buildSpecs)).firstMatch(new Predicate<ICommand>() {

                @Override
                public boolean apply(ICommand command) {
                    return command.getBuilderName().equals(GradleProjectBuilders.this.builderId);
                }
            });
            if (!existingGradleBuilder.isPresent()) {
                ICommand command = description.newCommand();
                command.setBuilderName(this.builderId);
                description.setBuildSpec(ImmutableList.<ICommand> builder().add(buildSpecs).add(command).build().toArray(new ICommand[0]));
                project.setDescription(description, new NullProgressMonitor());
            }

        } catch (CoreException e) {
            CorePlugin.logger().error("Failed to add GradleBuilder to project " + project.getName());
        }
    }

    /**
     * Removes the builder from the target project if it was there before.
     *
     * @param project the target project
     */
    public void deconfigureOnProject(IProject project) {
        try {
            Preconditions.checkState(project.isOpen());
            IProjectDescription description = project.getDescription();
            ICommand[] buildSpecs = description.getBuildSpec();
            ICommand[] updatedCommands = FluentIterable.from(Arrays.asList(buildSpecs)).filter(new Predicate<ICommand>() {

                @Override
                public boolean apply(ICommand command) {
                    return !command.getBuilderName().equals(GradleProjectBuilders.this.builderId);
                }
            }).toArray(ICommand.class);

            // if the builder was enabled then update the project description
            if (updatedCommands.length < buildSpecs.length) {
                description.setBuildSpec(updatedCommands);
                project.setDescription(description, new NullProgressMonitor());
            }

        } catch (CoreException e) {
            CorePlugin.logger().error("Failed to add GradleBuilder to project " + project.getName());
        }
    }

}
