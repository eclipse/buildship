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

package org.eclipse.buildship.ui.wizard.task;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.WritableMap;

import org.eclipse.buildship.core.model.taskmetadata.TaskProperty;
import org.eclipse.buildship.core.model.taskmetadata.TaskType;
import org.eclipse.buildship.ui.part.execution.model.AbstractModelObject;
import org.eclipse.buildship.ui.wizard.task.CreateTaskTypeWizardMainPage.NullableTaskType;

/**
 * This model contains all information, which is configured by the NewTaskWizard.
 *
 */
public class TaskCreationModel extends AbstractModelObject {

    public static final String FIELD_TASKNAME = "taskName"; //$NON-NLS-1$
    public static final String FIELD_TASKTYPE = "taskType"; //$NON-NLS-1$
    public static final String FIELD_TASKPROPERTYVALUES = "taskPropertyValues"; //$NON-NLS-1$

    private String taskName;

    private TaskType taskType;

    private IObservableMap taskPropertyValues = new WritableMap(TaskProperty.class, String.class);

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        firePropertyChange(FIELD_TASKNAME, this.taskName, this.taskName = taskName);
    }

    public TaskType getTaskType() {
        if (taskType instanceof NullableTaskType) {
            return null;
        }
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        firePropertyChange(FIELD_TASKTYPE, this.taskType, this.taskType = taskType);
    }

    public IObservableMap getTaskPropertyValues() {
        return taskPropertyValues;
    }

    public void setTaskPropertyValues(IObservableMap taskPropertyValues) {
        firePropertyChange(FIELD_TASKPROPERTYVALUES, this.taskPropertyValues, this.taskPropertyValues = taskPropertyValues);
    }

    public String getTaskTypeFunction() {

        StringBuilder sb = new StringBuilder();
        sb.append("task");
        sb.append(" ");
        sb.append(taskName);
        if (getTaskType() != null) {
            sb.append("(");
            sb.append("type:");
            sb.append(" ");
            sb.append(taskType.getClassName());
            sb.append(") {");
        }
        sb.append(System.lineSeparator());
        for (Object properties : taskPropertyValues.entrySet()) {
            if (properties instanceof Map.Entry<?, ?>) {
                Map.Entry<?, ?> entry = (Entry<?, ?>) properties;
                String value = (String) entry.getValue();
                if (value != null && !value.isEmpty()) {
                    sb.append(((TaskProperty) entry.getKey()).getName());
                    sb.append(" \'");
                    sb.append(value);
                    sb.append("\'");
                    sb.append(System.lineSeparator());
                }
            }
        }
        sb.append("}");

        return sb.toString();
    }
}
