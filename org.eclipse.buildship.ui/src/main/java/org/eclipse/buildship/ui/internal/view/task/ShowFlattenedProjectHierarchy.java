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

import org.eclipse.buildship.ui.internal.PluginImage;
import org.eclipse.buildship.ui.internal.PluginImages;
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
