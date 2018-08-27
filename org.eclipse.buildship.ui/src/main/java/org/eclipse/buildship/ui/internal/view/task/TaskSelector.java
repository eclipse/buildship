/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.internal.view.task;

import java.util.SortedSet;

import com.google.common.collect.ImmutableSortedSet;

import org.eclipse.buildship.core.internal.util.gradle.Path;

/**
 * A task that is executed on a target project and on all sub-projects where the same task is
 * peresent.
 *
 * @author Donat Csikos
 */
public class TaskSelector {

    private String name;
    private String description;
    private Path projectPath;
    private boolean isPublic;
    private String group;
    private ImmutableSortedSet<Path> selectedTaskPaths;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Path getProjectPath() {
        return this.projectPath;
    }

    public void setProjectPath(Path projectPath) {
        this.projectPath = projectPath;
    }

    public boolean isPublic() {
        return this.isPublic;
    }

    private void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getGroup() {
        return this.group;
    }

    private void setGroup(String group) {
        this.group = group;
    }

    public ImmutableSortedSet<Path> getSelectedTaskPaths() {
        return this.selectedTaskPaths;
    }

    public void setSelectedTaskPaths(SortedSet<Path> selectedTaskPaths) {
        this.selectedTaskPaths = ImmutableSortedSet.copyOfSorted(selectedTaskPaths);
    }

    public static TaskSelector from(String name, String description, Path projectPath, boolean isPublic, String group, SortedSet<Path> selectedTaskPaths) {
        TaskSelector taskSelector = new TaskSelector();
        taskSelector.setName(name);
        taskSelector.setDescription(description);
        taskSelector.setProjectPath(projectPath);
        taskSelector.setPublic(isPublic);
        taskSelector.setGroup(group);
        taskSelector.setSelectedTaskPaths(selectedTaskPaths);
        return taskSelector;
    }
}
