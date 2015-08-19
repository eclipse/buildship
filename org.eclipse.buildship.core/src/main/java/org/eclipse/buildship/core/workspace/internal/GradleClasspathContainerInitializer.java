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

import java.util.List;

import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProgressListener;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.ModelRepository;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.gradle.Specs;
import org.eclipse.buildship.core.util.progress.ToolingApiWorkspaceJob;
import org.eclipse.buildship.core.workspace.GradleClasspathContainer;

/**
 * Initializes the classpath of each Eclipse workspace project that has a Gradle nature with the
 * linked resources/sources/project and external dependencies of the underlying Gradle project.
 * <p/>
 * When this initializer is invoked, it looks up the {@link OmniEclipseProject} for the given
 * Eclipse workspace project, applies all the found linked resources and the sources, reads the
 * project dependencies and external dependencies and adds the dependencies to the
 * {@link org.eclipse.buildship.core.workspace.GradleClasspathContainer#CONTAINER_ID} classpath
 * container.
 * <p/>
 * This initializer is assigned to the projects via the
 * {@code org.eclipse.jdt.core.classpathContainerInitializer} extension point.
 * <p/>
 * The initialization is scheduled as a job, to not block the IDE upon startup.
 */
public final class GradleClasspathContainerInitializer extends ClasspathContainerInitializer {

    @Override
    public void initialize(IPath containerPath, IJavaProject project) {
        scheduleClasspathInitialization(project);
    }

    @Override
    public void requestClasspathContainerUpdate(IPath containerPath, IJavaProject project, IClasspathContainer containerSuggestion) {
        scheduleClasspathInitialization(project);
    }

    private void scheduleClasspathInitialization(final IJavaProject project) {
        new ToolingApiWorkspaceJob("Initialize Gradle classpath for project '" + project.getElementName() + "'") {

            @Override
            protected void runToolingApiJobInWorkspace(IProgressMonitor monitor) throws Exception {
                monitor.beginTask("Initializing classpath", 2);

                // use the same rule as the ProjectImportJob to do the initialization
                IJobManager manager = Job.getJobManager();
                IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
                manager.beginRule(workspaceRoot, monitor);
                try {
                    internalInitialize(project, monitor);
                } finally {
                    manager.endRule(workspaceRoot);
                }
            }
        }.schedule();
    }

    private void internalInitialize(IJavaProject javaProject, IProgressMonitor monitor) throws CoreException {
        IProject project = javaProject.getProject();
        Optional<OmniEclipseProject> gradleProject = findEclipseProject(project);
        monitor.worked(1);
        if (gradleProject.isPresent()) {
            if (project.isAccessible()) {
                // update linked resources
                LinkedResourcesUpdater.update(project, gradleProject.get().getLinkedResources(), new SubProgressMonitor(monitor, 1));

                // update the sources
                SourceFolderUpdater.update(javaProject, gradleProject.get().getSourceDirectories(), new SubProgressMonitor(monitor, 1));

                // update project/external dependencies
                ClasspathContainerUpdater.update(javaProject, gradleProject.get(), new Path(GradleClasspathContainer.CONTAINER_ID), new SubProgressMonitor(monitor, 1));
            }
        } else {
            throw new GradlePluginsRuntimeException(String.format("Cannot find Eclipse project model for project %s.", project));
        }
    }

    private Optional<OmniEclipseProject> findEclipseProject(IProject project) {
        ProjectConfiguration configuration = CorePlugin.projectConfigurationManager().readProjectConfiguration(project);
        OmniEclipseGradleBuild eclipseGradleBuild = fetchEclipseGradleBuild(configuration.getRequestAttributes());
        return eclipseGradleBuild.getRootEclipseProject().tryFind(Specs.eclipseProjectMatchesProjectPath(configuration.getProjectPath()));
    }

    private OmniEclipseGradleBuild fetchEclipseGradleBuild(FixedRequestAttributes fixedRequestAttributes) {
        ProcessStreams streams = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
        List<ProgressListener> noProgressListeners = ImmutableList.of();
        List<org.gradle.tooling.events.ProgressListener> noTypedProgressListeners = ImmutableList.of();
        CancellationToken cancellationToken = GradleConnector.newCancellationTokenSource().token();
        TransientRequestAttributes transientAttributes = new TransientRequestAttributes(false, streams.getOutput(), streams.getError(), null, noProgressListeners,
                noTypedProgressListeners, cancellationToken);
        ModelRepository repository = CorePlugin.modelRepositoryProvider().getModelRepository(fixedRequestAttributes);
        return repository.fetchEclipseGradleBuild(transientAttributes, FetchStrategy.LOAD_IF_NOT_CACHED);
    }

}
