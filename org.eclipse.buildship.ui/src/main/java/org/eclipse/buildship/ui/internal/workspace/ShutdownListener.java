/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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
