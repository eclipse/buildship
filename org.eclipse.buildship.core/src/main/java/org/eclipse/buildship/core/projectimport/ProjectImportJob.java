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

package org.eclipse.buildship.core.projectimport;

import java.io.File;
import java.util.List;

import org.gradle.tooling.ProgressListener;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniEclipseProjectDependency;
import com.gradleware.tooling.toolingmodel.OmniEclipseSourceDirectory;
import com.gradleware.tooling.toolingmodel.OmniExternalDependency;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.ModelRepository;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;
import com.gradleware.tooling.toolingmodel.util.Pair;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.launching.JavaRuntime;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.gradle.Specs;
import org.eclipse.buildship.core.util.progress.DelegatingProgressListener;
import org.eclipse.buildship.core.util.progress.ToolingApiWorkspaceJob;
import org.eclipse.buildship.core.workspace.ClasspathDefinition;
import org.eclipse.buildship.core.workspace.WorkspaceOperations;

/**
 * Imports a Gradle project into Eclipse using the project import coordinates given by a
 * {@code ProjectImportConfiguration} instance.
 */
public final class ProjectImportJob extends ToolingApiWorkspaceJob {

    private final FixedRequestAttributes fixedAttributes;

    public ProjectImportJob(ProjectImportConfiguration configuration) {
        super("Importing Gradle project");

        this.fixedAttributes = configuration.toFixedAttributes();

        // explicitly show a dialog with the progress while the import is in process
        setUser(true);
    }

    @Override
    public void runToolingApiJobInWorkspace(IProgressMonitor monitor) {
        monitor.beginTask("Import Gradle project", 100);

        OmniEclipseGradleBuild eclipseGradleBuild = fetchEclipseGradleBuild(new SubProgressMonitor(monitor, 50));
        OmniEclipseProject rootProject = eclipseGradleBuild.getRootEclipseProject();
        List<OmniEclipseProject> allProjects = rootProject.getAll();
        for (OmniEclipseProject project : allProjects) {
            importProject(project, rootProject, new SubProgressMonitor(monitor, 50 / allProjects.size()));
        }
    }

    private OmniEclipseGradleBuild fetchEclipseGradleBuild(IProgressMonitor monitor) {
        monitor.beginTask("Load Eclipse Gradle project", IProgressMonitor.UNKNOWN);
        try {
            ProcessStreams streams = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
            List<ProgressListener> listeners = ImmutableList.<ProgressListener>of(new DelegatingProgressListener(monitor));
            TransientRequestAttributes transientAttributes = new TransientRequestAttributes(false, streams.getOutput(), streams.getError(), null, listeners,
                    ImmutableList.<org.gradle.tooling.events.ProgressListener>of(), getToken());
            ModelRepository repository = CorePlugin.modelRepositoryProvider().getModelRepository(this.fixedAttributes);
            return repository.fetchEclipseGradleBuild(transientAttributes, FetchStrategy.FORCE_RELOAD);
        } finally {
            monitor.done();
        }
    }

    private void importProject(OmniEclipseProject project, OmniEclipseProject rootProject, IProgressMonitor monitor) {
        monitor.beginTask("Import project " + project.getName(), 4);
        try {
            // check if an Eclipse project already exists at the location of the Gradle project to import
            WorkspaceOperations workspaceOperations = CorePlugin.workspaceOperations();
            File projectDirectory = project.getProjectDirectory();
            Optional<IProjectDescription> projectDescription = workspaceOperations.findProjectInFolder(projectDirectory, new SubProgressMonitor(monitor, 1));

            List<File> childProjectLocations = collectChildProjectLocations(project);
            ImmutableList<String> gradleNature = ImmutableList.of(GradleProjectNature.ID);

            IProject workspaceProject;
            if (projectDescription.isPresent()) {
                // include the existing Eclipse project in the workspace
                workspaceProject = workspaceOperations.includeProject(projectDescription.get(), childProjectLocations, gradleNature,
                        new SubProgressMonitor(monitor, 2));
            } else {
                // create a new Eclipse project in the workspace for the current Gradle project
                workspaceProject = workspaceOperations.createProject(project.getName(), project.getProjectDirectory(), childProjectLocations, gradleNature,
                        new SubProgressMonitor(monitor, 1));

                // if the current Gradle project is a Java project, configure the Java nature,
                // the classpath, and the source paths
                if (isJavaProject(project)) {
                    ClasspathDefinition classpath = collectClasspath(project, rootProject);
                    workspaceOperations.createJavaProject(workspaceProject, classpath, new SubProgressMonitor(monitor, 1));
                } else {
                    monitor.worked(1);
                }
            }

            // persist the Gradle-specific configuration in the Eclipse project's .settings folder
            ProjectConfiguration projectConfiguration = ProjectConfiguration.from(this.fixedAttributes, project);
            CorePlugin.projectConfigurationManager().saveProjectConfiguration(projectConfiguration, workspaceProject);

            // refresh the project content
            workspaceOperations.refresh(workspaceProject, new SubProgressMonitor(monitor, 1));
        } finally {
            monitor.done();
        }
    }

    private boolean isJavaProject(OmniEclipseProject modelProject) {
        return !modelProject.getSourceDirectories().isEmpty();
    }

    private List<File> collectChildProjectLocations(OmniEclipseProject project) {
        return FluentIterable.from(project.getChildren()).transform(new Function<OmniEclipseProject, File>() {

            @Override
            public File apply(OmniEclipseProject project) {
                return project.getProjectDirectory();
            }
        }).toList();
    }

    private ClasspathDefinition collectClasspath(OmniEclipseProject project, OmniEclipseProject rootProject) {
        List<Pair<IPath, IPath>> jars = collectJarDependencies(project);
        List<IPath> projects = collectProjectDependencies(project, rootProject);
        List<String> sources = collectSourceDirectories(project);
        IPath jre = collectDefaultJreLocation();
        return new ClasspathDefinition(jars, projects, sources, jre);
    }

    private ImmutableList<Pair<IPath, IPath>> collectJarDependencies(OmniEclipseProject project) {
        return FluentIterable.from(project.getExternalDependencies()).transform(new Function<OmniExternalDependency, Pair<IPath, IPath>>() {

            @Override
            public Pair<IPath, IPath> apply(OmniExternalDependency dependency) {
                IPath jar = Path.fromOSString(dependency.getFile().getAbsolutePath());
                IPath sourceJar = dependency.getSource() != null ? Path.fromOSString(dependency.getSource().getAbsolutePath()) : null;
                return new Pair<IPath, IPath>(jar, sourceJar);
            }
        }).toList();
    }

    private ImmutableList<IPath> collectProjectDependencies(OmniEclipseProject project, final OmniEclipseProject rootProject) {
        return FluentIterable.from(project.getProjectDependencies()).transform(new Function<OmniEclipseProjectDependency, IPath>() {

            @Override
            public IPath apply(OmniEclipseProjectDependency dependency) {
                OmniEclipseProject dependentProject = rootProject.tryFind(Specs.eclipseProjectMatchesProjectPath(dependency.getTargetProjectPath())).get();
                return new Path("/" + dependentProject.getName());
            }
        }).toList();
    }

    private ImmutableList<String> collectSourceDirectories(OmniEclipseProject project) {
        return FluentIterable.from(project.getSourceDirectories()).transform(new Function<OmniEclipseSourceDirectory, String>() {

            @Override
            public String apply(OmniEclipseSourceDirectory directory) {
                return directory.getPath();
            }
        }).toList();
    }

    private IPath collectDefaultJreLocation() {
        return JavaRuntime.getDefaultJREContainerEntry().getPath();
    }

}
