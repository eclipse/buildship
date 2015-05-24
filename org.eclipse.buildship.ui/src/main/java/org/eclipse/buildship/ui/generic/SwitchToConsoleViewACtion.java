/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.generic;

import com.google.common.base.Preconditions;
import org.eclipse.buildship.ui.PluginImage;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.buildship.ui.view.Page;
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

        setText("Show in console");
        setToolTipText("Shows the console of this build in the console view");
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
