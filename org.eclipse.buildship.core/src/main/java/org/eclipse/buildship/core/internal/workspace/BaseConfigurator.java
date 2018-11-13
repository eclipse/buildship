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

public class BaseConfigurator implements ProjectConfigurator {

    // TODO (donat) shall we share the original EclipseProject instances via ProjectContext?
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
        try {
            doConfigure(context, monitor);
        } catch (CoreException e) {
            // TODO (donat) maybe add and error marker
            CorePlugin.logger().warn("Failed to configure project " + context.getProject().getName(), e);
        }
    }

    private void doConfigure(ProjectContext context, IProgressMonitor monitor) throws CoreException {
        SubMonitor progress = SubMonitor.convert(monitor);
        progress.setWorkRemaining(4);

        IProject workspaceProject = context.getProject();
        SynchronizeGradleBuildOperation.persistentModel = new PersistentModelBuilder(CorePlugin.modelPersistence().loadModel(workspaceProject));
        EclipseProject model = lookupEclipseModel(context.getProject());
        progress.worked(1);

        BuildScriptLocationUpdater.update(model, SynchronizeGradleBuildOperation.persistentModel, progress.newChild(1));

        LinkedResourcesUpdater.update(workspaceProject, ImmutableList.copyOf(model.getLinkedResources()), SynchronizeGradleBuildOperation.persistentModel, progress.newChild(1));
        GradleFolderUpdater.update(workspaceProject, model, SynchronizeGradleBuildOperation.persistentModel, progress.newChild(1));
        ProjectNatureUpdater.update(workspaceProject,  ImmutableList.copyOf(model.getProjectNatures()), SynchronizeGradleBuildOperation.persistentModel, progress.newChild(1));
        BuildCommandUpdater.update(workspaceProject, ImmutableList.copyOf(model.getBuildCommands()), SynchronizeGradleBuildOperation.persistentModel, progress.newChild(1));

        if (isJavaProject(model)) {
            synchronizeJavaProject(model, workspaceProject, SynchronizeGradleBuildOperation.persistentModel, progress);
        } else {
            SynchronizeGradleBuildOperation.persistentModel.classpath(ImmutableList.<IClasspathEntry>of());
        }
    }

    private void synchronizeJavaProject(final EclipseProject project, final IProject workspaceProject, final PersistentModelBuilder persistentModel, SubMonitor progress) throws CoreException {
        JavaCore.run(new IWorkspaceRunnable() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                SubMonitor progress = SubMonitor.convert(monitor);
                synchronizeJavaProjectInTransaction(project, workspaceProject, persistentModel, progress);
            }
        }, progress.newChild(1));
    }

    private void synchronizeJavaProjectInTransaction(final EclipseProject project, final IProject workspaceProject, PersistentModelBuilder persistentModel, SubMonitor progress) throws JavaModelException, CoreException {
        progress.setWorkRemaining(8);
        //old Gradle versions did not expose natures, so we need to add the Java nature explicitly
        CorePlugin.workspaceOperations().addNature(workspaceProject, JavaCore.NATURE_ID, progress.newChild(1));
        IJavaProject javaProject = JavaCore.create(workspaceProject);
        OutputLocationUpdater.update(javaProject, project.getOutputLocation(), progress.newChild(1));
        SourceFolderUpdater.update(javaProject, ImmutableList.copyOf(project.getSourceDirectories()), progress.newChild(1));
        LibraryFilter.update(javaProject, project, progress.newChild(1));
        ClasspathContainerUpdater.update(javaProject, project, progress.newChild(1));
        JavaSourceSettingsUpdater.update(javaProject, project, progress.newChild(1));
        GradleClasspathContainerUpdater.updateFromModel(javaProject, project, this.locationToProject.values(), persistentModel, progress.newChild(1));
        WtpClasspathUpdater.update(javaProject, project, this.internalGradleBuild, progress.newChild(1));
        CorePlugin.externalLaunchConfigurationManager().updateClasspathProviders(workspaceProject);
    }

    private boolean isJavaProject(EclipseProject project) {
        return project.getJavaSourceSettings() != null;
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
