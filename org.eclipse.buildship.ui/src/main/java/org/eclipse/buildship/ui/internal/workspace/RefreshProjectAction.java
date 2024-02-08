/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.workspace;

import org.eclipse.buildship.ui.internal.PluginImage.ImageState;
import org.eclipse.buildship.ui.internal.PluginImages;
import org.eclipse.buildship.ui.internal.UiPluginConstants;
import org.eclipse.buildship.ui.internal.util.action.CommandBackedAction;

/**
 * Executes the refresh project command.
 *
 * @author Donat Csikos
 */
public final class RefreshProjectAction extends CommandBackedAction {

    public RefreshProjectAction() {
        super(UiPluginConstants.REFRESH_PROJECT_COMMAND_ID);
        setImageDescriptor(PluginImages.REFRESH.withState(ImageState.ENABLED).getImageDescriptor());
        setDisabledImageDescriptor(PluginImages.REFRESH.withState(ImageState.DISABLED).getImageDescriptor());
        setText(WorkspaceMessages.Action_RefreshProjectAction_Text);
        setToolTipText(WorkspaceMessages.Action_RefreshProjectAction_Tooltip);
    }
}
