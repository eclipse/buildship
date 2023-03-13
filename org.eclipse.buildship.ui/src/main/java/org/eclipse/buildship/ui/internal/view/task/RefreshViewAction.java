/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view.task;

import org.eclipse.buildship.ui.internal.PluginImage.ImageState;
import org.eclipse.buildship.ui.internal.PluginImages;
import org.eclipse.buildship.ui.internal.util.action.CommandBackedAction;

/**
 * An action on the {@link TaskView} to reload/refresh the content of the task view.
 */
public final class RefreshViewAction extends CommandBackedAction {

    @SuppressWarnings("cast")
    public RefreshViewAction(String commandId) {
        super(commandId);

        setText(TaskViewMessages.Action_Refresh_Text);
        setToolTipText(TaskViewMessages.Action_Refresh_Tooltip);
        setImageDescriptor(PluginImages.REFRESH.withState(ImageState.ENABLED).getImageDescriptor());
        setDisabledImageDescriptor(PluginImages.REFRESH.withState(ImageState.DISABLED).getImageDescriptor());
    }

}
