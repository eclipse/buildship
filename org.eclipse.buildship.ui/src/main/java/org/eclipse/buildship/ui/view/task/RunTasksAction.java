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

package org.eclipse.buildship.ui.view.task;

import org.eclipse.buildship.ui.PluginImage;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.buildship.ui.util.nodeselection.NodeSelection;
import org.eclipse.buildship.ui.util.nodeselection.SelectionSpecificAction;
import org.eclipse.buildship.ui.view.CommandBackedAction;

/**
 * Runs the selected Gradle tasks.
 */
public final class RunTasksAction extends CommandBackedAction implements SelectionSpecificAction {

    @SuppressWarnings("cast")
    public RunTasksAction(String commandId) {
        super(commandId);

        setText(TaskViewMessages.Action_RunTasks_Text);
        setToolTipText(TaskViewMessages.Action_RunTasks_Tooltip);
        setImageDescriptor(PluginImages.RUN_TASKS.withState(PluginImage.ImageState.ENABLED).getImageDescriptor());
    }

    @Override
    public boolean isVisibleFor(NodeSelection selection) {
        return TaskViewActionStateRules.taskScopedTaskExecutionActionsVisibleFor(selection);
    }

    @Override
    public boolean isEnabledFor(NodeSelection selection) {
        return TaskViewActionStateRules.taskScopedTaskExecutionActionsEnabledFor(selection);
    }

    @Override
    public void setEnabledFor(NodeSelection selection) {
        boolean isEnabled = isEnabledFor(selection);
        setEnabled(isEnabled);
        if (isEnabled) {
            setText(TaskViewMessages.Action_RunTasks_Text);
            setImageDescriptor(PluginImages.RUN_TASKS.withState(PluginImage.ImageState.ENABLED).getImageDescriptor());
        } else {
            setText(TaskViewMessages.Action_RunTasks_Text_Disabled);
            setImageDescriptor(PluginImages.RUN_TASKS.withState(PluginImage.ImageState.DISABLED).getImageDescriptor());
        }
    }

}
