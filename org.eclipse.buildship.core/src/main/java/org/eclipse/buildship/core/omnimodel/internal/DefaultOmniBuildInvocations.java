/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel.internal;

import java.util.List;

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.Task;
import org.gradle.tooling.model.TaskSelector;
import org.gradle.tooling.model.gradle.BuildInvocations;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import org.eclipse.buildship.core.omnimodel.OmniBuildInvocations;
import org.eclipse.buildship.core.omnimodel.OmniProjectTask;
import org.eclipse.buildship.core.omnimodel.OmniTaskSelector;
import org.eclipse.buildship.core.util.gradle.Path;

/**
 * Default implementation of the {@link OmniBuildInvocations} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniBuildInvocations implements OmniBuildInvocations {

    private final ImmutableList<OmniProjectTask> projectTasks;
    private final ImmutableList<OmniTaskSelector> taskSelectors;

    private DefaultOmniBuildInvocations(List<OmniProjectTask> projectTasks, List<OmniTaskSelector> taskSelectors) {
        this.projectTasks = ImmutableList.copyOf(projectTasks);
        this.taskSelectors = ImmutableList.copyOf(taskSelectors);
    }

    @Override
    public ImmutableList<OmniProjectTask> getProjectTasks() {
        return this.projectTasks;
    }

    @Override
    public ImmutableList<OmniTaskSelector> getTaskSelectors() {
        return this.taskSelectors;
    }

    public static DefaultOmniBuildInvocations from(BuildInvocations buildInvocations, Path projectPath) {
        return new DefaultOmniBuildInvocations(
                createProjectTasks(buildInvocations.getTasks()),
                createTaskSelectors(buildInvocations.getTaskSelectors(), projectPath));
    }

    private static ImmutableList<OmniProjectTask> createProjectTasks(DomainObjectSet<? extends Task> projectTasks) {
        return FluentIterable.from(projectTasks).transform(new Function<Task, OmniProjectTask>() {
            @Override
            public OmniProjectTask apply(Task input) {
                return DefaultOmniProjectTask.from(input);
            }
        }).toList();
    }

    private static ImmutableList<OmniTaskSelector> createTaskSelectors(DomainObjectSet<? extends TaskSelector> taskSelectors, final Path projectPath) {
        return FluentIterable.from(taskSelectors).transform(new Function<TaskSelector, OmniTaskSelector>() {
            @Override
            public OmniTaskSelector apply(TaskSelector input) {
                return DefaultOmniTaskSelector.from(input, projectPath);
            }
        }).toList();
    }

    public static DefaultOmniBuildInvocations from(List<OmniProjectTask> projectTasks, List<OmniTaskSelector> taskSelectors) {
        return new DefaultOmniBuildInvocations(projectTasks, taskSelectors);
    }

}
