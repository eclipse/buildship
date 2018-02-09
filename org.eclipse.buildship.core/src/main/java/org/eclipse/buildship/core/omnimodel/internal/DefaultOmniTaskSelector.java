/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel.internal;

import java.util.SortedSet;

import org.gradle.tooling.model.TaskSelector;

import com.google.common.collect.ImmutableSortedSet;

import org.eclipse.buildship.core.omnimodel.OmniTaskSelector;
import org.eclipse.buildship.core.util.gradle.Maybe;
import org.eclipse.buildship.core.util.gradle.Path;

/**
 * Default implementation of the {@link OmniTaskSelector} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniTaskSelector implements OmniTaskSelector {

    private String name;
    private String description;
    private Path projectPath;
    private boolean isPublic;
    private Maybe<String> group;
    private ImmutableSortedSet<Path> selectedTaskPaths;

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Path getProjectPath() {
        return this.projectPath;
    }

    public void setProjectPath(Path projectPath) {
        this.projectPath = projectPath;
    }

    @Override
    public boolean isPublic() {
        return this.isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    @Override
    public Maybe<String> getGroup() {
        return this.group;
    }

    public void setGroup(Maybe<String> group) {
        this.group = group;
    }

    @Override
    public ImmutableSortedSet<Path> getSelectedTaskPaths() {
        return this.selectedTaskPaths;
    }

    public void setSelectedTaskPaths(SortedSet<Path> selectedTaskPaths) {
        this.selectedTaskPaths = ImmutableSortedSet.copyOfSorted(selectedTaskPaths);
    }

    public static DefaultOmniTaskSelector from(TaskSelector selector, Path projectPath) {
        DefaultOmniTaskSelector taskSelector = new DefaultOmniTaskSelector();
        taskSelector.setName(selector.getName());
        taskSelector.setDescription(selector.getDescription());
        taskSelector.setProjectPath(projectPath);
        taskSelector.setGroup(Maybe.<String>absent());
        setIsPublic(taskSelector, selector);
        taskSelector.setSelectedTaskPaths(ImmutableSortedSet.<Path>of());
        return taskSelector;
    }

    public static DefaultOmniTaskSelector from(String name, String description, Path projectPath, boolean isPublic, Maybe<String> group, SortedSet<Path> selectedTaskPaths) {
        DefaultOmniTaskSelector taskSelector = new DefaultOmniTaskSelector();
        taskSelector.setName(name);
        taskSelector.setDescription(description);
        taskSelector.setProjectPath(projectPath);
        taskSelector.setPublic(isPublic);
        taskSelector.setGroup(group);
        taskSelector.setSelectedTaskPaths(selectedTaskPaths);
        return taskSelector;
    }

    /**
     * TaskSelector#isPublic is only available in Gradle versions >= 2.1.
     *
     * @param gradleTaskSelector the task selector to populate
     * @param taskSelector the task selector model
     */
    private static void setIsPublic(DefaultOmniTaskSelector gradleTaskSelector, TaskSelector taskSelector) {
        try {
            boolean isPublic = taskSelector.isPublic();
            gradleTaskSelector.setPublic(isPublic);
        } catch (Exception ignore) {
            gradleTaskSelector.setPublic(true);
        }
    }

}
