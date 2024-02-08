/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view;

import org.eclipse.jface.action.Action;

import org.eclipse.buildship.ui.internal.i18n.UiMessages;
import org.eclipse.buildship.ui.internal.util.nodeselection.NodeSelection;
import org.eclipse.buildship.ui.internal.util.nodeselection.SelectionSpecificAction;

/**
 * Action to show in the context menu when no other actions are available.
 *
 * @author Andy Wu <andy.wu@liferay.com>
 */
public final class NoActionsAvailableAction extends Action implements SelectionSpecificAction {

    public NoActionsAvailableAction() {
        setText(UiMessages.Action_NoActionsAvailable_Label);
        setEnabled(false);
    }

    @Override
    public boolean isVisibleFor(NodeSelection selection) {
        return false;
    }

    @Override
    public boolean isEnabledFor(NodeSelection selection) {
        return false;
    }

    @Override
    public void setEnabledFor(NodeSelection selection) {
    }

}
