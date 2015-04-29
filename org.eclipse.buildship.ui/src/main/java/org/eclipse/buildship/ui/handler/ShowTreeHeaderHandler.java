package org.eclipse.buildship.ui.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.buildship.ui.view.FilteredTreePart;
import org.eclipse.buildship.ui.view.executionview.ExecutionPartPreferences;

public class ShowTreeHeaderHandler extends AbstractToogleStateHandler {

    ExecutionPartPreferences prefs = new ExecutionPartPreferences();

    @Override
    protected boolean getToggleState() {
        return prefs.getHeaderVisibile();
    }

    @Override
    protected Object doExecute(ExecutionEvent event) {

        // invert current header visible state respectively the togglestate
        prefs.setHeaderVisibile(!getToggleState());

        IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
        if (activePart instanceof FilteredTreePart) {
            FilteredTreePart filteredTreePart = (FilteredTreePart) activePart;
            Tree tree = filteredTreePart.getTreeViewer().getTree();
            tree.setHeaderVisible(getToggleState());
        }

        return Status.OK_STATUS;
    }

}
