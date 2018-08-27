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

package org.eclipse.buildship.ui.internal.launch;

import org.eclipse.osgi.util.NLS;

/**
 * Lists the i18n resource keys for the launch messages.
 */
public final class LaunchMessages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.buildship.ui.internal.launch.LaunchMessages"; //$NON-NLS-1$

    public static String Tab_Name_GradleTasks;
    public static String Tab_Name_ProjectSettings;
    public static String Tab_Name_JavaHome;
    public static String Tab_Name_Arguments;

    public static String Test_Not_Found_Dialog_Details;
    public static String Test_Not_Found_Dialog_Message;
    public static String Test_Not_Found_Dialog_Title;

    public static String Title_BrowseFileSystemDialog;
    public static String Title_BrowseWorkspaceDialog;

    public static String Button_Label_BrowseFilesystem;
    public static String Button_Label_BrowseWorkspace;
    public static String Button_Label_SelectVariables;

    public static String ErrorMessage_CannotResolveExpression_0;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, LaunchMessages.class);
    }

    private LaunchMessages() {
    }

}
