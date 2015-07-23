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

package org.eclipse.buildship.ui.util.selection;

import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Dynamically adds a given {@link ContextActivatingSelectionListener} when the window is opened.
 */
public final class ContextActivatingWindowListener implements IWindowListener {

    private final ContextActivatingSelectionListener contextActivatingSelectionListener;

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
    public void windowOpened(IWorkbenchWindow window) {
        window.getSelectionService().addSelectionListener(this.contextActivatingSelectionListener);
    }

    @Override
    public void windowClosed(IWorkbenchWindow window) {
        window.getSelectionService().removeSelectionListener(this.contextActivatingSelectionListener);
    }

}
