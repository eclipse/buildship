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

package org.eclipse.buildship.ui.internal.view.execution;

import org.eclipse.osgi.util.NLS;

/**
 * Lists the i18n resource keys for the executions view messages.
 */
public final class ExecutionViewMessages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.buildship.ui.internal.view.execution.ExecutionViewMessages"; //$NON-NLS-1$

    public static String Label_No_Execution;

    public static String Tree_Column_Operation_Name_Text;
    public static String Tree_Column_Operation_Duration_Text;

    public static String Tree_Item_Operation_Running_For_0_Sec_Text;
    public static String Tree_Item_Operation_Finished_In_0_Sec_Text;

    public static String Action_RunTest_Text;
    public static String Action_ShowFailure_Text;
    public static String Action_OpenTestSourceFile_Text;

    public static String Action_SwitchExecutionPage_Tooltip;
    public static String Action_RemoveExecutionPage_Tooltip;
    public static String Action_RemoveAllExecutionPages_Tooltip;
    public static String Action_SwitchToConsole_Tooltip;

    public static String Dialog_Failure_Title;
    public static String Dialog_Failure_Back_Tooltip;
    public static String Dialog_Failure_Next_Tooltip;
    public static String Dialog_Failure_Copy_Details_Tooltip;
    public static String Dialog_Failure_Operation_Label;
    public static String Dialog_Failure_Message_Label;
    public static String Dialog_Failure_Details_Label;
    public static String Dialog_Failure_Link_Label;
    public static String Dialog_Failure_Root_Cause_Label;


    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, ExecutionViewMessages.class);
    }

    private ExecutionViewMessages() {
    }

}
