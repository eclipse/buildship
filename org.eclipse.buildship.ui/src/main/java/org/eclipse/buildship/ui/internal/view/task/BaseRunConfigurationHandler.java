/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view.task;

import org.eclipse.buildship.core.internal.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.ui.internal.util.nodeselection.NodeSelection;
import org.eclipse.core.commands.ExecutionEvent;

/**
 * Base class for handlers that operate on the selected tasks.
 */
public abstract class BaseRunConfigurationHandler extends SelectionDependentHandler {

    @Override
    protected boolean isEnabledFor(NodeSelection selection) {
        return TaskNodeSelectionUtils.isValidRunConfiguration(selection);
    }

    protected GradleRunConfigurationAttributes getRunConfigurationAttributes(ExecutionEvent event) {
        NodeSelection selectionHistory = getSelectionHistory(event);
        return TaskNodeSelectionUtils.getRunConfigurationAttributes(selectionHistory);
    }

}
