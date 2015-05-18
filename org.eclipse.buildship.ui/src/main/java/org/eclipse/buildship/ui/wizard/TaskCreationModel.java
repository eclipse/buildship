/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz - initial API and implementation and initial documentation
 */
package org.eclipse.buildship.ui.wizard;

import java.util.Map;

import com.google.common.collect.Maps;

import org.eclipse.buildship.core.model.taskmetadata.TaskProperty;
import org.eclipse.buildship.core.model.taskmetadata.TaskType;
import org.eclipse.buildship.ui.part.execution.model.AbstractModelObject;

/**
 * This model contains all information, which is configured by the NewTaskWizard.
 *
 */
public class TaskCreationModel extends AbstractModelObject {

    private String taskName;

    private TaskType taskType;

    private Map<TaskProperty, String> taskPropertyValues = Maps.newHashMap();

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        firePropertyChange("taskName", this.taskName, this.taskName = taskName);
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        firePropertyChange("taskType", this.taskType, this.taskType = taskType);
    }

    public Map<TaskProperty, String> getTaskPropertyValues() {
        return taskPropertyValues;
    }

    public void setTaskPropertyValues(Map<TaskProperty, String> taskPropertyValues) {
        firePropertyChange("taskPropertyValues", this.taskPropertyValues, this.taskPropertyValues = taskPropertyValues);
    }

    public String getTaskTypeFunction() {

        StringBuilder sb = new StringBuilder();
        sb.append("task");
        sb.append(" ");
        sb.append(taskName);
        sb.append("(");
        sb.append("type:");
        sb.append(" ");
        sb.append(taskType.getClassName());
        sb.append(") {");
        sb.append(System.lineSeparator());
        for (Map.Entry<TaskProperty, String> properties : taskPropertyValues.entrySet()) {
            sb.append(properties.getKey().getName());
            sb.append(" ");
            sb.append(properties.getValue());
            sb.append(System.lineSeparator());
        }
        sb.append("}");

        return sb.toString();
    }
}
