/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 473389
 */

package org.eclipse.buildship.ui.util.window;

import org.eclipse.buildship.ui.util.selection.ContextActivatingSelectionListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * {@link IWindowListener}, which adds
 * {@link ContextActivatingSelectionListener} to a newly opened window.
 */
public class ContextActivatingWindowListener implements IWindowListener {

    private ContextActivatingSelectionListener contextActivatingSelectionListener;

    public ContextActivatingWindowListener(ContextActivatingSelectionListener contextActivatingSelectionListener) {
        this.contextActivatingSelectionListener = contextActivatingSelectionListener;
    }

    @Override
    public void windowActivated(IWorkbenchWindow window) {
    }

    @Override
    public void windowDeactivated(IWorkbenchWindow window) {
    }

    @Override
    public void windowClosed(IWorkbenchWindow window) {
    }

    @Override
    public void windowOpened(IWorkbenchWindow window) {
        window.getSelectionService().addSelectionListener(this.contextActivatingSelectionListener);
    }

}
