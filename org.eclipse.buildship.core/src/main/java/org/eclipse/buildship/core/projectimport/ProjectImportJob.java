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

import com.google.common.base.*;
import org.gradle.tooling.BuildCancelledException;
import org.gradle.tooling.BuildException;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.events.build.BuildProgressListener;
import org.gradle.tooling.events.task.TaskProgressListener;
import org.gradle.tooling.events.test.TestProgressListener;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.launching.JavaRuntime;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.gradle.Specs;
import org.eclipse.buildship.core.util.progress.DelegatingProgressListener;
import org.eclipse.buildship.core.util.progress.ToolingApiWorkspaceJob;
import org.eclipse.buildship.core.util.string.StringUtils;
import org.eclipse.buildship.core.workspace.ClasspathDefinition;
import org.eclipse.buildship.core.workspace.WorkspaceOperations;

/**
 * Imports a Gradle project into Eclipse using the project import coordinates given by a
 * {@code ProjectImportConfiguration} instance.
 */
public final class ProjectImportJob extends ToolingApiWorkspaceJob {

    // if the job returns a normal error status then eclipse shows the default error dialog which is
    // too basic. on the other hand the OK_STATUS is not feasible since clients of the job might
    // need to determine if the job was finished successfully. to solve both problems we use this
    // custom, non-ok status
    private static final IStatus SILENT_ERROR_STATUS = new Status(IStatus.CANCEL, CorePlugin.PLUGIN_ID, "");

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
            return handleBuildCancelled(e);
        } catch (BuildException e) {
            return handleBuildFailed(e);
        } catch (GradleConnectionException e) {
            return handleGradleConnectionFailed(e);
        } catch (GradlePluginsRuntimeException e) {
            return handlePluginFailed(e);
        } catch (Throwable t) {
            return handleUnknownFailed(t);
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
            TransientRequestAttributes transientAttributes = new TransientRequestAttributes(false, streams.getOutput(), streams.getError(), null, listeners,
                    ImmutableList.<BuildProgressListener>of(), ImmutableList.<TaskProgressListener>of(), ImmutableList.<TestProgressListener>of(), getToken());
            ModelRepository repository = CorePlugin.modelRepositoryProvider().getModelRepository(this.fixedAttributes);
            return repository.fetchEclipseGradleBuild(transientAttributes, FetchStrategy.FORCE_RELOAD);
        } finally {
            monitor.done();
        }
    }

    private void importProject(OmniEclipseProject project, OmniEclipseProject rootProject, IProgressMonitor monitor) {
        monitor.beginTask("Import project " + project.getName(), 4);
        try {
            // check if an Eclipse project already exists at the location of the Gradle project to
            // import
            WorkspaceOperations workspaceOperations = CorePlugin.workspaceOperations();
            File projectDirectory = project.getProjectDirectory();
            Optional<IProjectDescription> projectDescription = workspaceOperations.findProjectInFolder(projectDirectory, new SubProgressMonitor(monitor, 1));

            IProject workspaceProject;
            if (projectDescription.isPresent()) {
                // include the existing Eclipse project in the workspace
                workspaceProject = workspaceOperations
                        .includeProject(projectDescription.get(), collectChildProjectLocations(project), ImmutableList.of(GradleProjectNature.ID), new SubProgressMonitor(monitor,
                                2));
            } else {
                // create a new Eclipse project in the workspace for the current Gradle project
                workspaceProject = workspaceOperations.createProject(project.getName(), project.getProjectDirectory(), collectChildProjectLocations(project), ImmutableList
                        .of(GradleProjectNature.ID), new SubProgressMonitor(monitor, 1));

                // if the current Gradle project is a Java project, configure the Java nature,
                // the classpath, and the source paths
                if (isJavaProject(project)) {
                    ClasspathDefinition classpath = collectClasspath(project, rootProject);
                    workspaceOperations.createJavaProject(workspaceProject, classpath, new SubProgressMonitor(monitor, 1));
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

    private IStatus handleBuildCancelled(BuildCancelledException e) {
        // if the job was cancelled by the user, just log the event
        CorePlugin.logger().info("Gradle project import cancelled", e);
        return Status.CANCEL_STATUS;
    }

    private IStatus handleBuildFailed(BuildException e) {
        // if there is an error in the project's build script, notify the user, but don't
        // put it in the error log (log as a warning instead)
        String message = "Gradle project import failed due to an error in the referenced Gradle build.";
        CorePlugin.logger().warn(message, e);
        CorePlugin.userNotification().errorOccurred("Project import failed", message, collectErrorMessages(e), IStatus.WARNING, e);
        // the problem is already logged, the job doesn't have to record it again
        return SILENT_ERROR_STATUS;
    }

    private IStatus handleGradleConnectionFailed(GradleConnectionException e) {
        // if there is an error connecting to Gradle, notify the user, but don't
        // put it in the error log (log as a warning instead)
        String message = "Gradle project import failed due to an error connecting to the Gradle build.";
        CorePlugin.logger().warn(message, e);
        CorePlugin.userNotification().errorOccurred("Project import failed", message, collectErrorMessages(e), IStatus.WARNING, e);
        // the problem is already logged, the job doesn't have to record it again
        return SILENT_ERROR_STATUS;
    }

    private IStatus handlePluginFailed(GradlePluginsRuntimeException e) {
        // if the exception was thrown by Buildship it should be shown and logged
        String message = "Gradle project import failed due to an error setting up the Eclipse projects.";
        CorePlugin.logger().error(message, e);
        CorePlugin.userNotification().errorOccurred("Project import failed", message, collectErrorMessages(e), IStatus.ERROR, e);
        // the problem is already logged, the job doesn't have to record it again
        return SILENT_ERROR_STATUS;
    }

    private IStatus handleUnknownFailed(Throwable t) {
        // if an unexpected exception was thrown it should be shown and logged
        String message = "Gradle project import failed due to an unexpected error.";
        CorePlugin.logger().error(message, t);
        CorePlugin.userNotification().errorOccurred("Project import failed", message, collectErrorMessages(t), IStatus.ERROR, t);
        // the problem is already logged, the job doesn't have to record it again
        return SILENT_ERROR_STATUS;
    }

    private String collectErrorMessages(Throwable t) {
        // recursively collect the error messages going up the stacktrace
        // avoid the same message showing twice in a row
        List<String> messages = Lists.newArrayList();
        collectCausesRecursively(t.getCause(), messages);
        String messageStack = Joiner.on('\n').join(StringUtils.removeAdjacentDuplicates(messages));
        return t.getMessage() + (messageStack.isEmpty() ? "" : "\n\n" + messageStack);
    }

    private void collectCausesRecursively(Throwable t, List<String> messages) {
        List<String> singleLineMessages = Splitter.on('\n').omitEmptyStrings().splitToList(Strings.nullToEmpty(t.getMessage()));
        messages.addAll(singleLineMessages);
        Throwable cause = t.getCause();
        if (cause != null) {
            collectCausesRecursively(cause, messages);
        }
    }

}
