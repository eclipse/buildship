package org.eclipse.buildship.ui.view.execution;

import com.google.common.base.Preconditions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;


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
