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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;

/**
 * This action can be used to show a certain {@link IConsole} in the console view, which is found by
 * the given targetConsolePageName.
 *
 */
public class ShowConsolePageAction extends Action {

    private String targetConsolePageName;

    public ShowConsolePageAction(String actionName, String actionTooltip, ImageDescriptor ImageDescriptor, String targetConsolePageName) {
        this.targetConsolePageName = Preconditions.checkNotNull(targetConsolePageName);

        setText(actionName);
        setToolTipText(actionTooltip);
        setImageDescriptor(ImageDescriptor);
    }

    @Override
    public void run() {
        IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
        IConsole[] consoles = consoleManager.getConsoles();
        for (IConsole console : consoles) {
            if (targetConsolePageName.equals(console.getName())) {
                consoleManager.showConsoleView(console);
                return;
            }
        }
    }
}
