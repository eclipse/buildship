package org.eclipse.buildship.ui.internal.view.execution;

import org.gradle.tooling.events.test.TestOutputDescriptor;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;


public class TestOutputFilter extends ViewerFilter {

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (parentElement instanceof OperationItem && element instanceof OperationItem) {
            OperationItem item = (OperationItem) element;

            if (item.getDescriptor() instanceof TestOutputDescriptor) {
                return false;
            }
        }
        return true;
    }
}
