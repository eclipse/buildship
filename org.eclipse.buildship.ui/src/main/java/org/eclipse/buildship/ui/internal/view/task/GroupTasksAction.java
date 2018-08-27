/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.internal.view.task;

import com.google.common.base.Preconditions;

import org.eclipse.jface.action.Action;

import org.eclipse.buildship.ui.internal.PluginImage;
import org.eclipse.buildship.ui.internal.PluginImages;

/**
 * An action on the {@link TaskView} that toggles task grouping.
 */
public final class GroupTasksAction extends Action {

    private final TaskView taskView;

    public GroupTasksAction(TaskView taskView) {
        super(null, AS_CHECK_BOX);
        this.taskView = Preconditions.checkNotNull(taskView);

        setText(TaskViewMessages.Action_GroupTasks_Text);
        setImageDescriptor(PluginImages.TASK_GROUP.withState(PluginImage.ImageState.ENABLED).getImageDescriptor());
        setChecked(this.taskView.getState().isGroupTasks());
    }

    @Override
    public void run() {
        this.taskView.getState().setGroupTasks(isChecked());
        this.taskView.refresh();
    }

}
