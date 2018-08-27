/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.internal.view.task;

import java.util.List;
import java.util.Map;
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

import org.eclipse.buildship.core.internal.util.gradle.Path;

/**
 * Holds a set of tasks that belongs to one project.
 *
 * @author Etienne Studer
 */
class BuildInvocations {

    private final ImmutableList<ProjectTask> projectTasks;
    private final ImmutableList<TaskSelector> taskSelectors;

    private BuildInvocations(List<ProjectTask> projectTasks, List<TaskSelector> taskSelectors) {
        this.projectTasks = ImmutableList.copyOf(projectTasks);
        this.taskSelectors = ImmutableList.copyOf(taskSelectors);
    }

    public List<ProjectTask> getProjectTasks() {
        return this.projectTasks;
    }

    public List<TaskSelector> getTaskSelectors() {
        return this.taskSelectors;
    }

    public static Map<Path, BuildInvocations> collectAll(GradleProject project) {
        ImmutableMultimap<Path, ProjectTask> projectTasks = getAllProjectTasksByProjectPath(project);
        ImmutableMultimap<Path, TaskSelector> taskSelectors = getAllTaskSelectorsByProjectPath(project);
        return buildBuildInvocationsMapping(project, projectTasks, taskSelectors);
    }

    private static ImmutableMultimap<Path, ProjectTask> getAllProjectTasksByProjectPath(GradleProject project) {
        Builder<Path, ProjectTask> tasks = ImmutableMultimap.builder();

        for (GradleTask task : project.getTasks()) {
            tasks.put(Path.from(project.getPath()), ProjectTask.from(task));
        }

        for (GradleProject child : project.getChildren()) {
            tasks.putAll(getAllProjectTasksByProjectPath(child));
        }

        return tasks.build();
    }

    private static ImmutableMultimap<Path, TaskSelector> getAllTaskSelectorsByProjectPath(GradleProject project) {
        Builder<Path, TaskSelector> taskSelectors = ImmutableMultimap.builder();
        TreeBasedTable<String, Path, ProjectTask> tasksByNameAndPath = getAllProjectTasksByNameAndPath(project);

        for (String selectorName : tasksByNameAndPath.rowKeySet()) {
            SortedMap<Path, ProjectTask> tasksByPath = tasksByNameAndPath.row(selectorName);
            ProjectTask taskWithShortestPath = tasksByPath.get(tasksByPath.firstKey());
            boolean isPublic = Iterables.any(tasksByPath.values(), new Predicate<ProjectTask>() {

                @Override
                public boolean apply(ProjectTask input) {
                    return input.isPublic();
                }
            });
            SortedSet<Path> selectedPaths = ImmutableSortedSet.copyOf(tasksByPath.comparator(), tasksByPath.keySet());

            TaskSelector taskSelector = TaskSelector
                    .from(selectorName, taskWithShortestPath.getDescription(), Path.from(project.getPath()), isPublic, taskWithShortestPath.getGroup(), selectedPaths);

            taskSelectors.put(Path.from(project.getPath()), taskSelector);
        }

        for (GradleProject childProject : project.getChildren()) {
            taskSelectors.putAll(getAllTaskSelectorsByProjectPath(childProject));
        }

        return taskSelectors.build();
    }

    private static TreeBasedTable<String, Path, ProjectTask> getAllProjectTasksByNameAndPath(GradleProject project) {
        TreeBasedTable<String, Path, ProjectTask> tasks = TreeBasedTable.create(Ordering.natural(), Path.Comparator.INSTANCE);
        for (GradleTask task : project.getTasks()) {
            ProjectTask projectTask = ProjectTask.from(task);
            tasks.put(projectTask.getName(), projectTask.getPath(), projectTask);
        }

        for (GradleProject childProject : project.getChildren()) {
            tasks.putAll(getAllProjectTasksByNameAndPath(childProject));
        }
        return tasks;
    }

    private static ImmutableSortedMap<Path, BuildInvocations> buildBuildInvocationsMapping(GradleProject project, Multimap<Path, ProjectTask> projectTasks,
            Multimap<Path, TaskSelector> taskSelectors) {
        Preconditions.checkState(taskSelectors.keySet().containsAll(projectTasks.keySet()), "Task selectors are always configured for all projects");

        // create mappings for all projects which contain tasks selectors (which covers at least
        // those projects that contain project tasks)
        ImmutableSortedMap.Builder<Path, BuildInvocations> mapping = ImmutableSortedMap.orderedBy(Path.Comparator.INSTANCE);
        for (Path projectPath : taskSelectors.keySet()) {
            ImmutableList<ProjectTask> projectTasksOfProject = ImmutableList.copyOf(projectTasks.get(projectPath));
            ImmutableList<TaskSelector> taskSelectorsOfProject = ImmutableList.copyOf(taskSelectors.get(projectPath));
            mapping.put(projectPath, new BuildInvocations(projectTasksOfProject, taskSelectorsOfProject));
        }

        // create additional mappings for all those projects which do not contain any task selectors
        // this is the case if a project does not contain any tasks nor does any of its child
        // projects these additional mappings ensure the caller never gets back null for any project
        // in the hierarchy
        Set<Path> projectsWithoutTaskSelectors = Sets.difference(getAllProjectPaths(project), taskSelectors.keySet());
        for (Path projectPath : projectsWithoutTaskSelectors) {
            mapping.put(projectPath, new BuildInvocations(ImmutableList.<ProjectTask> of(), ImmutableList.<TaskSelector> of()));
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

}