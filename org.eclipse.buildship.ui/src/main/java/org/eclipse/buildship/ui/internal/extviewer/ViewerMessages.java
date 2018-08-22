/*******************************************************************************
 * Copyright (c) 2014 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - Initial API and implementation based on WorkbenchSWTMessages
 *******************************************************************************/

package org.eclipse.buildship.ui.internal.extviewer;

import org.eclipse.osgi.util.NLS;

/**
 * Based on org.eclipse.ui.internal.WorkbenchMessages.
 */
public class ViewerMessages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.buildship.ui.internal.extviewer.ViewerMessages";//$NON-NLS-1$

    public static String FilteredTree_AccessibleListenerClearButton;
    public static String FilteredTree_ClearToolTip;
    public static String FilteredTree_FilterMessage;
    public static String FilteredTree_AccessibleListenerFiltered;

    static {
        // load message values from bundle file
        reloadMessages();
    }

    public static void reloadMessages() {
        NLS.initializeMessages(BUNDLE_NAME, ViewerMessages.class);
    }
}
