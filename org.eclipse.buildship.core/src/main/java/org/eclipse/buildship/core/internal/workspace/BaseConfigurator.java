/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.workspace;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.collect.ImmutableList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.InitializationContext;
import org.eclipse.buildship.core.ProjectConfigurator;
import org.eclipse.buildship.core.ProjectContext;
import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.marker.GradleErrorMarker;

public class BaseConfigurator implements ProjectConfigurator {

    private Map<File, EclipseProject> locationToProject;
    private org.eclipse.buildship.core.internal.workspace.GradleBuild internalGradleBuild;

    @Override
    public void init(InitializationContext context, IProgressMonitor monitor) {
        GradleBuild gradleBuild = context.getGradleBuild();
        this.internalGradleBuild = ((org.eclipse.buildship.core.internal.DefaultGradleBuild)gradleBuild).getInternalGradleBuild();
        Collection<EclipseProject> eclipseProjects = ModelProviderUtil.fetchAllEclipseProjects(this.internalGradleBuild, GradleConnector.newCancellationTokenSource(), FetchStrategy.LOAD_IF_NOT_CACHED, monitor);
        this.locationToProject = eclipseProjects.stream().collect(Collectors.toMap(p -> p.getProjectDirectory(), p -> p));
    }

    @Override
    public void configure(ProjectContext context, IProgressMonitor monitor) {
        IProject project = context.getProject();
        try {
            configure(project, monitor);
        } catch (CoreException e) {
            GradleErrorMarker.create(context.getProject(), this.internalGradleBuild, "Failed to configure project", e, 0);
            CorePlugin.logger().warn("Failed to configure project " + context.getProject().getName(), e);
        }
    }

    private void configure(IProject project, IProgressMonitor monitor) throws CoreException {
        SubMonitor progress = SubMonitor.convert(monitor);
        progress.setWorkRemaining(4);

        PersistentModelBuilder persistentModel = new PersistentModelBuilder(CorePlugin.modelPersistence().loadModel(project));
        EclipseProject model = lookupEclipseModel(project);
        progress.worked(1);

        BuildScriptLocationUpdater.update(model, persistentModel, progress.newChild(1));

        LinkedResourcesUpdater.update(project, ImmutableList.copyOf(model.getLinkedResources()), persistentModel, progress.newChild(1));
        GradleFolderUpdater.update(project, model, persistentModel, progress.newChild(1));
        ProjectNatureUpdater.update(project,  ImmutableList.copyOf(model.getProjectNatures()), persistentModel, progress.newChild(1));
        BuildCommandUpdater.update(project, ImmutableList.copyOf(model.getBuildCommands()), persistentModel, progress.newChild(1));

        // TODO (donat) extract Java synchronization to external configurator
        if (isJavaProject(model)) {
            synchronizeJavaProject(model, project, persistentModel, progress);
        } else {
            persistentModel.classpath(ImmutableList.<IClasspathEntry>of());
        }

        CorePlugin.modelPersistence().saveModel(persistentModel.build());
    }

    private void synchronizeJavaProject(final EclipseProject model, final IProject project, final PersistentModelBuilder persistentModel, SubMonitor progress) throws CoreException {
        JavaCore.run(new IWorkspaceRunnable() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                SubMonitor progress = SubMonitor.convert(monitor);
                synchronizeJavaProjectInTransaction(model, project, persistentModel, progress);
            }
        }, progress.newChild(1));
    }

    private void synchronizeJavaProjectInTransaction(final EclipseProject model, final IProject project, PersistentModelBuilder persistentModel, SubMonitor progress) throws JavaModelException, CoreException {
        progress.setWorkRemaining(7);
        //old Gradle versions did not expose natures, so we need to add the Java nature explicitly
        CorePlugin.workspaceOperations().addNature(project, JavaCore.NATURE_ID, progress.newChild(1));
        IJavaProject javaProject = JavaCore.create(project);
        OutputLocationUpdater.update(javaProject, model.getOutputLocation(), progress.newChild(1));
        SourceFolderUpdater.update(javaProject, ImmutableList.copyOf(model.getSourceDirectories()), progress.newChild(1));
        LibraryFilter.update(javaProject, model, progress.newChild(1));
        ClasspathContainerUpdater.update(javaProject, model, progress.newChild(1));
        JavaSourceSettingsUpdater.update(javaProject, model, progress.newChild(1));
        GradleClasspathContainerUpdater.updateFromModel(javaProject, model, this.locationToProject.values(), persistentModel, progress.newChild(1));
        CorePlugin.externalLaunchConfigurationManager().updateClasspathProviders(project);
    }

    private boolean isJavaProject(EclipseProject model) {
        return model.getJavaSourceSettings() != null;
    }

    private EclipseProject lookupEclipseModel(IProject project) {
        IPath path = project.getLocation();
        if (path == null) {
            return null;
        }
        return this.locationToProject.get(path.toFile());
    }

    @Override
    public void unconfigure(ProjectContext context, IProgressMonitor monitor) {
        CorePlugin.modelPersistence().deleteModel(context.getProject());
    }
}
