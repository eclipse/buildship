/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view.task;

import org.gradle.tooling.model.Task;

import org.eclipse.buildship.core.internal.util.gradle.Path;

/**
 * A task that is executed only on a target project.
 *
 * @author Donat Csikos
 */
public class ProjectTask {

    private Task task;

    public ProjectTask(Task task) {
        this.task = task;
    }

    public Task getTask() {
        return this.task;
    };

    public String getName() {
        return this.task.getName();
    }

    public String getDescription() {
        return this.task.getDescription();
    }

    public Path getPath() {
        return Path.from(this.task.getPath());
    }

    public boolean isPublic() {
        return this.task.isPublic();
    }

    public String getGroup() {
        return this.task.getGroup();
    }

    public static ProjectTask from(Task task) {
        return new ProjectTask(task);
    }
}
