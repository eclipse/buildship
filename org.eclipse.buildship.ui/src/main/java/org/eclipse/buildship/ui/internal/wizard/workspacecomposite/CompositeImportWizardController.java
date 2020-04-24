/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 *     Sebastian Kuzniarz (Diebold Nixdorf Inc.) - refactored WizardHelper
 */

package org.eclipse.buildship.ui.internal.wizard.workspacecomposite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.buildship.core.GradleDistribution;
import org.eclipse.buildship.core.internal.i18n.CoreMessages;
import org.eclipse.buildship.core.internal.util.binding.Property;
import org.eclipse.buildship.core.internal.util.binding.ValidationListener;
import org.eclipse.buildship.core.internal.util.binding.Validator;
import org.eclipse.buildship.core.internal.util.binding.Validators;
import org.eclipse.buildship.core.internal.util.collections.CollectionsUtils;
import org.eclipse.buildship.core.internal.util.file.FileUtils;
import org.eclipse.buildship.ui.internal.UiPlugin;
import org.eclipse.buildship.ui.internal.util.gradle.GradleDistributionViewModel;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 * Controller class for the {@link WorkspaceCompositeCreationWizard}. Contains all non-UI related calculations
 * the wizard has to perform.
 */
public class CompositeImportWizardController {
	
	private static String PROJECT_CREATION_DIALOG_SETTINGS = "org.eclipse.buildship.ui.wizard.composite.creation";

	private IWorkingSet workingSet;

    // keys to load/store project properties in the dialog setting
    private static final String SETTINGS_KEY_COMPOSITE_DIR = "composite_location"; //$NON-NLS-1$
    //private static final String SETTINGS_KEY_GRADLE_PROJECT_LIST = "gradle_project_list"; //$NON-NLS-1$
    private static final String SETTINGS_KEY_GRADLE_DISTRIBUTION = "gradle_distribution"; //$NON-NLS-1$
    private static final String SETTINGS_KEY_APPLY_WORKING_SETS = "apply_working_sets"; //$NON-NLS-1$
    private static final String SETTINGS_KEY_WORKING_SETS = "working_sets"; //$NON-NLS-1$
    private static final String SETTINGS_KEY_GRADLE_USER_HOME = "gradle_user_home"; //$NON-NLS-1$
    private static final String SETTINGS_KEY_JAVA_HOME = "java_home"; //$NON-NLS-1$
    private static final String SETTINGS_KEY_BUILD_SCANS = "build_scans"; //$NON-NLS-1$
    private static final String SETTINGS_KEY_OFFLINE_MODE = "offline_mode"; //$NON-NLS-1$
    private static final String SETTINGS_KEY_AUTO_SYNC = "auto_sync"; //$NON-NLS-1$
    private static final String SETTINGS_KEY_ARGUMENTS = "arguments"; //$NON-NLS-1$
    private static final String SETTINGS_KEY_JVM_ARGUMENTS = "jvm_arguments"; //$NON-NLS-1$
    private static final String SETTINGS_KEY_SHOW_CONSOLE_VIEW = "show_console_view"; //$NON-NLS-1$
    private static final String SETTINGS_KEY_SHOW_EXECUTIONS_VIEW = "show_executions_view"; //$NON-NLS-1$
    private static final String SETTINGS_KEY_PROJECT_AS_COMPOSITE_ROOT = "project_as_composite_root"; //$NON-NLS-1$
    private static final String SETTINGS_KEY_ROOT_PROJECT = "root_project"; //$NON-NLS-1$

    private final CompositeConfiguration configuration;

