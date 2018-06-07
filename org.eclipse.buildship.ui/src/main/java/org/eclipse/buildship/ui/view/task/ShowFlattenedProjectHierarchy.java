/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.view.task;

import com.google.common.base.Preconditions;

import org.eclipse.buildship.ui.PluginImage;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.jface.action.Action;

/**
 * An action on the {@link TaskView} that toggles the project between flat and hierarchy.
 *
 * @author Charles Wu <charles.wu@liferay.com>
 */
public final class ShowFlattenedProjectHierarchy extends Action {

    private final TaskView taskView;

    public ShowFlattenedProjectHierarchy(TaskView taskView) {
        super(null, AS_CHECK_BOX);
        this.taskView = Preconditions.checkNotNull(taskView);

        setText(TaskViewMessages.Action_ShowFlattenProjectHiearchy);
        setImageDescriptor(PluginImages.PROJECT_GROUP.withState(PluginImage.ImageState.ENABLED).getImageDescriptor());
        setChecked(this.taskView.getState().isProjectHierarchyFlattened());
    }

    @Override
    public void run() {
        this.taskView.getState().setProjectHierarchyFlattened(isChecked());
        this.taskView.refresh();
    }

}
