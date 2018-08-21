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

package org.eclipse.buildship.core.internal.i18n;

import org.eclipse.osgi.util.NLS;

/**
 * Lists the i18n resource keys for the core messages.
 */
public final class CoreMessages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.buildship.core.internal.i18n.CoreMessages"; //$NON-NLS-1$

    public static String ClasspathContainer_Label;

    public static String GradleDistribution_Label_Group;
    public static String GradleDistribution_Label_FromImportSettings;
    public static String GradleDistribution_Label_FromImportSettings_Tooltip;
    public static String GradleDistribution_Label_GradleWrapper;
    public static String GradleDistribution_Label_LocalInstallationDirectory;
    public static String GradleDistribution_Label_RemoteDistributionUri;
    public static String GradleDistribution_Label_SpecificGradleVersion;

    public static String GradleDistribution_Value_UseGradleWrapper;
    public static String GradleDistribution_Value_UseLocalInstallation_0;
    public static String GradleDistribution_Value_UseRemoteDistribution_0;
    public static String GradleDistribution_Value_UseGradleVersion_0;

    public static String BuildExecution_Label_ShowExecutionView;
    public static String BuildExecution_Label_ShowConsoleView;

    public static String RunConfiguration_Label_GradleTasks;
    public static String RunConfiguration_Label_WorkingDirectory;
    public static String RunConfiguration_Label_GradleDistribution;
    public static String RunConfiguration_Label_GradleVersion;
    public static String RunConfiguration_Label_JavaHome;
    public static String RunConfiguration_Label_JvmArguments;
    public static String RunConfiguration_Label_Arguments;
    public static String RunConfiguration_Label_BuildExecution;
    public static String RunConfiguration_Label_Tests;
    public static String RunConfiguration_Label_OverrideProjectSettings;
    public static String RunConfiguration_Label_BuildScansEnabled;
    public static String RunConfiguration_Label_OfflineModeEnabled;

    public static String RunConfiguration_Value_RunDefaultTasks;

    public static String Value_None;
    public static String Value_Unknown;
    public static String Value_UseGradleDefault;

    public static String WarningMessage_Using_0_NonPortable;

    public static String ErrorMessage_0_DoesNotExist;
    public static String ErrorMessage_0_IsNotValid;
    public static String ErrorMessage_0_MustBeSpecified;
    public static String ErrorMessage_0_MustBeDirectory;
    public static String ErrorMessage_0_AlreadyExists;
    public static String ErrorMessage_0_WorkspaceDirectory;

    public static String Preference_Label_GradleUserHome;
    public static String Preference_Label_OfflineMode;
    public static String Preference_Label_BuildScans;
    public static String Preference_Label_BuildScansHover;
    public static String Preference_Label_AutoSync;
    public static String Preference_Label_AutoSyncHover;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, CoreMessages.class);
    }

    private CoreMessages() {
    }

}