    public CompositeImportWizardController(IWizard compositeImportWizard) {
        // assemble configuration object that serves as the data model of the wizard
        Validator<File> compositePreferenceDirValidator = Validators.and(
                Validators.requiredDirectoryValidator(WorkspaceCompositeWizardMessages.Label_CompositeName),
                Validators.nonWorkspaceFolderValidator(WorkspaceCompositeWizardMessages.Label_CompositeName));
        Validator<GradleDistributionViewModel> gradleDistributionValidator = GradleDistributionViewModel.validator();
        Validator<Boolean> applyWorkingSetsValidator = Validators.nullValidator();
        Validator<List<String>> workingSetsValidator = Validators.nullValidator();
        Validator<File> gradleUserHomeValidator = Validators.optionalDirectoryValidator(CoreMessages.Preference_Label_Gradle_User_Home);
        Validator<File> javaHomeValidator = Validators.optionalDirectoryValidator(CoreMessages.Preference_Label_Java_Home);
        Validator<File> rootProjectValidator = Validators.optionalDirectoryValidator(CoreMessages.Preference_Label_Root_Project);

        this.configuration = new CompositeConfiguration(compositePreferenceDirValidator, gradleDistributionValidator, gradleUserHomeValidator, javaHomeValidator, applyWorkingSetsValidator, workingSetsValidator, rootProjectValidator);

        // initialize values from the persisted dialog settings
        IDialogSettings dialogSettings;
      	
        if (compositeImportWizard != null) {
        	 dialogSettings = compositeImportWizard.getDialogSettings();
        } else {

        	 dialogSettings = getOrCreateDialogSection(UiPlugin.getInstance().getDialogSettings());
        }
        
        Optional<File> projectDir = FileUtils.getAbsoluteFile(dialogSettings.get(SETTINGS_KEY_COMPOSITE_DIR));
        String gradleDistributionString = dialogSettings.get(SETTINGS_KEY_GRADLE_DISTRIBUTION);
        Optional<File> gradleUserHome = FileUtils.getAbsoluteFile(dialogSettings.get(SETTINGS_KEY_GRADLE_USER_HOME));
        Optional<File> javaHome = FileUtils.getAbsoluteFile(dialogSettings.get(SETTINGS_KEY_JAVA_HOME));
        boolean applyWorkingSets = dialogSettings.get(SETTINGS_KEY_APPLY_WORKING_SETS) != null && dialogSettings.getBoolean(SETTINGS_KEY_APPLY_WORKING_SETS);
        List<String> workingSets = ImmutableList.copyOf(CollectionsUtils.nullToEmpty(dialogSettings.getArray(SETTINGS_KEY_WORKING_SETS)));
        boolean buildScansEnabled = dialogSettings.getBoolean(SETTINGS_KEY_BUILD_SCANS);
        boolean offlineMode = dialogSettings.getBoolean(SETTINGS_KEY_OFFLINE_MODE);
        boolean autoSync = dialogSettings.getBoolean(SETTINGS_KEY_AUTO_SYNC);
        List<String> arguments = ImmutableList.copyOf(CollectionsUtils.nullToEmpty(dialogSettings.getArray(SETTINGS_KEY_ARGUMENTS)));
        List<String> jvmArguments = ImmutableList.copyOf(CollectionsUtils.nullToEmpty(dialogSettings.getArray(SETTINGS_KEY_JVM_ARGUMENTS)));
        boolean showConsoleView = dialogSettings.getBoolean(SETTINGS_KEY_SHOW_CONSOLE_VIEW);
        boolean showExecutionsView = dialogSettings.getBoolean(SETTINGS_KEY_SHOW_EXECUTIONS_VIEW);
        boolean projectAsCompositeRoot = dialogSettings.getBoolean(SETTINGS_KEY_PROJECT_AS_COMPOSITE_ROOT);
        Optional<File> rootProject = FileUtils.getAbsoluteFile(dialogSettings.get(SETTINGS_KEY_ROOT_PROJECT));

        this.configuration.setCompositePreferencesDir(projectDir.orNull());
        this.configuration.setOverwriteWorkspaceSettings(false);
        GradleDistribution distribution;
        try {
            distribution = GradleDistribution.fromString(gradleDistributionString);
        } catch (RuntimeException ignore) {
            distribution = GradleDistribution.fromBuild();
        }
        this.configuration.setDistribution(GradleDistributionViewModel.from(distribution));
        this.configuration.setGradleUserHome(gradleUserHome.orNull());
        this.configuration.setJavaHomeHome(javaHome.orNull());
        this.configuration.setApplyWorkingSets(applyWorkingSets);
        this.configuration.setWorkingSets(workingSets);
        this.configuration.setBuildScansEnabled(buildScansEnabled);
        this.configuration.setOfflineMode(offlineMode);
        this.configuration.setAutoSync(autoSync);
        this.configuration.setArguments(arguments);
        this.configuration.setJvmArguments(jvmArguments);
        this.configuration.setShowConsoleView(showConsoleView);
        this.configuration.setShowExecutionsView(showExecutionsView);
        this.configuration.setProjectAsCompositeRoot(projectAsCompositeRoot);
        this.configuration.setRootProject(rootProject.orNull());

        // store the values every time they change
        saveFilePropertyWhenChanged(dialogSettings, SETTINGS_KEY_COMPOSITE_DIR, this.configuration.getCompositePreferencesDir());
        saveDistributionPropertyWhenChanged(dialogSettings, this.configuration.getDistribution());
        saveFilePropertyWhenChanged(dialogSettings, SETTINGS_KEY_GRADLE_USER_HOME, this.configuration.getGradleUserHome());
        saveFilePropertyWhenChanged(dialogSettings, SETTINGS_KEY_JAVA_HOME, this.configuration.getJavaHome());
        saveBooleanPropertyWhenChanged(dialogSettings, SETTINGS_KEY_APPLY_WORKING_SETS, this.configuration.getApplyWorkingSets());
        saveStringArrayPropertyWhenChanged(dialogSettings, SETTINGS_KEY_WORKING_SETS, this.configuration.getWorkingSets());
        saveBooleanPropertyWhenChanged(dialogSettings, SETTINGS_KEY_BUILD_SCANS, this.configuration.getBuildScansEnabled());
        saveBooleanPropertyWhenChanged(dialogSettings, SETTINGS_KEY_OFFLINE_MODE, this.configuration.getOfflineMode());
        saveBooleanPropertyWhenChanged(dialogSettings, SETTINGS_KEY_AUTO_SYNC, this.configuration.getAutoSync());
        saveStringArrayPropertyWhenChanged(dialogSettings, SETTINGS_KEY_ARGUMENTS, this.configuration.getArguments());
        saveStringArrayPropertyWhenChanged(dialogSettings, SETTINGS_KEY_JVM_ARGUMENTS, this.configuration.getJvmArguments());
        saveBooleanPropertyWhenChanged(dialogSettings, SETTINGS_KEY_SHOW_CONSOLE_VIEW, this.configuration.getShowConsoleView());
        saveBooleanPropertyWhenChanged(dialogSettings, SETTINGS_KEY_SHOW_EXECUTIONS_VIEW, this.configuration.getShowExecutionsView());
        saveBooleanPropertyWhenChanged(dialogSettings, SETTINGS_KEY_PROJECT_AS_COMPOSITE_ROOT, this.configuration.getProjectAsCompositeRoot());
        saveFilePropertyWhenChanged(dialogSettings, SETTINGS_KEY_ROOT_PROJECT, this.configuration.getRootProject());
    }

