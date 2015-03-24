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

package com.gradleware.tooling.eclipse.core.projectimport;

import java.io.File;
import java.util.List;

import com.gradleware.tooling.eclipse.core.gradle.Specs;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.launching.JavaRuntime;
import org.gradle.tooling.BuildCancelledException;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.TestProgressListener;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.eclipse.core.CorePlugin;
import com.gradleware.tooling.eclipse.core.GradleNature;
import com.gradleware.tooling.eclipse.core.configuration.ProjectConfiguration;
import com.gradleware.tooling.eclipse.core.console.ProcessStreams;
import com.gradleware.tooling.eclipse.core.util.progress.DelegatingProgressListener;
import com.gradleware.tooling.eclipse.core.util.progress.ToolingApiWorkspaceJob;
import com.gradleware.tooling.eclipse.core.workspace.ClasspathDefinition;
import com.gradleware.tooling.eclipse.core.workspace.WorkspaceOperations;
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

/**
 * Imports a Gradle project into Eclipse using the project import coordinates given by a {@code ProjectImportConfiguration} instance.
 */
public final class ProjectImportJob extends ToolingApiWorkspaceJob {

    private final FixedRequestAttributes fixedAttributes;

    public ProjectImportJob(ProjectImportConfiguration configuration) {
        super("Importing project");

        this.fixedAttributes = configuration.toFixedAttributes();

        // explicitly show a dialog with the progress while the import is in process
        setUser(true);
    }

    @Override
    public IStatus runInWorkspace(IProgressMonitor monitor) {
        try {
            importProject(monitor);
            return Status.OK_STATUS;
        } catch (BuildCancelledException e) {
            // if the job was cancelled by the user, do not show an error dialog
            CorePlugin.logger().info(e.getMessage());
            return Status.CANCEL_STATUS;
        } catch (Exception e) {
            return new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, "Importing the project failed.", e);
        } finally {
            monitor.done();
        }
    }

    public void importProject(IProgressMonitor monitor) {
        monitor.beginTask("Import Gradle Project", 100);

        OmniEclipseGradleBuild eclipseGradleBuild = fetchEclipseGradleBuild(new SubProgressMonitor(monitor, 50));
        OmniEclipseProject rootProject = eclipseGradleBuild.getRootEclipseProject();
        List<OmniEclipseProject> allProjects = rootProject.getAll();
        for (OmniEclipseProject project : allProjects) {
            importProject(project, rootProject, new SubProgressMonitor(monitor, 50 / allProjects.size()));
        }
    }

    private OmniEclipseGradleBuild fetchEclipseGradleBuild(IProgressMonitor monitor) {
        monitor.beginTask("Load Eclipse Project", IProgressMonitor.UNKNOWN);
        try {
            ProcessStreams streams = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
            List<ProgressListener> listeners = ImmutableList.<ProgressListener>of(new DelegatingProgressListener(monitor));
            TransientRequestAttributes transientAttributes = new TransientRequestAttributes(false, streams.getOutput(), streams.getError(), null, listeners, ImmutableList.<TestProgressListener>of(), getToken());
            ModelRepository repository = CorePlugin.modelRepositoryProvider().getModelRepository(this.fixedAttributes);
            return repository.fetchEclipseGradleBuild(transientAttributes, FetchStrategy.FORCE_RELOAD);
        } finally {
            monitor.done();
        }
    }

    private void importProject(OmniEclipseProject project, OmniEclipseProject rootProject, IProgressMonitor monitor) {
        monitor.beginTask("Import project " + project.getName(), 3);
        try {
            WorkspaceOperations workspaceOperations = CorePlugin.workspaceOperations();

            // create a new project in the Eclipse workspace for the current Gradle project
            IProject workspaceProject = workspaceOperations.createProject(project.getName(), project.getProjectDirectory(), collectChildProjectLocations(project),
                    ImmutableList.of(GradleNature.ID), new SubProgressMonitor(monitor, 1));

            // persist the Gradle-specific configuration in the Eclipse project's .settings folder
            ProjectConfiguration projectConfiguration = ProjectConfiguration.from(this.fixedAttributes, project);
            CorePlugin.projectConfigurationManager().saveProjectConfiguration(projectConfiguration, workspaceProject);

            // if the current Gradle project is a Java project, configure the Java nature,
            // the classpath, and the source paths
            if (isJavaProject(project)) {
                ClasspathDefinition classpath = collectClasspath(project, rootProject);
                workspaceOperations.createJavaProject(workspaceProject, classpath, new SubProgressMonitor(monitor, 1));
                workspaceOperations.refresh(workspaceProject, new SubProgressMonitor(monitor, 1));
            }
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
