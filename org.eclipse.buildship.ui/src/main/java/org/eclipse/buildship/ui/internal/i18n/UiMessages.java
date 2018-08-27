/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 *     Simon Scholz - Bug 465723
 */

package org.eclipse.buildship.ui.internal.i18n;

import org.eclipse.osgi.util.NLS;

/**
 * Lists the i18n resource keys for the ui messages.
 */
public final class UiMessages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.buildship.ui.internal.i18n.UiMessages"; //$NON-NLS-1$

    public static String Title_Select_0;

    public static String Button_Label_Browse;

    public static String Action_ExpandNodes_Tooltip;
    public static String Action_CollapseNodes_Tooltip;
    public static String Action_ShowFilter_Tooltip;
    public static String Action_CancelExecution_Tooltip;
    public static String Action_RerunBuild_Tooltip;
    public static String Action_RerunFailedTests_Tooltip;
    public static String Action_NoActionsAvailable_Label;

    public static String Button_CopyFailuresToClipboard_Tooltip;

    public static String Dialog_Title_Multiple_Errors;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, UiMessages.class);
    }

    private UiMessages() {
    }

}
