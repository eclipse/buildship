/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.view.task;

import com.google.common.base.Preconditions;

import org.eclipse.jface.action.Action;

/**
 * An action on the {@link TaskView} whether to present the tasks grouped by their task groups.
 */
public final class GroupTasksByTaskGroupAction extends Action {

    private TaskView taskView;

    public GroupTasksByTaskGroupAction(TaskView taskView) {
        super(null, AS_CHECK_BOX);
        this.taskView = Preconditions.checkNotNull(taskView);

        setText(TaskViewMessages.Action_GroupTasksByTaskGroup_Text);
        setChecked(this.taskView.getState().isGroupTasksByTaskGroup());
    }

    @Override
    public void run() {
        this.taskView.getState().setGroupTasksByTaskGroup(isChecked());
        this.taskView.refresh();
    }

}
