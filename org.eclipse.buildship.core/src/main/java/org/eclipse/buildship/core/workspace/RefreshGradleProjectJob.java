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
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProgressListener;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniGradleProject;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.ModelRepositoryProvider;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;
import com.gradleware.tooling.toolingmodel.util.Maybe;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.gradle.Specs;
import org.eclipse.buildship.core.projectimport.ProjectCreatedEvent;
import org.eclipse.buildship.core.projectimport.internal.DefaultProjectCreatedEvent;
import org.eclipse.buildship.core.util.predicate.Predicates;
import org.eclipse.buildship.core.util.progress.DelegatingProgressListener;

/**
 * Forces the reload of the given Gradle root project and requests the
 * {@link GradleClasspathContainer} to refresh all workspace projects that are part of the given
 * Gradle root project.
 */
public final class RefreshGradleProjectJob extends WorkspaceJob {

    private final FixedRequestAttributes rootRequestAttributes;
    private final CancellationTokenSource tokenSource;

    public RefreshGradleProjectJob(FixedRequestAttributes rootRequestAttributes) {
        super("Reload root project at " + Preconditions.checkNotNull(rootRequestAttributes).getProjectDir().getAbsolutePath());
        this.rootRequestAttributes = rootRequestAttributes;
        this.tokenSource = GradleConnector.newCancellationTokenSource();
    }

