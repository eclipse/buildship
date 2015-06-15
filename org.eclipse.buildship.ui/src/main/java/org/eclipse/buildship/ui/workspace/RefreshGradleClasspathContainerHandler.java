/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.workspace;

import java.util.List;
import java.util.Set;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProgressListener;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;

import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure;
import com.gradleware.tooling.toolingmodel.OmniGradleProjectStructure;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.workspace.ClasspathDefinition;

/**
 * Refreshes the classpath for all Gradle projects that belong to the same builds as the currently selected Gradle projects.
 */
public final class RefreshGradleClasspathContainerHandler extends AbstractHandler {

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        Set<OmniGradleProjectStructure> rootProjects = collectSelectedRootGradleProjects(event);
        for (IJavaProject javaProject : collectAllRelatedWorkspaceProjects(rootProjects)) {
            updateClasspathContainer(javaProject);
        }
        return null;
    }

    private List<IJavaProject> collectAllRelatedWorkspaceProjects(Set<OmniGradleProjectStructure> rootProjects) {
        final ImmutableSet<String> allProjectNames = getAllProjectNames(rootProjects);
        return getExistingJavaProjects(allProjectNames);
    }

    private ImmutableSet<String> getAllProjectNames(Set<OmniGradleProjectStructure> rootProjects) {
        ImmutableSet.Builder<String> relatedProjects = ImmutableSet.builder();
        for (OmniGradleProjectStructure rootProject : rootProjects) {
            relatedProjects.addAll(getAllProjectNamesInGradleRootProject(rootProject));
        }

        return relatedProjects.build();
    }

    private List<String> getAllProjectNamesInGradleRootProject(OmniGradleProjectStructure root) {
        Builder<String> result = ImmutableList.builder();
        result.add(root.getName());
        for (OmniGradleProjectStructure child : root.getChildren()) {
            result.add(child.getName());
        }
        return result.build();
    }

    private List<IJavaProject> getExistingJavaProjects(final ImmutableSet<String> projectNames) {
        return FluentIterable.from(CorePlugin.workspaceOperations().getAllProjects()).filter(new Predicate<IProject>() {

            @Override
            public boolean apply(IProject project) {
                try {
                    return project.isAccessible() && projectNames.contains(project.getName()) && project.hasNature(JavaCore.NATURE_ID);
                } catch (CoreException e) {
                    throw new GradlePluginsRuntimeException(e);
                }
            }
        }).transform(new Function<IProject, IJavaProject>() {

            @Override
            public IJavaProject apply(IProject project) {
                return JavaCore.create(project);
            }
        }).toList();
    }

    private Set<OmniGradleProjectStructure> collectSelectedRootGradleProjects(ExecutionEvent event) {
        return FluentIterable.from(collectSelectedJavaProjects(event)).transform(new Function<IJavaProject, OmniGradleProjectStructure>() {

            @Override
            public OmniGradleProjectStructure apply(IJavaProject javaProject) {
                FixedRequestAttributes requestAttributes = CorePlugin.projectConfigurationManager().readProjectConfiguration(javaProject.getProject()).getRequestAttributes();
                ProcessStreams stream = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();

                OmniGradleBuildStructure structure = CorePlugin
                        .modelRepositoryProvider()
                        .getModelRepository(requestAttributes)
                        .fetchGradleBuildStructure(new TransientRequestAttributes(false, stream.getOutput(), stream.getError(), stream.getInput(),
                                ImmutableList.<ProgressListener> of(), ImmutableList.<org.gradle.tooling.events.ProgressListener> of(), GradleConnector
                                        .newCancellationTokenSource().token()), FetchStrategy.LOAD_IF_NOT_CACHED);
                return structure.getRootProject();
            }
        }).toSet();
    }

    private List<IJavaProject> collectSelectedJavaProjects(ExecutionEvent event) {
        return FluentIterable.from(collectSelectedProjects(event)).filter(new Predicate<IProject>() {

            @Override
            public boolean apply(IProject project) {
                try {
                    return project.hasNature(JavaCore.NATURE_ID);
                } catch (CoreException e) {
                    CorePlugin.logger().error("Failed to obtain java project for " + project, e);
                    return false;
                }
            }
        }).transform(new Function<IProject, IJavaProject>() {

            @Override
            public IJavaProject apply(IProject project) {
                return JavaCore.create(project);
            }
        }).toList();
    }

    private List<IProject> collectSelectedProjects(ExecutionEvent event) {
        ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
        Builder<IProject> result = ImmutableList.builder();
        if (currentSelection instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) currentSelection;
            IAdapterManager adapterManager = Platform.getAdapterManager();
            for (Object selectionItem : selection.toList()) {
                @SuppressWarnings({"cast", "RedundantCast"})
                IResource resource = (IResource) adapterManager.getAdapter(selectionItem, IResource.class);
                if (resource != null) {
                    IProject project = resource.getProject();
                    result.add(project);
                }
            }
        }
        return result.build();
    }

    private void updateClasspathContainer(IJavaProject project) {
        ClasspathContainerInitializer initializer = JavaCore.getClasspathContainerInitializer(ClasspathDefinition.GRADLE_CLASSPATH_CONTAINER_ID);
        org.eclipse.core.runtime.Path containerPath = new org.eclipse.core.runtime.Path(ClasspathDefinition.GRADLE_CLASSPATH_CONTAINER_ID);
        try {
            initializer.requestClasspathContainerUpdate(containerPath, project, null);
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }

}
