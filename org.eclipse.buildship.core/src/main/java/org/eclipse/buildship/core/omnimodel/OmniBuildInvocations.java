/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel;

import java.util.List;

/**
 * Provides information about the launchables (project tasks, task selectors) that can be used to initiate a Gradle build.
 *
 * @author Etienne Studer
 * @see org.gradle.tooling.model.gradle.BuildInvocations
 */
public interface OmniBuildInvocations {

    /**
     * Returns the tasks of this project.
     *
     * @return the tasks of this project
     */
    List<OmniProjectTask> getProjectTasks();

    /**
     * Returns the task selectors of this project.
     *
     * @return the task selectors of this project
     */
    List<OmniTaskSelector> getTaskSelectors();

}
