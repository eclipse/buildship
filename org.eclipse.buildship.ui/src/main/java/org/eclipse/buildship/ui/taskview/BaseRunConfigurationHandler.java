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

import org.eclipse.core.commands.ExecutionEvent;

import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.ui.generic.NodeSelection;

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
