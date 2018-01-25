/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel.internal;

import org.gradle.tooling.model.Task;

import org.eclipse.buildship.core.omnimodel.OmniProjectTask;
import org.eclipse.buildship.core.util.gradle.Maybe;
import org.eclipse.buildship.core.util.gradle.Path;

/**
 * Default implementation of the {@link OmniProjectTask} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniProjectTask implements OmniProjectTask {

    private String name;
    private String description;
    private Path path;
    private boolean isPublic;
    private Maybe<String> group;

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
    public Path getPath() {
        return this.path;
    }

    public void setPath(Path path) {
        this.path = path;
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

    public static DefaultOmniProjectTask from(Task task) {
        DefaultOmniProjectTask projectTask = new DefaultOmniProjectTask();
        projectTask.setName(task.getName());
        projectTask.setDescription(task.getDescription());
        projectTask.setPath(Path.from(task.getPath()));
        setIsPublic(projectTask, task);
        setGroup(projectTask, task);
        return projectTask;
    }

    /**
     * GradleTask#isPublic is only available in Gradle versions >= 2.1.
     * <p/>
     *
     * @param projectTask the task to populate
     * @param task the task model
     */
    private static void setIsPublic(DefaultOmniProjectTask projectTask, Task task) {
        try {
            boolean isPublic = task.isPublic();
            projectTask.setPublic(isPublic);
        } catch (Exception ignore) {
            projectTask.setPublic(true);
        }
    }

   /**
     * GradleTask#getGroup is only available in Gradle versions >= 2.5.
     *
     * @param projectTask the task to populate
     * @param task the task model
     */
    private static void setGroup(DefaultOmniProjectTask projectTask, Task task) {
        try {
            String group = task.getGroup();
            projectTask.setGroup(Maybe.of(group));
        } catch (Exception ignore) {
            projectTask.setGroup(Maybe.<String>absent());
        }
    }

}
