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

package org.eclipse.buildship.ui.i18n;

import org.eclipse.osgi.util.NLS;

/**
 * Lists the i18n resource keys for the ui messages.
 */
public final class UiMessages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.buildship.ui.i18n.UiMessages"; //$NON-NLS-1$

    public static String Action_SwitchPage_Tooltip;
    public static String Action_ExpandNode_Tooltip;
    public static String Action_CollapseNode_Tooltip;

    public static String Action_ShowTreeHeader_Text;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, UiMessages.class);
    }

    private UiMessages() {
    }

}
