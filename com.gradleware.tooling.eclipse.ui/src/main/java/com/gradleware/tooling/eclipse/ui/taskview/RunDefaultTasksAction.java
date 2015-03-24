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

import com.gradleware.tooling.eclipse.ui.PluginImage;
import com.gradleware.tooling.eclipse.ui.PluginImages;
import com.gradleware.tooling.eclipse.ui.generic.CommandBackedAction;
import com.gradleware.tooling.eclipse.ui.generic.NodeSelection;
import com.gradleware.tooling.eclipse.ui.generic.SelectionSpecificAction;

/**
 * Runs the default tasks of the selected Gradle project.
 */
public final class RunDefaultTasksAction extends CommandBackedAction implements SelectionSpecificAction {

    @SuppressWarnings("cast")
    public RunDefaultTasksAction(String commandId) {
        super(commandId);

        setText(TaskViewMessages.Action_RunDefaultTasks_Text);
        setToolTipText(TaskViewMessages.Action_RunDefaultTasks_Tooltip);
        setImageDescriptor(PluginImages.RUN_TASKS.withState(PluginImage.ImageState.ENABLED).getImageDescriptor());
    }

    @Override
    public boolean isVisibleFor(NodeSelection selection) {
        return TaskViewActionStateRules.projectScopedTaskExecutionActionsVisibleFor(selection);
    }

    @Override
    public boolean isEnabledFor(NodeSelection selection) {
        return TaskViewActionStateRules.projectScopedTaskExecutionActionsEnabledFor(selection);
    }

}
