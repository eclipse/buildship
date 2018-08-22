/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.internal.workspace;

import org.eclipse.osgi.util.NLS;

/**
 * Lists the i18n resource keys for the workspace messages.
 */
public class WorkspaceMessages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.buildship.ui.internal.workspace.WorkspaceMessages"; //$NON-NLS-1$
    public static String Action_RefreshProjectAction_Text;
    public static String Action_RefreshProjectAction_Tooltip;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, WorkspaceMessages.class);
    }

    private WorkspaceMessages() {
    }
}
