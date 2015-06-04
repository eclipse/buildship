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
import org.eclipse.buildship.ui.generic.CommandBackedAction;
import org.eclipse.buildship.ui.generic.NodeSelection;
import org.eclipse.buildship.ui.generic.SelectionSpecificAction;

/**
 * Runs the selected Gradle tasks.
 */
public final class RunTasksAction extends CommandBackedAction implements SelectionSpecificAction {

    @SuppressWarnings("cast")
    public RunTasksAction(String commandId) {
        super(commandId);

        setText(TasksViewMessages.Action_RunTasks_Text);
        setToolTipText(TasksViewMessages.Action_RunTasks_Tooltip);
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
        setEnabled(isEnabledFor(selection));
    }

}
