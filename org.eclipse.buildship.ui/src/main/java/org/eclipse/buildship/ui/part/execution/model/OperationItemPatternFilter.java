package org.eclipse.buildship.ui.part.execution.model;

import org.eclipse.jface.viewers.Viewer;

import org.eclipse.buildship.ui.viewer.PatternFilter;


public class OperationItemPatternFilter extends PatternFilter {

    @Override
    protected boolean isLeafMatch(Viewer viewer, Object element) {
        if (element instanceof OperationItem) {
            String label = ((OperationItem) element).getLabel();

            return wordMatches(label);
        }

        return super.isLeafMatch(viewer, element);
    }
}
