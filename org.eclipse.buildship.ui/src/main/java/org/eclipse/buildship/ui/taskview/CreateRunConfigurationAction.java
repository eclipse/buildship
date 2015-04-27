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

import com.google.common.base.Optional;

import org.eclipse.debug.core.ILaunchConfiguration;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.ui.generic.CommandBackedAction;
import org.eclipse.buildship.ui.generic.NodeSelection;
import org.eclipse.buildship.ui.generic.SelectionSpecificAction;

/**
 * Opens an existing run configuration for the selected Gradle tasks.
 */
public final class CreateRunConfigurationAction extends CommandBackedAction implements SelectionSpecificAction {

    public CreateRunConfigurationAction(String commandId) {
        super(commandId);
        setText(TaskViewMessages.Action_CreateRunConfiguration_Text);
        setToolTipText(TaskViewMessages.Action_CreateRunConfiguration_Tooltip);
    }

    @Override
    public boolean isVisibleFor(NodeSelection selection) {
        return checkEnablementAndUpdateLabel(selection);
    }

    @Override
    public boolean isEnabledFor(NodeSelection selection) {
        return checkEnablementAndUpdateLabel(selection);
    }

    private boolean checkEnablementAndUpdateLabel(NodeSelection selection) {
        Optional<GradleRunConfigurationAttributes> attributes = TaskNodeSelectionUtils.getRunConfigurationAttributes(selection);
        Optional<ILaunchConfiguration> launchConfiguration = attributes.isPresent() ? CorePlugin.gradleLaunchConfigurationManager().getRunConfiguration(attributes.get())
                : Optional.<ILaunchConfiguration> absent();
        return (TaskViewActionStateRules.taskScopedTaskExecutionActionsEnabledFor(selection) || TaskViewActionStateRules.projectScopedTaskExecutionActionsEnabledFor(selection)) && !launchConfiguration.isPresent();
    }
}
