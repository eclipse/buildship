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

package org.eclipse.buildship.core.i18n;

import org.eclipse.osgi.util.NLS;

/**
 * Lists the i18n resource keys for the core messages.
 */
public final class CoreMessages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.buildship.core.i18n.CoreMessages"; //$NON-NLS-1$

    public static String DefaultGradleLaunchConfigurationManager_ErrorMessage_CanNotCreateLaunchConfig;

	public static String DefaultGradleLaunchConfigurationManager_ErrorMessage_CanNotGetLaunchConfig;

	public static String DefaultGradleTestRunSession_ErrorMessage_CanNotFindParent;

	public static String DefaultGradleTestRunSession_ErrorMessage_CanNotFindTest;

	public static String DefaultGradleTestRunSession_ErrorMessage_CanNotFindTestSuite;

	public static String DefaultGradleTestRunSession_WarningMessage_TestDescriptorNotRecognized;

	public static String DefaultGradleTestRunSession_WarningMessage_TestEventNotRecognized;

	public static String DefaultGradleTestRunSession_WarningMessage_TestKindNotRecognized;

	public static String DefaultProjectConfigurationManager_ErrorMessage_InconsitentConfiguration;

	public static String DefaultWorkspaceOperations_AddNatureToEclipseProject;

	public static String DefaultWorkspaceOperations_ConfigSourcesAndClasspathForEclipse;

	public static String DefaultWorkspaceOperations_CreateEclipseJavaProject;

	public static String DefaultWorkspaceOperations_CreateEclipseProject;

	public static String DefaultWorkspaceOperations_CreateOutputFolderForEclipseProject;

	public static String DefaultWorkspaceOperations_DeleteAllEclipseProjectsFromWorkspace;

	public static String DefaultWorkspaceOperations_ErrorMessage_CanNotAddNatureToEclipseProject;

	public static String DefaultWorkspaceOperations_ErrorMessage_CanNotConfigSourcesAndClassPathForEclispeProject;

	public static String DefaultWorkspaceOperations_ErrorMessage_CanNotCreateEclipseJavaProject;

	public static String DefaultWorkspaceOperations_ErrorMessage_CanNotCreateEclipseProject;

	public static String DefaultWorkspaceOperations_ErrorMessage_CanNotCreateFolder;

	public static String DefaultWorkspaceOperations_ErrorMessage_CanNotCreateOutputFolderForEclipse;

	public static String DefaultWorkspaceOperations_ErrorMessage_CanNotDeleteProject;

	public static String DefaultWorkspaceOperations_ErrorMessage_CanNotRefreshEclipseProject;

	public static String DefaultWorkspaceOperations_MarkFolderasDerivedForEclipseProject;

	public static String DefaultWorkspaceOperations_ProjectLocationMustBeADirectory;

	public static String DefaultWorkspaceOperations_ProjectLocationMustExist;

	public static String DefaultWorkspaceOperations_ProjectMustBeOpen;

	public static String DefaultWorkspaceOperations_ProjectNameMustNotBeEmpty;

	public static String DefaultWorkspaceOperations_RefreshEclipseProject;

	public static String DefaultWorkspaceOperations_WorkspaceAlreadyContainsProjectName;

	public static String GradleClasspathContainerInitializer_Description_ExternalDependencies;

	public static String GradleClasspathContainerInitializer_ErrorMessage_CanNotFindEclipseProjectModel;

	public static String GradleClasspathContainerInitializer_ErrorMessage_FailToInitializeClassPath;

	public static String GradleClasspathContainerInitializer_InitializeClassPath;

	public static String GradleDistribution_Label_GradleWrapper;
    public static String GradleDistribution_Label_LocalInstallationDirectory;
    public static String GradleDistribution_Label_RemoteDistributionUri;
    public static String GradleDistribution_Label_SpecificGradleVersion;

    public static String GradleDistribution_Value_UseGradleWrapper;
    public static String GradleDistribution_Value_UseLocalInstallation_0;
    public static String GradleDistribution_Value_UseRemoteDistribution_0;
    public static String GradleDistribution_Value_UseGradleVersion_0;

	public static String GradleDistributionSerializer_ErrorMessage_CanNotDeserialize;

	public static String GradleDistributionSerializer_ErrorMessage_CanNotSerialize;

	public static String GradleDistributionWrapper_ErrorMessage_CanNotSerialize;

	public static String GradleProjectBuilder_ErrorMessage_FailToAddProject;

	public static String GradleProjectBuilder_ErrorMessage_FailToRemoveProject;

    public static String GradleProjectNature_ErrorMessage_CanNotCheckOnProject;

	public static String GradleProjectNature_ErrorMessage_ProjectClosed;

	public static String GradleProjectValidationResourceDeltaVisitor_ErrorMessage_InvalidConfigurationFile;

	public static String GradleProjectValidationResourceDeltaVisitor_ErrorMessage_MissingConfigurationFile;

	public static String GradleProjectValidationResourceDeltaVisitor_ErrorMessage_MissingConfigurationFolder;

	public static String GradleRunConfigurationAttributes_ErrorMessage_CanNotReadConfigAttribute;

	public static String GradleRunConfigurationAttributes_ErrorMessage_CanNotReadLaunchConfig;

	public static String GradleRunConfigurationAttributes_ErrorMessage_CanNotResolveArgumentExpression;

	public static String GradleRunConfigurationAttributes_ErrorMessage_CanNotResolveHomeDirectoryExpression;

	public static String GradleRunConfigurationAttributes_ErrorMessage_CanNotResolveJavaHomeDirectoryExpression;

	public static String GradleRunConfigurationAttributes_ErrorMessage_CanNotResolveJVMArgumentExpression;

	public static String GradleRunConfigurationAttributes_ErrorMessage_CanNotResolveWorkingDirectoryExpression;

	public static String GradleRunConfigurationDelegate_ErrorMessage_FailToLaunchTasks;

	public static String GradleRunConfigurationDelegate_LaunchGradleTasks;

	public static String ProgressVisualization_Label_VisualizeTestProgress;

	public static String ProjectConfigurationPersistence_ErrorMessage_CanNotPersistConfiguration;

	public static String ProjectConfigurationPersistence_ErrorMessage_CanNotReadConfiguration;

    public static String ProjectImportJob_ErrorMessage_ImportingProjectFailed;

	public static String ProjectImportJob_ImportGradleProject;

	public static String ProjectImportJob_ImportingProject;

	public static String ProjectImportJob_ImportProject;

	public static String ProjectImportJob_LoadEclipseProject;

	public static String ProjectPreviewJob_ErrorMessage_LoadingPreviewFailed;

	public static String ProjectPreviewJob_LoadGradleBuildEnviroment;

	public static String ProjectPreviewJob_LoadGradleProjectStructure;

	public static String ProjectPreviewJob_LoadingProjectPreview;

	public static String ProjectPreviewJob_LoadProjectPreview;

	public static String ResourceFilter_ErrorMessage_CanNotCreateNewResourceFilters;

	public static String ResourceFilter_ErrorMessage_CanNotDeleteCurrentResource;

	public static String ResourceFilter_ErrorMessage_CanNotRetrieveCurrentFilters;

	public static String ResourceFilter_SetResourceFiltersForProject;

	public static String RunConfiguration_Label_GradleTasks;
    public static String RunConfiguration_Label_WorkingDirectory;
    public static String RunConfiguration_Label_GradleDistribution;
    public static String RunConfiguration_Label_GradleUserHome;
    public static String RunConfiguration_Label_JavaHome;
    public static String RunConfiguration_Label_JvmArguments;
    public static String RunConfiguration_Label_Arguments;
    public static String RunConfiguration_Label_ProgressVisualization;

    public static String RunConfiguration_Value_RunDefaultTasks;

    public static String RunGradleConfigurationDelegateJob_createProcessName;

	public static String RunGradleConfigurationDelegateJob_ErrorMessage_CanNotWriteConfigDescription;

	public static String RunGradleConfigurationDelegateJob_ErrorMessage_GradleBuildFailed;

	public static String RunGradleConfigurationDelegateJob_ErrorMessage_LaunchingGradleTaskFailed;

	public static String RunGradleConfigurationDelegateJob_LauchGradleTasks;

	public static String RunGradleConfigurationDelegateJob_LaunchGradleTask;

	public static String Value_None;
    public static String Value_Unknown;
    public static String Value_UseGradleDefault;

    public static String ErrorMessage_0_DoesNotExist;
    public static String ErrorMessage_0_IsNotValid;
    public static String ErrorMessage_0_MustBeSpecified;
    public static String ErrorMessage_0_MustBeDirectory;

    public static String LoadEclipseGradleBuildJob_ErrorMessage_FailToLoadTasks;

	public static String LoadEclipseGradleBuildJob_LoadEclipseProject;

	public static String LoadEclipseGradleBuildJob_LoadingTasks;

	public static String LoadEclipseGradleBuildJob_LoadTasks;

	public static String LoadEclipseGradleBuildsJob_ErrorMessage_LoadingTasksFailed;

	public static String LoadEclipseGradleBuildsJob_LoadingTasksOfAllProjects;

	public static String LoadEclipseGradleBuildsJob_LoadTasksOfAllProjects;

	static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, CoreMessages.class);
    }

    private CoreMessages() {
    }

}
