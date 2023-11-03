/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view.execution;

import com.google.common.base.Preconditions;
import org.eclipse.buildship.ui.internal.PluginImage;
import org.eclipse.buildship.ui.internal.PluginImages;
import org.eclipse.buildship.ui.internal.view.Page;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;

/**
 * Navigates from the target {@link Page} to the corresponding {@link org.eclipse.ui.console.IConsole} page in the Console View.
 */
public final class SwitchToConsoleViewAction extends Action {

    private final Page page;

    public SwitchToConsoleViewAction(Page page) {
        this.page = Preconditions.checkNotNull(page);

        setToolTipText(ExecutionViewMessages.Action_SwitchToConsole_Tooltip);
        setImageDescriptor(PluginImages.SWITCH_TO_CONSOLE.withState(PluginImage.ImageState.ENABLED).getImageDescriptor());
    }

    @Override
    public void run() {
        IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
        for (IConsole console : consoleManager.getConsoles()) {
            if (this.page.getDisplayName().equals(console.getName())) {
                consoleManager.showConsoleView(console);
                return;
            }
        }
    }

}
