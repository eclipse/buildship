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

package org.eclipse.buildship.core.internal.configuration;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.buildship.core.internal.CorePlugin;

/**
 * Project builder for Gradle projects.
 */
public final class GradleProjectBuilder {

    // the builder ID has to be in the following format: ${PLUGIN_ID}.${BUILDER_ID}
    public static final String ID = CorePlugin.PLUGIN_ID + ".gradleprojectbuilder";

    /**
     * Configures the builder on the target project if it was not added previously.
     * <p/>
     * This method requires the {@link org.eclipse.core.resources.IWorkspaceRoot} scheduling rule.
     *
     * @param project the target project
     */
    public static void configureOnProject(IProject project) {
        try {
            Preconditions.checkState(project.isOpen());

            // check if the builder is already registered with the project
            IProjectDescription description = project.getDescription();
            List<ICommand> buildSpecs = Arrays.asList(description.getBuildSpec());
            boolean exists = FluentIterable.from(buildSpecs).anyMatch(new Predicate<ICommand>() {

                @Override
                public boolean apply(ICommand command) {
                    return command.getBuilderName().equals(ID);
                }
            });

            // register the builder with the project if it is not already registered
            if (!exists) {
                ICommand buildSpec = description.newCommand();
                buildSpec.setBuilderName(ID);
                ImmutableList<ICommand> newBuildSpecs = ImmutableList.<ICommand>builder().addAll(buildSpecs).add(buildSpec).build();
                description.setBuildSpec(newBuildSpecs.toArray(new ICommand[newBuildSpecs.size()]));
                project.setDescription(description, new NullProgressMonitor());
            }
        } catch (CoreException e) {
            CorePlugin.logger().error(String.format("Failed to add Gradle Project Builder to project %s.", project.getName()));
        }
    }

    /**
     * Removes the builder from the target project if it was there before.
     * <p/>
     * This method requires the {@link org.eclipse.core.resources.IWorkspaceRoot} scheduling rule.
     *
     * @param project the target project
     */
    public static void deconfigureOnProject(IProject project) {
        try {
            Preconditions.checkState(project.isOpen());

            // remove the builder if it is registered with the project
            IProjectDescription description = project.getDescription();
            List<ICommand> buildSpecs = Arrays.asList(description.getBuildSpec());
            ICommand[] filteredBuildSpecs = FluentIterable.from(buildSpecs).filter(new Predicate<ICommand>() {

                @Override
                public boolean apply(ICommand command) {
                    return !command.getBuilderName().equals(ID);
                }
            }).toArray(ICommand.class);

            // if the builder was previously registered, unregister the builder form the project
            if (filteredBuildSpecs.length < buildSpecs.size()) {
                description.setBuildSpec(filteredBuildSpecs);
                project.setDescription(description, new NullProgressMonitor());
            }
        } catch (CoreException e) {
            CorePlugin.logger().error(String.format("Failed to remove Gradle Project Builder from project %s.", project.getName()));
        }
    }

    private GradleProjectBuilder() {
    }
    
}
