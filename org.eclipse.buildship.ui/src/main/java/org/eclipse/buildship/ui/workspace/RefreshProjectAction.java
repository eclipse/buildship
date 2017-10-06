/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.workspace;

import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.buildship.ui.UiPluginConstants;
import org.eclipse.buildship.ui.util.action.CommandBackedAction;

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
