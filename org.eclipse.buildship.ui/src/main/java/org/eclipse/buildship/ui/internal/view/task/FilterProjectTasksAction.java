/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view.task;

import com.google.common.base.Preconditions;

import org.eclipse.jface.action.Action;

import org.eclipse.buildship.ui.internal.PluginImage;
import org.eclipse.buildship.ui.internal.PluginImages;

/**
 * An action on the {@link TaskView} to include/exclude the project task nodes in the filter
 * criteria.
 */
public final class FilterProjectTasksAction extends Action {

    private final TaskView taskViewer;

    public FilterProjectTasksAction(TaskView taskViewer) {
        super(null, AS_CHECK_BOX);
        this.taskViewer = Preconditions.checkNotNull(taskViewer);

        setText(TaskViewMessages.Action_FilterProjectTasks_Text);
        setImageDescriptor(PluginImages.PROJECT_TASK.withState(PluginImage.ImageState.ENABLED).getImageDescriptor());
        setChecked(taskViewer.getState().isProjectTasksVisible());
    }

    @Override
    public void run() {
        this.taskViewer.getState().setProjectTasksVisible(isChecked());
        this.taskViewer.getTreeViewer().refresh();
    }

}
