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
import org.eclipse.jface.action.IAction;

import org.eclipse.buildship.ui.internal.PluginImage;
import org.eclipse.buildship.ui.internal.PluginImages;

/**
 * An action on the {@link TaskView} to include the visibility of the task node in the sort
 * criteria.
 *
 * @see TaskNode#isPublic()
 */
public final class SortTasksByVisibilityAction extends Action {

    private final TaskView taskView;

    public SortTasksByVisibilityAction(TaskView taskView) {
        super(null, IAction.AS_CHECK_BOX);
        this.taskView = Preconditions.checkNotNull(taskView);

        setText(TaskViewMessages.Action_SortByVisibility_Text);
        setImageDescriptor(PluginImages.SORT_BY_VISIBILITY.withState(PluginImage.ImageState.ENABLED).getImageDescriptor());
        setChecked(taskView.getState().isSortByVisibility());
    }

    @Override
    public void run() {
        this.taskView.getState().setSortByVisibility(isChecked());
        this.taskView.getTreeViewer().setComparator(TaskNodeViewerSorter.createFor(this.taskView.getState()));
    }

}
