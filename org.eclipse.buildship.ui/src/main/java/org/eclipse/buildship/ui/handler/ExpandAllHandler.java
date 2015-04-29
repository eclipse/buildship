package org.eclipse.buildship.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.buildship.ui.view.FilteredTreePart;


public class ExpandAllHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
        if (activePart instanceof FilteredTreePart) {
            FilteredTreePart filteredTreePart = (FilteredTreePart) activePart;
            TreeViewer treeViewer = filteredTreePart.getTreeViewer();
            treeViewer.expandAll();
        }

        return Status.OK_STATUS;
    }

}
