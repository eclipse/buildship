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

import org.eclipse.buildship.core.internal.launch.GradleLaunchConfigurationAttributes;
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

    protected GradleLaunchConfigurationAttributes getRunConfigurationAttributes(ExecutionEvent event) {
        NodeSelection selectionHistory = getSelectionHistory(event);
        return TaskNodeSelectionUtils.getRunConfigurationAttributes(selectionHistory);
    }

}
