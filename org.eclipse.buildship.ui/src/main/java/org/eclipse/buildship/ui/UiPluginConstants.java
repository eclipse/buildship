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

package org.eclipse.buildship.ui;

/**
 * Enumerates some of the values defined in the <i>plugin.xml</i>.
 * <p>
 * Every time the content of the <i>plugin.xml</i> file changes, the values in this class must be
 * aligned accordingly.
 */
public final class UiPluginConstants {

    /**
     * The context ID associated with the task view. This context is activated when the task view is
     * in focus.
     */
    public static final String TASKVIEW_CONTEXT_ID = "org.eclipse.buildship.ui.contexts.taskview";

    /**
     * The id of the <i>Console View</i> provided by Eclipse core.
     */
    public static final String CONSOLE_VIEW_ID = "org.eclipse.ui.console.ConsoleView";

    /**
     * The id of the <i>Run</i> launch group provided by Eclipse core.
     */
    public static final String RUN_LAUNCH_GROUP_ID = "org.eclipse.debug.ui.launchGroup.run";

    /**
     * The id of the command to refresh the task view.
     */
    public static final String REFRESH_TASKVIEW_COMMAND_ID = "org.eclipse.buildship.ui.commands.refreshtaskview";

    /**
     * The id of the command to run the selected tasks.
     */
    public static final String RUN_TASKS_COMMAND_ID = "org.eclipse.buildship.ui.commands.runtasks";

    /**
     * The id of the command to run the default tasks of the selected project.
     */
    public static final String RUN_DEFAULT_TASKS_COMMAND_ID = "org.eclipse.buildship.ui.commands.rundefaulttasks";

    /**
     * The id of the command to open the run configuration for the selected tasks.
     */
    public static final String OPEN_RUN_CONFIGURATION_COMMAND_ID = "org.eclipse.buildship.ui.commands.openrunconfiguration";

    /**
     * The id of the command to open the build script for the selected project.
     */
    public static final String OPEN_BUILD_SCRIPT_COMMAND_ID = "org.eclipse.buildship.ui.commands.openbuildscript";

    /**
     * The id of the command to execute a project refresh for the selected project.
     */
    public static final String REFRESH_PROJECT_COMMAND_ID = "org.eclipse.buildship.ui.commands.refreshproject";

    /**
     * The id of the 'Resource' working set page.
     */
    public static final String RESOURCE = "org.eclipse.ui.resourceWorkingSetPage"; //$NON-NLS-1$

    /**
     * The id of the 'Java' working set page.
     */
    public static final String JAVA = "org.eclipse.jdt.ui.JavaWorkingSetPage"; //$NON-NLS-1$

    public static final String IMPORT_WIZARD_ID = "org.eclipse.buildship.ui.wizards.project.import";

    private UiPluginConstants() {
    }

}
