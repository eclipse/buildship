package org.eclipse.buildship.ui.propertytester;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.ui.part.AbstractPagePart;
import org.eclipse.buildship.ui.part.ViewerProvider;

public class PagesPartPropertyTester extends PropertyTester {

    private enum Properties {
        hasPages, isTreePage
    }

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

        Properties propertyValue = Properties.valueOf(property);

        if (Properties.hasPages.equals(propertyValue)) {
            if (receiver instanceof AbstractPagePart && ((AbstractPagePart) receiver).hasPages()) {
                return true;
            }

            IViewPart targetVisibleViewPart = getTargetVisibleViewPart(args);
            if (targetVisibleViewPart instanceof AbstractPagePart && ((AbstractPagePart) targetVisibleViewPart).hasPages()) {
                return true;
            }

        } else if (Properties.isTreePage.equals(propertyValue)) {
            if (receiver instanceof ViewerProvider) {
                return ((ViewerProvider) receiver).getViewer() instanceof AbstractTreeViewer;
            }

            IViewPart targetVisibleViewPart = getTargetVisibleViewPart(args);
            if (targetVisibleViewPart instanceof ViewerProvider) {
                return ((ViewerProvider) targetVisibleViewPart).getViewer() instanceof AbstractTreeViewer;
            }

        }

        return false;
    }

    private IViewPart getTargetVisibleViewPart(Object[] args) {
        if (args != null && args.length > 0 && args[0] instanceof String) {
            String viewId = (String) args[0];
            IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            IViewPart findView = activePage.findView(viewId);
            if (activePage.isPartVisible(findView)) {
                return findView;
            }
        }
        return null;
    }
}
