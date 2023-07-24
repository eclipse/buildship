/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.InitializationContext;
import org.eclipse.buildship.core.ProjectConfigurator;
import org.eclipse.buildship.core.ProjectContext;
import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.CoreTraceScopes;
import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.Logger;

public final class InternalProjectConfigurator implements ProjectConfigurator, Comparable<InternalProjectConfigurator> {

    private final ProjectConfigurator configurator;
    private final ProjectConfiguratorContribution contribution;
    private static final Logger LOGGER = CorePlugin.logger();

    private InternalProjectConfigurator(ProjectConfiguratorContribution contribution) {
        this.configurator = createConfigurator(contribution);
        this.contribution = contribution;
    }

    private static ProjectConfigurator createConfigurator(ProjectConfiguratorContribution contribution) {
        try {
            return contribution.createConfigurator();
        } catch (CoreException e) {
            // This object should be created from valid configurators only
            throw new GradlePluginsRuntimeException(e);
        }
    }

    @Override
    public void init(InitializationContext context, IProgressMonitor monitor) {
        this.configurator.init(context, monitor);
    }

    @Override
    public void configure(ProjectContext context, IProgressMonitor monitor) {
        this.configurator.configure(context, monitor);
    }

    @Override
    public void unconfigure(ProjectContext context, IProgressMonitor monitor) {
        this.configurator.unconfigure(context, monitor);
    }

    public String getContributorPluginId() {
        return this.contribution.getContributorPluginId();
    }

    public String getId() {
        return this.contribution.getId();
    }

    public static List<InternalProjectConfigurator> from(List<ProjectConfiguratorContribution> configurators) {
        if (LOGGER.isScopeEnabled(CoreTraceScopes.PROJECT_CONFIGURATORS)) {
            LOGGER.trace(CoreTraceScopes.PROJECT_CONFIGURATORS, "Contributed configurators: " +
                    configurators.stream().map(Objects::toString).collect(Collectors.joining(", ")));
        }

        configurators = new ArrayList<>(configurators);
        filterInvalidConfigurators(configurators);
        filterDuplicateIds(configurators);
        filterInvalidDependencies(configurators);
        filterCylicDependencies(configurators);

        List<InternalProjectConfigurator> internalConfigurators = configurators.stream().map(c -> new InternalProjectConfigurator(c)).collect(Collectors.toList());
        Collections.sort(internalConfigurators);

        return internalConfigurators;
    }

    private static void filterInvalidConfigurators(List<ProjectConfiguratorContribution> configurators) {
        Iterator<ProjectConfiguratorContribution> it = configurators.iterator();
        while (it.hasNext()) {
            ProjectConfiguratorContribution configurator = it.next();
            if (configurator.getId() == null) {
                LOGGER.trace(CoreTraceScopes.PROJECT_CONFIGURATORS, configurator.getId() + " was removed because it had no ID defined");
                it.remove();
            } else {
                try {
                    configurator.createConfigurator();
                } catch (Exception e) {
                    LOGGER.trace(CoreTraceScopes.PROJECT_CONFIGURATORS, configurator.getId() + " was removed because configurator instance cannot be created", e);
                    it.remove();
                }
            }
        }
    }

    private static void filterDuplicateIds(List<ProjectConfiguratorContribution> configurators) {
        Set<String> ids = Sets.newHashSet();
        Iterator<ProjectConfiguratorContribution> it = configurators.iterator();
        while (it.hasNext()) {
            ProjectConfiguratorContribution configurator = it.next();
            String id = configurator.getId();
            if (ids.contains(id)) {
                LOGGER.trace(CoreTraceScopes.PROJECT_CONFIGURATORS, configurator.getId() + " was removed because another configurator already exists with same ID");
                it.remove();
            } else {
                ids.add(id);
            }
        }
    }

    private static void filterInvalidDependencies(List<ProjectConfiguratorContribution> configurators) {
        Map<String, ProjectConfiguratorContribution> idToConfigurator = configurators.stream()
                .collect(Collectors.toMap(ProjectConfiguratorContribution::getId, Function.identity()));
        for (ProjectConfiguratorContribution configurator : configurators) {
            filterInvalidDependencies(idToConfigurator, configurator.getId(), configurator.getRunsBefore());
            filterInvalidDependencies(idToConfigurator, configurator.getId(), configurator.getRunsAfter());
        }
    }

    private static void filterInvalidDependencies(Map<String, ProjectConfiguratorContribution> idToConfigurator, String currentId, List<String> dependencyIds) {
        Iterator<String> it = dependencyIds.iterator();
        while (it.hasNext()) {
            String id = it.next();
            if (id.equals(currentId)) {
                LOGGER.trace(CoreTraceScopes.PROJECT_CONFIGURATORS, "Configurator " + currentId + " self-dependency removed");
                it.remove();
            } else if (idToConfigurator.get(id) == null) {
                it.remove();
                LOGGER.trace(CoreTraceScopes.PROJECT_CONFIGURATORS, "Configurator dependency (" + currentId + " -> " + id + ") removed because dependency does not exist");
            }
        }
    }

    private static void filterCylicDependencies(List<ProjectConfiguratorContribution> configurators) {
        Map<String, ProjectConfiguratorContribution> idToConfigurator = configurators.stream()
                .collect(Collectors.toMap(ProjectConfiguratorContribution::getId, Function.identity()));
        MutableGraph<ProjectConfiguratorContribution> dependencyGraph = GraphBuilder.directed().nodeOrder(ElementOrder.insertion()).build();
        configurators.forEach(c -> dependencyGraph.addNode(c));
        for (ProjectConfiguratorContribution configurator : configurators) {
            filterCylicDependencies(configurator, configurator.getRunsBefore(), idToConfigurator, dependencyGraph, false);
            filterCylicDependencies(configurator, configurator.getRunsAfter(), idToConfigurator, dependencyGraph, true);
        }
    }

    private static void filterCylicDependencies(ProjectConfiguratorContribution configurator, List<String> dependencyIds,
            Map<String, ProjectConfiguratorContribution> idToConfigurator, MutableGraph<ProjectConfiguratorContribution> dependencyGraph,
            boolean reverseDirection) {
        Iterator<String> it = dependencyIds.iterator();
        while (it.hasNext()) {
            ProjectConfiguratorContribution otherConfigurator = idToConfigurator.get(it.next());
            ProjectConfiguratorContribution source = reverseDirection ? otherConfigurator : configurator;
            ProjectConfiguratorContribution target = reverseDirection ? configurator : otherConfigurator;
            dependencyGraph.putEdge(source, target);
            if (Graphs.hasCycle(dependencyGraph)) {
                LOGGER.trace(CoreTraceScopes.PROJECT_CONFIGURATORS, "Configurator dependency (" + source.getId() + " -> " + target.getId() + ") removed because it introduces dependency cycle");
                dependencyGraph.removeEdge(source, target);
                it.remove();
            }
        }
    }

    @Override
    public int compareTo(InternalProjectConfigurator that) {
        if (this.runsBefore(that) || that.runsAfter(this)) {
            return -1;
        } else if (this.runsAfter(that) || that.runsBefore(this)) {
            return 1;
        } else {
            return 0;
        }
    }

    private boolean runsBefore(InternalProjectConfigurator that) {
        return this.contribution.getRunsBefore().contains(that.getId());
    }

    private boolean runsAfter(InternalProjectConfigurator that) {
        return this.contribution.getRunsAfter().contains(that.getId());
    }
}
