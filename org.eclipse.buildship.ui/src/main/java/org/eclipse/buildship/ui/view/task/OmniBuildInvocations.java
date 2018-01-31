/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.view.task;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * TODO (donat) This class should be merged with ProjectTasks.
 * TODO (donat) migrate test cases from tooling-commons to the test plugins.
 * @author Donat Csikos
 */
public class OmniBuildInvocations {

    private final ImmutableList<OmniProjectTask> projectTasks;
    private final ImmutableList<OmniTaskSelector> taskSelectors;

    private OmniBuildInvocations(List<OmniProjectTask> projectTasks, List<OmniTaskSelector> taskSelectors) {
        this.projectTasks = ImmutableList.copyOf(projectTasks);
        this.taskSelectors = ImmutableList.copyOf(taskSelectors);
    }

    public ImmutableList<OmniProjectTask> getProjectTasks() {
        return this.projectTasks;
    }

    public ImmutableList<OmniTaskSelector> getTaskSelectors() {
        return this.taskSelectors;
    }

    public static OmniBuildInvocations from(List<OmniProjectTask> projectTasks, List<OmniTaskSelector> taskSelectors) {
        return new OmniBuildInvocations(projectTasks, taskSelectors);
    }

}
