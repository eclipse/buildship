package org.eclipse.buildship.ui.part;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.buildship.ui.part.execution.AbstractPagePart;
import org.eclipse.buildship.ui.part.pages.IPage;
import org.eclipse.buildship.ui.viewer.FilteredTree;


public abstract class FilteredTreePagePart extends AbstractPagePart implements FilteredTreeProvider {

    @Override
    public FilteredTree getFilteredTree() {
        IPage page = getCurrentPage();
        if (page instanceof FilteredTreeProvider) {
            return ((FilteredTreeProvider) page).getFilteredTree();
        }
        return null;
    }

    @Override
    public Viewer getViewer() {
        IPage page = getCurrentPage();
        if (page instanceof ViewerProvider) {
            return ((ViewerProvider) page).getViewer();
        }

        return null;
    }

    @Override
    public ISelectionProvider getSelectionProvider() {
        return getViewer();
    }

}
