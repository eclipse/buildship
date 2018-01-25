/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel.internal;

import java.util.Comparator;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.GradleTask;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeBasedTable;

import org.eclipse.buildship.core.omnimodel.OmniBuildInvocations;
import org.eclipse.buildship.core.omnimodel.OmniProjectTask;
import org.eclipse.buildship.core.omnimodel.OmniTaskSelector;
import org.eclipse.buildship.core.util.gradle.Path;

/**
 * Builds a {@code DefaultOmniBuildInvocationsContainer} from a given Gradle project.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniBuildInvocationsContainerBuilder {

    /**
     * Converts a {@link GradleProject} to a {@link DefaultOmniBuildInvocationsContainer}.
     *
     * @param project the Gradle project to convert
     * @return the build invocations container
     */
    public static DefaultOmniBuildInvocationsContainer build(GradleProject project) {
        ImmutableMultimap<Path, OmniProjectTask> tasks = getAllProjectTasksByProjectPath(project);
        ImmutableMultimap<Path, OmniTaskSelector> taskSelectors = getAllTaskSelectorsByProjectPath(project);
        ImmutableSortedMap<Path, OmniBuildInvocations> buildInvocationsPerProject = buildBuildInvocationsMapping(project, tasks, taskSelectors);
        return DefaultOmniBuildInvocationsContainer.from(buildInvocationsPerProject);
    }

    private static ImmutableSortedMap<Path, OmniBuildInvocations> buildBuildInvocationsMapping(GradleProject project,
                                                                                               Multimap<Path, OmniProjectTask> projectTasks,
                                                                                               Multimap<Path, OmniTaskSelector> taskSelectors) {
        Preconditions.checkState(taskSelectors.keySet().containsAll(projectTasks.keySet()), "Task selectors are always configured for all projects");

        // create mappings for all projects which contain tasks selectors (which covers at least those projects that contain project tasks)
        ImmutableSortedMap.Builder<Path, OmniBuildInvocations> mapping = ImmutableSortedMap.orderedBy(Path.Comparator.INSTANCE);
        for (Path projectPath : taskSelectors.keySet()) {
            ImmutableList<OmniProjectTask> projectTasksOfProject = ImmutableSortedSet.orderedBy(TaskComparator.INSTANCE).addAll(projectTasks.get(projectPath)).build().asList();
            ImmutableList<OmniTaskSelector> taskSelectorsOfProject = ImmutableSortedSet.orderedBy(TaskSelectorComparator.INSTANCE).addAll(taskSelectors.get(projectPath)).build().asList();
            mapping.put(projectPath, DefaultOmniBuildInvocations.from(projectTasksOfProject, taskSelectorsOfProject));
        }

        // create additional mappings for all those projects which do not contain any task selectors
        // this is the case if a project does not contain any tasks nor does any of its child projects
        // these additional mappings ensure the caller never gets back null for any project in the hierarchy
        Set<Path> projectsWithoutTaskSelectors = Sets.difference(getAllProjectPaths(project), taskSelectors.keySet());
        for (Path projectPath : projectsWithoutTaskSelectors) {
            mapping.put(projectPath, DefaultOmniBuildInvocations.from(ImmutableList.<OmniProjectTask>of(), ImmutableList.<OmniTaskSelector>of()));
        }

        return mapping.build();
    }

    private static ImmutableSet<Path> getAllProjectPaths(GradleProject project) {
        ImmutableSet.Builder<Path> projectPaths = ImmutableSet.builder();
        projectPaths.add(Path.from(project.getPath()));

        for (GradleProject childProject : project.getChildren()) {
            projectPaths.addAll(getAllProjectPaths(childProject));
        }
        return projectPaths.build();
    }

    private static ImmutableMultimap<Path, OmniProjectTask> getAllProjectTasksByProjectPath(GradleProject project) {
        Builder<Path, OmniProjectTask> tasks = ImmutableMultimap.builder();

        for (GradleTask task : project.getTasks()) {
            tasks.put(Path.from(project.getPath()), DefaultOmniProjectTask.from(task));
        }

        for (GradleProject child : project.getChildren()) {
            tasks.putAll(getAllProjectTasksByProjectPath(child));
        }

        return tasks.build();
    }

    private static ImmutableMultimap<Path, OmniTaskSelector> getAllTaskSelectorsByProjectPath(GradleProject project) {
        Builder<Path, OmniTaskSelector> taskSelectors = ImmutableMultimap.builder();
        TreeBasedTable<String, Path, OmniProjectTask> tasksByNameAndPath = getAllProjectTasksByNameAndPath(project);

        for (String selectorName : tasksByNameAndPath.rowKeySet()) {
            SortedMap<Path, OmniProjectTask> tasksByPath = tasksByNameAndPath.row(selectorName);
            OmniProjectTask taskWithShortestPath = tasksByPath.get(tasksByPath.firstKey());
            boolean isPublic = Iterables.any(tasksByPath.values(), new Predicate<OmniProjectTask>() {

                @Override
                public boolean apply(OmniProjectTask input) {
                    return input.isPublic();
                }
            });
            SortedSet<Path> selectedPaths = ImmutableSortedSet.copyOf(tasksByPath.comparator(), tasksByPath.keySet());

            OmniTaskSelector taskSelector = DefaultOmniTaskSelector.from(
                    selectorName,
                    taskWithShortestPath.getDescription(),
                    Path.from(project.getPath()),
                    isPublic,
                    taskWithShortestPath.getGroup(),
                    selectedPaths);

            taskSelectors.put(Path.from(project.getPath()), taskSelector);
        }

        for (GradleProject childProject : project.getChildren()) {
            taskSelectors.putAll(getAllTaskSelectorsByProjectPath(childProject));
        }

        return taskSelectors.build();
    }

    private static TreeBasedTable<String, Path, OmniProjectTask> getAllProjectTasksByNameAndPath(GradleProject project) {
        TreeBasedTable<String, Path, OmniProjectTask> tasks = TreeBasedTable.create(Ordering.natural(), Path.Comparator.INSTANCE);
        for (GradleTask task : project.getTasks()) {
            OmniProjectTask projectTask = DefaultOmniProjectTask.from(task);
            tasks.put(projectTask.getName(), projectTask.getPath(), projectTask);
        }

        for (GradleProject childProject : project.getChildren()) {
            tasks.putAll(getAllProjectTasksByNameAndPath(childProject));
        }
        return tasks;
    }

    /**
     * Singleton comparator to compare {@code OmniProjectTask} instances by their name.
     */
    private enum TaskComparator implements Comparator<OmniProjectTask> {

        INSTANCE;

        @Override
        public int compare(OmniProjectTask o1, OmniProjectTask o2) {
            return o1.getName().compareTo(o2.getName());
        }

    }

    /**
     * Singleton comparator to compare {@code OmniTaskSelector} instances by their name.
     */
    private enum TaskSelectorComparator implements Comparator<OmniTaskSelector> {

        INSTANCE;

        @Override
        public int compare(OmniTaskSelector o1, OmniTaskSelector o2) {
            return o1.getName().compareTo(o2.getName());
        }

    }

}
