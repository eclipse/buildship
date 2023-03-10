/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal;

/**
 * Help contexts registered via the <code>org.eclipse.help.contexts</code> extension point in the
 * plugin.xml.
 */
public final class HelpContext {

    // the help context ID has to be in the following format: ${PLUGIN_ID}.${CONTEXT_ID}
    // the context id is defined in the external (xml) file specified in the plugin.xml

    /**
     * The help context id of the project import.
     */
    public static final String PROJECT_IMPORT = UiPlugin.PLUGIN_ID + ".projectimport"; //$NON-NLS-1$

    /**
     * The help context id of the project creation.
     */
    public static final String PROJECT_CREATION = UiPlugin.PLUGIN_ID + ".projectcreation"; //$NON-NLS-1$

    private HelpContext() {
    }

}
