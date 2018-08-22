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
