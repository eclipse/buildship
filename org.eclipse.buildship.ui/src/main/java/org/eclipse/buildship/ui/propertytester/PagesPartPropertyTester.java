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

package org.eclipse.buildship.ui.propertytester;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.ui.part.AbstractPagePart;
import org.eclipse.buildship.ui.part.ViewerProvider;

/**
 * This PropertyTester is used to determine, if an AbstractPagePart contains pages and also to
 * check, if the provided page contains a tree.
 *
 */
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
