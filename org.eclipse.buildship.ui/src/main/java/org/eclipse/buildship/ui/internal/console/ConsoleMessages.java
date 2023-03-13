/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.console;

import org.eclipse.osgi.util.NLS;

/**
 * Lists the i18n resource keys for the console messages.
 */
public final class ConsoleMessages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.buildship.ui.internal.console.ConsoleMessages"; //$NON-NLS-1$

    public static String Background_Console_Title;

    public static String Action_RemoveTerminatedConsole_Tooltip;
    public static String Action_RemoveAllTerminatedConsoles_Tooltip;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, ConsoleMessages.class);
    }

    private ConsoleMessages() {
    }

}