    @Override
    public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
        monitor.beginTask("Reload projects and request project update", IProgressMonitor.UNKNOWN);
        IJobManager manager = Job.getJobManager();
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        manager.beginRule(workspaceRoot, monitor);
        try {
            OmniEclipseGradleBuild result = forceReloadEclipseGradleBuild(this.rootRequestAttributes, monitor);
            synchronizeGradleProjectsWithWorkspace(result);
            return Status.OK_STATUS;
        } catch (Exception e) {
            // todo (etst) should be error, handle exception like in ToolingApiInvoker
            return new Status(IStatus.WARNING, CorePlugin.PLUGIN_ID, "refresh failed", e);
        } finally {
            manager.endRule(workspaceRoot);
            monitor.done();
        }
    }

    private OmniEclipseGradleBuild forceReloadEclipseGradleBuild(FixedRequestAttributes requestAttributes, final IProgressMonitor monitor) {
        ProcessStreams streams = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
        ImmutableList<ProgressListener> listeners = ImmutableList.<ProgressListener>of(new DelegatingProgressListener(monitor));
        TransientRequestAttributes transientAttributes = new TransientRequestAttributes(false, streams.getOutput(), streams.getError(), streams.getInput(), listeners,
                ImmutableList.<org.gradle.tooling.events.ProgressListener>of(), this.tokenSource.token());
        ModelRepositoryProvider repository = CorePlugin.modelRepositoryProvider();
        return repository.getModelRepository(requestAttributes).fetchEclipseGradleBuild(transientAttributes, FetchStrategy.FORCE_RELOAD);
    }

    private void synchronizeGradleProjectsWithWorkspace(OmniEclipseGradleBuild gradleBuild) {
        // collect added and removed projects
        List<OmniEclipseProject> allGradleProjects = gradleBuild.getRootEclipseProject().getAll();
        List<IProject> oldWorkspaceProjects = collectWorkspaceProjectsRemovedFromGradle(allGradleProjects);
        List<OmniEclipseProject> newGradleProjects = collectGradleProjectsNotExistInWorkspace(allGradleProjects);

        // remove old, add new and refresh existing workspace projects
        for (IProject oldProject : oldWorkspaceProjects) {
            removeProject(oldProject);
        }
        for (OmniEclipseProject gradleProject : allGradleProjects) {
            if (newGradleProjects.contains(gradleProject)) {
                addProject(gradleProject, gradleBuild);
            } else {
                updateProject(gradleProject);
            }
        }
    }

    private List<IProject> collectWorkspaceProjectsRemovedFromGradle(List<OmniEclipseProject> gradleProjects) {
        // find all projects in the workspace that belong to the same Gradle project (based on the
        // FixedRequestAttributes) but no module matches with its location
        final Set<File> projectDirectories = FluentIterable.from(gradleProjects).transform(new Function<OmniEclipseProject, File>() {

            @Override
            public File apply(OmniEclipseProject gradleProject) {
                return gradleProject.getProjectDirectory();
            }
        }).toSet();

        return FluentIterable.from(CorePlugin.workspaceOperations().getAllProjects()).filter(Predicates.accessibleGradleProject()).filter(new Predicate<IProject>() {

            @Override
            public boolean apply(IProject project) {
                IPath location = project.getLocation();
                if (location != null) {
                    return !projectDirectories.contains(location.toFile()) && CorePlugin.projectConfigurationManager().readProjectConfiguration(project).getRequestAttributes()
                            .equals(RefreshGradleProjectJob.this.rootRequestAttributes);
                } else {
                    return false;
                }
            }
        }).toList();
    }

    private List<OmniEclipseProject> collectGradleProjectsNotExistInWorkspace(List<OmniEclipseProject> gradleProjects) {
        // collect all Gradle project which doesn't have a corresponding workspace project with the
        // same location
        return FluentIterable.from(gradleProjects).filter(new Predicate<OmniEclipseProject>() {

            @Override
            public boolean apply(OmniEclipseProject gradleProject) {
                return !CorePlugin.workspaceOperations().findProjectByLocation(gradleProject.getProjectDirectory()).isPresent();
            }
        }).toList();
    }

    private void addProject(OmniEclipseProject gradleProject, OmniEclipseGradleBuild eclipseGradleBuild) {
        importProject(gradleProject, eclipseGradleBuild, this.rootRequestAttributes, ImmutableList.<String>of(), new NullProgressMonitor());
    }

    private void removeProject(IProject project) {
        try {
            removeNature(project, GradleProjectNature.ID);
            removeProjectConfiguration(project);
            if (project.hasNature(JavaCore.NATURE_ID)) {
                IJavaProject javaProject = JavaCore.create(project);
                GradleClasspathContainer.requestUpdateOf(javaProject);
                cleanBuildProject(javaProject);
            }
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }

    private void cleanBuildProject(IJavaProject javaProject) throws CoreException {
        // when a workspace project is excluded and included multiple times, JDT throws an
        // exception, unless a project is cleaned
        javaProject.getProject().build(IncrementalProjectBuilder.CLEAN_BUILD, null);
    }

    private void updateProject(OmniEclipseProject gradleProject) {
        // todo (donat) the update mechanism should be extended to non-java projects too
        try {
            Optional<IProject> workspaceProject = CorePlugin.workspaceOperations().findProjectByLocation(gradleProject.getProjectDirectory());
            if (workspaceProject.isPresent()) {
                IProject project = workspaceProject.get();

                if (project.isAccessible() && !GradleProjectNature.INSTANCE.isPresentOn(project)) {
                    addProjectConfiguration(this.rootRequestAttributes, gradleProject, project);
                    addNature(project, GradleProjectNature.ID);
                }

                if (project.hasNature(JavaCore.NATURE_ID)) {
                    IJavaProject javaProject = JavaCore.create(project);
                    GradleClasspathContainer.requestUpdateOf(javaProject);
                }
            }
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }

    @Override
    public boolean belongsTo(Object family) {
        // associate with a family so we can cancel all builds of
        // this type at once through the Eclipse progress manager
        return RefreshGradleProjectJob.class.getName().equals(family);
    }

    @Override
    protected void canceling() {
        this.tokenSource.cancel();
    }

    private static void addProjectConfiguration(FixedRequestAttributes requestAttributes, OmniEclipseProject gradleProject, IProject project) {
        ProjectConfiguration configuration = ProjectConfiguration.from(requestAttributes, gradleProject);
        CorePlugin.projectConfigurationManager().saveProjectConfiguration(configuration, project);
    }

    private static void removeProjectConfiguration(IProject project) throws CoreException {
        IFolder settingsFolder = project.getFolder(".settings");
        if (settingsFolder.exists()) {
            settingsFolder.delete(true, false, null);
        }
    }

    private static void addNature(IProject project, String natureId) throws CoreException {
        // get the description
        IProjectDescription description = project.getDescription();

        // abort if the project already has the nature applied
        List<String> currentNatureIds = ImmutableList.copyOf(description.getNatureIds());
        if (currentNatureIds.contains(natureId)) {
            return;
        }

        // add the nature to the project
        ImmutableList<String> newIds = ImmutableList.<String>builder().addAll(currentNatureIds).add(natureId).build();
        description.setNatureIds(newIds.toArray(new String[newIds.size()]));

        // save the updated description
        project.setDescription(description, null);
    }

    private static void removeNature(IProject project, String natureId) throws CoreException {
        // get the description
        IProjectDescription description = project.getDescription();

        // remove the requested nature id from the nature list
        String[] natures = FluentIterable.from(Arrays.asList(description.getNatureIds())).filter(new Predicate<String>() {

            @Override
            public boolean apply(String natureId) {
                return !natureId.equals(GradleProjectNature.ID);
            }
        }).toArray(String.class);

        // set the filtered nature list for the project
        description.setNatureIds(natures);
        project.setDescription(description, null);
    }

    // the code below is copied from ProjectImportJob without changing anything

    private static void importProject(OmniEclipseProject gradleProject, OmniEclipseGradleBuild gradleBuild, FixedRequestAttributes fixedAttributes, List<String> workingSets,
            IProgressMonitor monitor) {
        monitor.beginTask("Import project " + gradleProject.getName(), 3);
        try {
            // check if an Eclipse project already exists at the location of the Gradle project to
            // import
            WorkspaceOperations workspaceOperations = CorePlugin.workspaceOperations();
            File projectDirectory = gradleProject.getProjectDirectory();
            Optional<IProjectDescription> projectDescription = workspaceOperations.findProjectInFolder(projectDirectory, new SubProgressMonitor(monitor, 1));

            // collect all the sub folders to hide under the project
            List<File> filteredSubFolders = ImmutableList.<File>builder().addAll(collectChildProjectLocations(gradleProject)).add(getBuildDirectory(gradleBuild, gradleProject))
                    .add(getDotGradleDirectory(gradleProject)).build();
            ImmutableList<String> gradleNature = ImmutableList.of(GradleProjectNature.ID);

            IProject workspaceProject;
            if (projectDescription.isPresent()) {
                // include the existing Eclipse project in the workspace
                workspaceProject = workspaceOperations.includeProject(projectDescription.get(), filteredSubFolders, gradleNature, new SubProgressMonitor(monitor, 2));
            } else {
                // create a new Eclipse project in the workspace for the current Gradle project
                workspaceProject = workspaceOperations
                        .createProject(gradleProject.getName(), gradleProject.getProjectDirectory(), filteredSubFolders, gradleNature, new SubProgressMonitor(monitor, 1));

                // if the current Gradle project is a Java project, configure the Java nature, the
                // classpath, and the source paths
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
