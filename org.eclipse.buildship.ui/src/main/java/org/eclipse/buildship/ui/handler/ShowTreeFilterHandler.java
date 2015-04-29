package org.eclipse.buildship.ui.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.buildship.ui.view.FilteredTreePart;
import org.eclipse.buildship.ui.view.executionview.ExecutionPartPreferences;
import org.eclipse.buildship.ui.viewer.FilteredTree;

public class ShowTreeFilterHandler extends AbstractToogleStateHandler {

    ExecutionPartPreferences prefs = new ExecutionPartPreferences();

    @Override
    protected boolean getToggleState() {
        return prefs.getFilterVisibile();
    }

    @Override
    protected Object doExecute(ExecutionEvent event) {

        // invert current filter visible state respectively the togglestate
        prefs.setFilterVisibile(!getToggleState());

        IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
        if (activePart instanceof FilteredTreePart) {
            FilteredTreePart filteredTreePart = (FilteredTreePart) activePart;
            FilteredTree filteredTree = filteredTreePart.getFilteredTree();
            filteredTree.setShowFilterControls(getToggleState());
        }

        return Status.OK_STATUS;
    }

}
