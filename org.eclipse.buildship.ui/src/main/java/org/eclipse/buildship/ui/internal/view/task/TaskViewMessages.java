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

package org.eclipse.buildship.ui.internal.view.task;

import org.eclipse.osgi.util.NLS;

/**
 * Lists the i18n resource keys for the task view messages.
 */
public final class TaskViewMessages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.buildship.ui.internal.view.task.TaskViewMessages"; //$NON-NLS-1$

    public static String Label_No_Gradle_Projects;
    public static String Label_Reload_Error;

    public static String Tree_Column_Name_Text;
    public static String Tree_Column_Description_Text;

    // context menu entries

    public static String Action_RunTasks_Text;
    public static String Action_RunTasks_Text_Disabled_Included;
    public static String Action_RunTasks_Text_Disabled_NonStandard_layout;
    public static String Action_RunTasks_Text_Disabled_Other;
    public static String Action_RunDefaultTasks_Text;
    public static String Action_CreateRunConfiguration_Text;
    public static String Action_OpenRunConfiguration_Text;
    public static String Action_OpenBuildScript_Text;
    public static String Action_Refresh_Text;

    public static String Action_RunTasks_Tooltip;
    public static String Action_RunDefaultTasks_Tooltip;
    public static String Action_CreateRunConfiguration_Tooltip;
    public static String Action_OpenRunConfiguration_Tooltip;
    public static String Action_OpenBuildScript_Tooltip;
    public static String Action_Refresh_Tooltip;
    public static String Action_LinkToSelection_Tooltip;

    // toolbar menu entries

    public static String Action_FilterProjectTasks_Text;
    public static String Action_FilterTaskSelectors_Text;
    public static String Action_FilterPrivateTasks_Text;
    public static String Action_SortByType_Text;
    public static String Action_SortByVisibility_Text;
    public static String Action_GroupTasks_Text;
    public static String Action_ShowFlattenProjectHiearchy;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, TaskViewMessages.class);
    }

    private TaskViewMessages() {
    }

}
