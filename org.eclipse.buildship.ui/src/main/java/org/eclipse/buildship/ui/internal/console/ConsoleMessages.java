/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

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
