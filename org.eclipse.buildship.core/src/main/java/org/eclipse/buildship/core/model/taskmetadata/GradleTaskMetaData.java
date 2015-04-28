/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Denis Zygann <d.zygann@web.de> - Bug 465728
 */
package org.eclipse.buildship.core.model.taskmetadata;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to store task types.
 */
public class GradleTaskMetaData {

    private List<TaskType> taskTypes;

    /**
     * Creates a new instance.
     */
    public GradleTaskMetaData() {
        taskTypes = new ArrayList<TaskType>();
    }

    /**
     * Adds a {@link TaskType}.
     * @param taskType {@link TaskType}
     */
    public void addTaskType(TaskType taskType ){
        taskTypes.add(taskType);
    }

    /**
     * Returns the stored {@link TaskType}s.
     * @return {@link List} contains {@link TaskType}s.
     */
    public List<TaskType> getTaskTypes(){
        return taskTypes;
    }
}
