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

/**
 * This class creates some example {@link GradleTaskMetaData}.
 */
public class GradleTaskMetaDataManager {

    private GradleTaskMetaData taskTypes = new GradleTaskMetaData();

    /**
     * Creates some example {@link TaskType}s.
     */
    public GradleTaskMetaDataManager(){

        taskTypes.addTaskType(getCopyTaskType());
        taskTypes.addTaskType(getDeleteTaskType());
        taskTypes.addTaskType(getExecTaskType());

    }

    /**
     * Returns the copy, delete and exec task types with some example {@link TaskProperty}s.
     * @return {@link GradleTaskMetaData}
     */
    public GradleTaskMetaData getTaskMetaData(){
        return taskTypes;
    }

    /**
     * Returns a copy task types with some example {@link TaskProperty}s.
     * @return {@link TaskType}
     */
    private TaskType getCopyTaskType() {
        TaskType copy = new TaskType("Copy");
        copy.addTaskProperty(new TaskProperty("caseSensitive", "Specifies whether case-sensitive pattern matching should be used."));
        copy.addTaskProperty(new TaskProperty("destinationDir", "The directory to copy files into."));
        copy.addTaskProperty(new TaskProperty("dirMode", "The Unix permissions to use for the target directories. null means that existing permissions are preserved. It is dependent on the copy action implementation whether these permissions will actually be applied."));
        return copy;
    }

    /**
     * Returns a delete task types with some example {@link TaskProperty}s.
     * @return {@link TaskType}
     */
    private TaskType getDeleteTaskType() {
        TaskType delete = new TaskType("Delete");
        delete.addTaskProperty(new TaskProperty("delete", "The set of files which will be deleted by this task."));
        delete.addTaskProperty(new TaskProperty("targetFiles", "The resolved set of files which will be deleted by this task."));
        return delete;
    }

    /**
     * Returns a exec task types with some example {@link TaskProperty}s.
     * @return {@link TaskType}
     */
    private TaskType getExecTaskType() {
        TaskType exec = new TaskType("Exec");
        exec.addTaskProperty(new TaskProperty("args", "The arguments for the command to be executed. Defaults to an empty list."));
        exec.addTaskProperty(new TaskProperty("commandLine", "The full command line, including the executable plus its arguments."));
        exec.addTaskProperty(new TaskProperty("environment", "The environment variables to use for the process. Defaults to the environment of this process."));
        return exec;
    }

}
