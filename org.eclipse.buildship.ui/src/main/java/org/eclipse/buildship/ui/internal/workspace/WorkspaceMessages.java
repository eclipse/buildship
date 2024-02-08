/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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
