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

import org.eclipse.buildship.ui.internal.PluginImage.ImageState;
import org.eclipse.buildship.ui.internal.PluginImages;

/**
 * An action on the {@link TaskView} to include/exclude the task selector nodes in the filter
 * criteria.
 */
public final class FilterTaskSelectorsAction extends Action {

    private final TaskView taskViewer;

    public FilterTaskSelectorsAction(TaskView taskViewer) {
        super(null, AS_CHECK_BOX);
        this.taskViewer = Preconditions.checkNotNull(taskViewer);

        setText(TaskViewMessages.Action_FilterTaskSelectors_Text);
        setImageDescriptor(PluginImages.TASK.withState(ImageState.ENABLED).getImageDescriptor());
        setChecked(taskViewer.getState().isTaskSelectorsVisible());
    }

    @Override
    public void run() {
        this.taskViewer.getState().setTaskSelectorsVisible(isChecked());
        this.taskViewer.getTreeViewer().refresh();
    }

}
