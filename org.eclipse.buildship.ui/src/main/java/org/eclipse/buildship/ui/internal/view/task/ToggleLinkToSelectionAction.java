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

import org.eclipse.buildship.ui.internal.PluginImage;
import org.eclipse.buildship.ui.internal.PluginImages;

/**
 * An action on the {@link TaskView} to toggle whether or not to link the selection in the task view
 * to the selection in the Explorer views.
 */
public final class ToggleLinkToSelectionAction extends Action {

    private final TaskView taskView;

    public ToggleLinkToSelectionAction(TaskView taskView) {
        super(null, AS_CHECK_BOX);
        this.taskView = Preconditions.checkNotNull(taskView);

        setToolTipText(TaskViewMessages.Action_LinkToSelection_Tooltip);
        setImageDescriptor(PluginImages.LINK_TO_SELECTION.withState(PluginImage.ImageState.ENABLED).getImageDescriptor());
        setChecked(taskView.getState().isLinkToSelection());
    }

    @Override
    public void run() {
        this.taskView.getState().setLinkToSelection(isChecked());
    }

}