    private void saveBooleanPropertyWhenChanged(final IDialogSettings settings, final String settingsKey, final Property<Boolean> target) {
        target.addValidationListener(new ValidationListener() {

            @Override
            public void validationTriggered(Property<?> source, Optional<String> validationErrorMessage) {
                settings.put(settingsKey, target.getValue());
            }
        });
    }

    private void saveStringArrayPropertyWhenChanged(final IDialogSettings settings, final String settingsKey, final Property<List<String>> target) {
        target.addValidationListener(new ValidationListener() {

            @Override
            public void validationTriggered(Property<?> source, Optional<String> validationErrorMessage) {
                List<String> value = target.getValue();
                settings.put(settingsKey, value.toArray(new String[value.size()]));
            }
        });
    }

    private void saveFilePropertyWhenChanged(final IDialogSettings settings, final String settingsKey, final Property<File> target) {
        target.addValidationListener(new ValidationListener() {

            @Override
            public void validationTriggered(Property<?> source, Optional<String> validationErrorMessage) {
                settings.put(settingsKey, FileUtils.getAbsolutePath(target.getValue()).orNull());
            }
        });
    }

    private void saveDistributionPropertyWhenChanged(final IDialogSettings settings, final Property<GradleDistributionViewModel> target) {
        target.addValidationListener(new ValidationListener() {

            @Override
            public void validationTriggered(Property<?> source, Optional<String> validationErrorMessage) {
                if (!validationErrorMessage.isPresent()) {
                    settings.put(SETTINGS_KEY_GRADLE_DISTRIBUTION, target.getValue().toGradleDistribution().toString());
                } else {
                    settings.put(SETTINGS_KEY_GRADLE_DISTRIBUTION, GradleDistribution.fromBuild().toString());
                }
            }
        });
    }

    public CompositeConfiguration getConfiguration() {
        return this.configuration;
    }
    
    public boolean performCreateComposite(IWizardContainer container, IWorkingSetManager workingSetManager) {
    	try {
    		File compositePreferenceFile = this.configuration.getCompositePreferencesDir().getValue();
    		List<IProject> projects = new ArrayList<IProject>();
            for (IAdaptable project : getConfiguration().getProjectList().getValue()) {
    			projects.add((IProject) project);
    		}
    		workingSet = workingSetManager.createWorkingSet(compositePreferenceFile.getName(), projects.toArray(new IProject[projects.size()]));
    		workingSet.setId(IGradleCompositeIDs.NATURE);
    		workingSetManager.addWorkingSet(workingSet);
    		
    		
			FileOutputStream out = new FileOutputStream(compositePreferenceFile.getAbsoluteFile());
			Properties prop = getConfiguration().toCompositeProperties().toProperties();
			prop.store(out, " ");
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
    }
    
    private static IDialogSettings getOrCreateDialogSection(IDialogSettings dialogSettings) {
        IDialogSettings section = dialogSettings.getSection(PROJECT_CREATION_DIALOG_SETTINGS);
        if (section == null) {
            section = dialogSettings.addNewSection(PROJECT_CREATION_DIALOG_SETTINGS);
        }
        return section;
    }
    
    public IWorkingSet getWorkingSet() {
    	return workingSet;
    }
}
