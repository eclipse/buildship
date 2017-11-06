/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.ui.workspace;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import com.gradleware.tooling.toolingclient.GradleDistribution;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.BuildConfiguration;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.util.collections.AdapterFunction;
import org.eclipse.buildship.core.workspace.GradleNatureAddedEvent;
import org.eclipse.buildship.core.workspace.GradleBuild;
import org.eclipse.buildship.core.workspace.NewProjectHandler;

/**
 * Synchronizes the given projects as if the user had run the import wizard on their location.
 *
 * @author Stefan Oehme
 *
 */
public class AddBuildshipNatureHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof StructuredSelection) {
            List<?> elements = ((StructuredSelection) selection).toList();
            Set<IProject> projects = collectProjects(elements);
            Set<BuildConfiguration> buildConfigs = createBuildConfigsFor(projects);
            synchronize(buildConfigs);
            publishNatureAddedEvent(projects);
        }
        return null;
    }

    private Set<IProject> collectProjects(List<?> elements) {
        Set<IProject> projects = Sets.newLinkedHashSet();
        AdapterFunction<IProject> adapterFunction = AdapterFunction.forType(IProject.class);
        for (Object element : elements) {
            IProject project = adapterFunction.apply(element);
            if (project != null && !GradleProjectNature.isPresentOn(project) && project.getLocation() != null) {
                projects.add(project);
            }
        }
        return projects;
    }

    private Set<BuildConfiguration> createBuildConfigsFor(Set<IProject> projects) {
        Set<BuildConfiguration> buildConfigs = Sets.newLinkedHashSet();
        for (IProject project : projects) {
            buildConfigs.add(CorePlugin.configurationManager().createBuildConfiguration(project.getLocation().toFile(), false, GradleDistribution.fromBuild(), null, false, false, false));
        }
        return buildConfigs;
    }

    private void synchronize(Set<BuildConfiguration> buildConfigs) {
        for (BuildConfiguration buildConfig : buildConfigs) {
            GradleBuild gradleBuild = CorePlugin.gradleWorkspaceManager().getGradleBuild(buildConfig);
            gradleBuild.synchronize(NewProjectHandler.IMPORT_AND_MERGE);
        }
    }

    private void publishNatureAddedEvent(Set<IProject> projects) {
        // TODO this could be solved in a more general way by publishing nature added and removed events during project synchronization
        CorePlugin.listenerRegistry().dispatch(new GradleNatureAddedEvent(projects));
    }

}
