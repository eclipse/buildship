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

package com.gradleware.tooling.eclipse.ui.taskview;

import com.gradleware.tooling.eclipse.ui.PluginImage.ImageState;
import com.gradleware.tooling.eclipse.ui.PluginImages;
import com.gradleware.tooling.eclipse.ui.generic.CommandBackedAction;

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
