/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

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
