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

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;

import org.eclipse.buildship.ui.internal.util.color.ColorUtils;

/**
 * Styled label provider for the task description column in the TaskView.
 */
public final class TaskDescriptionLabelProvider extends ColumnLabelProvider {

    private final Color descriptionColor;

    public TaskDescriptionLabelProvider() {
        this.descriptionColor = ColorUtils.getDecorationsColorFromCurrentTheme();
    }

    @Override
    public String getText(Object element) {
        if (element instanceof ProjectNode) {
            return getProjectTaskText((ProjectNode) element);
        } else if (element instanceof FaultyProjectNode) {
            return getFaultyProjectTaskText((FaultyProjectNode) element);
        } else if (element instanceof ProjectTaskNode) {
            return getProjectTaskText((ProjectTaskNode) element);
        } else if (element instanceof TaskSelectorNode) {
            return getTaskSelectorText((TaskSelectorNode) element);
        } else if (element instanceof TaskGroupNode) {
            return getTaskGroupText((TaskGroupNode) element);
        } else {
            throw new IllegalStateException(String.format("Unknown element type of element %s.", element));
        }
    }

    private String getProjectTaskText(ProjectNode projectNode) {
        return projectNode.getEclipseProject().getDescription();
    }

    private String getFaultyProjectTaskText(FaultyProjectNode projectNode) {
        return "";
    }

    private String getTaskGroupText(TaskGroupNode taskGroup) {
        return "";
    }

    private String getProjectTaskText(ProjectTaskNode projectTask) {
        return projectTask.getDescription();
    }

    private String getTaskSelectorText(TaskSelectorNode taskSelector) {
        return taskSelector.getDescription();
    }

    @Override
    public Color getForeground(Object element) {
        return this.descriptionColor;
    }

}
