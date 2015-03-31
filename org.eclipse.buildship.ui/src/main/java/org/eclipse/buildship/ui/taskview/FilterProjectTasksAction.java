/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.taskview;

import org.eclipse.buildship.ui.PluginImage;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.jface.action.Action;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * An action on the {@link TaskView} to include/exclude the project task nodes in the filter
 * criteria.
 */
public final class FilterProjectTasksAction extends Action {

    private final TaskView taskViewer;

    public FilterProjectTasksAction(TaskView taskViewer) {
        super(null, AS_CHECK_BOX);
        this.taskViewer = Preconditions.checkNotNull(taskViewer);

        setToolTipText(TaskViewMessages.Action_FilterProjectTasks_Tooltip);
        setImageDescriptor(PluginImages.TASK.withState(PluginImage.ImageState.ENABLED).getOverlayImageDescriptor(
                ImmutableList.of(PluginImages.OVERLAY_PROJECT_TASK.withState(PluginImage.ImageState.ENABLED))));
        setChecked(taskViewer.getState().isProjectTasksVisible());
    }

    @Override
    public void run() {
        this.taskViewer.getState().setProjectTasksVisible(isChecked());
        this.taskViewer.getTreeViewer().setFilters(TaskNodeViewerFilter.createFor(this.taskViewer.getState()));
    }

}
