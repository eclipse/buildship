/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 465728
 */

package org.eclipse.buildship.ui.wizard.task;

import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.WritableMap;
import org.eclipse.jface.text.templates.Template;

import org.eclipse.buildship.core.model.taskmetadata.TaskProperty;
import org.eclipse.buildship.core.model.taskmetadata.TaskType;
import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.buildship.ui.view.ObservableItem;

/**
 * This model contains all information, which is configured by the NewGradleTaskWizard.
 *
 */
public class TaskCreationModel extends ObservableItem {

    public static final String FIELD_TASKNAME = "taskName"; //$NON-NLS-1$
    public static final String FIELD_TASKTYPE = "taskType"; //$NON-NLS-1$
    public static final String FIELD_TASKPROPERTYVALUES = "taskPropertyValues"; //$NON-NLS-1$

    public static final String TASK_TEMPLATE_ID = "org.eclipse.buildship.ui.templates.task"; //$NON-NLS-1$
    public static final String TEMPLATE_TASKNAME = "${" + FIELD_TASKNAME + "}"; //$NON-NLS-1$
    public static final String TEMPLATE_TASKTYPE = "${" + FIELD_TASKTYPE + "}"; //$NON-NLS-1$
    public static final String TEMPLATE_TASKPROPERTYVALUES = "${" + FIELD_TASKPROPERTYVALUES + "}"; //$NON-NLS-1$

    private String taskName;

    private TaskType taskType = TaskType.DEFAULT_TASK_TYPE;

    private IObservableMap taskPropertyValues = new WritableMap(TaskProperty.class, String.class);

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        firePropertyChange(FIELD_TASKNAME, this.taskName, this.taskName = taskName);
    }

    public TaskType getTaskType() {
        if (TaskType.DEFAULT_TASK_TYPE.equals(taskType)) {
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

        // If the default task type is used simply return a default task
        if (TaskType.DEFAULT_TASK_TYPE.equals(getTaskType())) {
            return "task " + getTaskName() + " {" + System.lineSeparator() + "}";
        }

        // If it is not the TaskType.DEFAULT_TASK_TYPE we read the template for more complex tasks
        Template template = UiPlugin.templateService().getTemplateStore().findTemplateById(TASK_TEMPLATE_ID);

        String pattern = template.getPattern();

        // replace the variables in the template by actual values
        String templateResult = pattern.replace(TEMPLATE_TASKNAME, getTaskName());

        templateResult = templateResult.replace(TEMPLATE_TASKTYPE, getTaskType().getClassName());

        String propertiesString = getPropertiesString();
        templateResult = templateResult.replace(TEMPLATE_TASKPROPERTYVALUES, propertiesString);

        return templateResult;
    }

    private String getPropertiesString() {
        StringBuilder sb = new StringBuilder();
        for (Iterator<?> iterator = taskPropertyValues.entrySet().iterator(); iterator.hasNext();) {
            Entry<?, ?> entry = (Entry<?, ?>) iterator.next();
            String value = (String) entry.getValue();
            if (value != null && !value.isEmpty()) {
                sb.append("\t");
                sb.append(((TaskProperty) entry.getKey()).getName());
                sb.append(" = \'");
                sb.append(value);
                sb.append("\'");
                if (iterator.hasNext()) {
                    sb.append(System.lineSeparator());
                }
            }
        }

        return sb.toString();
    }
}
