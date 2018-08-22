/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.internal.workspace;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.workspace.WorkbenchShutdownEvent;

/**
 * Listener broadcasting {@link WorkbenchShutdownEvent} before Eclipse shuts down.
 *
 * @author Donat Csikos
 */
public final class ShutdownListener implements IWorkbenchListener {

    @Override
    public boolean preShutdown(IWorkbench workbench, boolean forced) {
        CorePlugin.listenerRegistry().dispatch(new WorkbenchShutdownEvent());
        return true; // if false then the workbench won't shut down
    }

    @Override
    public void postShutdown(IWorkbench workbench) {
    }

}
