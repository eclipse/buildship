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

package org.eclipse.buildship.core.workspace;

import java.io.File;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniGradleProject;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.util.Maybe;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.launching.JavaRuntime;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.gradle.Specs;
import org.eclipse.buildship.core.workspace.internal.DefaultProjectCreatedEvent;

/**
 * Imports a Gradle project as an Eclipse project into the current workspace.
 */
public final class WorkspaceProjectModifier {

    private WorkspaceProjectModifier() {
    }

    /**
     * Imports a project into the workspace.
     * <p/>
     * If a model defines a project location which already contains an Eclipse project then the
     * project is imported unchanged, only the {@link GradleProjectNature} is assigned to it and some
     * resources filters are applied. Otherwise the project is fully populated from the model.
     *
     * @param gradleProject the model defining the project to import
     * @param gradleBuild the container of the model
     * @param fixedAttributes the preferences used to query the models
     * @param workingSets the working set to assign the imported projects to
     * @param monitor the monitor to report the progress on
     * @throws IllegalStateException thrown if the project already exists in the workspace
     */
    public static void attachNewOrExistingProjectToWorkspace(OmniEclipseProject gradleProject, OmniEclipseGradleBuild gradleBuild, FixedRequestAttributes fixedAttributes, List<String> workingSets, IProgressMonitor monitor) {
        monitor.beginTask("Import project " + gradleProject.getName(), 3);
        try {
            // check if an Eclipse project already exists at the location of the Gradle project to import
            WorkspaceOperations workspaceOperations = CorePlugin.workspaceOperations();
            File projectDirectory = gradleProject.getProjectDirectory();
            Optional<IProjectDescription> projectDescription = workspaceOperations.findProjectInFolder(projectDirectory, new SubProgressMonitor(monitor, 1));

            // collect all the sub folders to hide under the project
            List<File> filteredSubFolders = ImmutableList.<File>builder().
                    addAll(collectChildProjectLocations(gradleProject)).
                    add(getBuildDirectory(gradleBuild, gradleProject)).
                    add(getDotGradleDirectory(gradleProject)).build();
            ImmutableList<String> gradleNature = ImmutableList.of(GradleProjectNature.ID);

            IProject workspaceProject;
            if (projectDescription.isPresent()) {
                // include the existing Eclipse project in the workspace
                workspaceProject = workspaceOperations.includeProject(projectDescription.get(), filteredSubFolders, gradleNature, new SubProgressMonitor(monitor, 2));
            } else {
                // create a new Eclipse project in the workspace for the current Gradle project
                workspaceProject = workspaceOperations.createProject(gradleProject.getName(), gradleProject.getProjectDirectory(), filteredSubFolders, gradleNature, new SubProgressMonitor(monitor, 1));

                // if the current Gradle project is a Java project, configure the Java nature, the classpath, and the source paths
                if (isJavaProject(gradleProject)) {
                    IPath jrePath = JavaRuntime.getDefaultJREContainerEntry().getPath();
                    workspaceOperations.createJavaProject(workspaceProject, jrePath, new SubProgressMonitor(monitor, 1));
                } else {
                    monitor.worked(1);
                }
            }

            // persist the Gradle-specific configuration in the Eclipse project's .settings folder
            ProjectConfiguration projectConfiguration = ProjectConfiguration.from(fixedAttributes, gradleProject);
            CorePlugin.projectConfigurationManager().saveProjectConfiguration(projectConfiguration, workspaceProject);

            // notify the listeners that a new IProject has been created
            ProjectCreatedEvent event = new DefaultProjectCreatedEvent(workspaceProject, workingSets);
            CorePlugin.listenerRegistry().dispatch(event);
        } finally {
            monitor.done();
        }
    }

    private static List<File> collectChildProjectLocations(OmniEclipseProject project) {
        return FluentIterable.from(project.getChildren()).transform(new Function<OmniEclipseProject, File>() {

            @Override
            public File apply(OmniEclipseProject project) {
                return project.getProjectDirectory();
            }
        }).toList();
    }

    private static File getBuildDirectory(OmniEclipseGradleBuild eclipseGradleBuild, OmniEclipseProject project) {
        Optional<OmniGradleProject> gradleProject = eclipseGradleBuild.getRootProject().tryFind(Specs.gradleProjectMatchesProjectPath(project.getPath()));
        Maybe<File> buildScript = gradleProject.get().getBuildDirectory();
        if (buildScript.isPresent() && buildScript.get() != null) {
            return buildScript.get();
        } else {
            return new File(project.getProjectDirectory(), "build");
        }
    }

    private static File getDotGradleDirectory(OmniEclipseProject project) {
        return new File(project.getProjectDirectory(), ".gradle");
    }

    private static boolean isJavaProject(OmniEclipseProject modelProject) {
        return !modelProject.getSourceDirectories().isEmpty();
    }

}
