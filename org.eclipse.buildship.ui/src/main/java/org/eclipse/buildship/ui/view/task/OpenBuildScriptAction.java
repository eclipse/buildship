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

import org.eclipse.buildship.ui.util.action.CommandBackedAction;
import org.eclipse.buildship.ui.util.nodeselection.NodeSelection;
import org.eclipse.buildship.ui.util.nodeselection.SelectionSpecificAction;

/**
 * Opens the build script of the selected Gradle project.
 */
public final class OpenBuildScriptAction extends CommandBackedAction implements SelectionSpecificAction {

    public OpenBuildScriptAction(String commandId) {
        super(commandId);

        setText(TaskViewMessages.Action_OpenBuildScript_Text);
        setToolTipText(TaskViewMessages.Action_OpenBuildScript_Tooltip);
    }

    @Override
    public boolean isVisibleFor(NodeSelection selection) {
        return isEnabledFor(selection);
    }

    @Override
    public boolean isEnabledFor(NodeSelection selection) {
        return isEnabledForSelection(selection);
    }

    @Override
    public void setEnabledFor(NodeSelection selection) {
        setEnabled(isEnabledFor(selection));
    }

    public static boolean isEnabledForSelection(NodeSelection selection) {
        return !selection.isEmpty() && selection.hasAllNodesOfType(ProjectNode.class);
    }

}
