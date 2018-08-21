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

package org.eclipse.buildship.ui.wizard.project;

import java.io.File;

import com.google.common.base.Optional;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.INewWizard;

import org.eclipse.buildship.core.util.binding.Property;
import org.eclipse.buildship.core.util.binding.ValidationListener;
import org.eclipse.buildship.core.util.binding.Validators;
import org.eclipse.buildship.core.util.file.FileUtils;

/**
 * Controller class for the {@link org.eclipse.buildship.ui.wizard.project.ProjectImportWizard}. Contains all non-UI related calculations
 * the wizard has to perform.
 */
public final class ProjectCreationWizardController {

    // keys to load/store project properties in the dialog setting
    private static final String SETTINGS_KEY_LOCATION_USE_DEFAULT = "use_default_location"; //$NON-NLS-1$
    private static final String SETTINGS_KEY_CUSTOM_LOCATION = "custom_location"; //$NON-NLS-1$

    private final ProjectCreationConfiguration configuration;

    public ProjectCreationWizardController(INewWizard projectCreationWizard) {
        // assemble configuration object that serves as the extra data model of the creation wizard
        Property<String> projectNameProperty = Property.create(Validators.uniqueWorkspaceProjectNameValidator(ProjectWizardMessages.Label_ProjectName));
        Property<Boolean> useDefaultLocationProperty = Property.create(Validators.<Boolean>nullValidator());
        Property<File> customLocationProperty = Property.create(Validators.validateIfConditionFalse(Validators.requiredDirectoryValidator(ProjectWizardMessages.Label_CustomLocation), useDefaultLocationProperty));
        Property<File> targetProjectDirProperty = Property.create(Validators.nonExistentDirectoryValidator(ProjectWizardMessages.Message_TargetProjectDirectory));

        this.configuration = new ProjectCreationConfiguration(projectNameProperty, useDefaultLocationProperty, customLocationProperty, targetProjectDirProperty);

        // initialize values from the persisted dialog settings
        IDialogSettings dialogSettings = projectCreationWizard.getDialogSettings();
        boolean useDefaultLocation = dialogSettings.get(SETTINGS_KEY_LOCATION_USE_DEFAULT) == null || dialogSettings.getBoolean(SETTINGS_KEY_LOCATION_USE_DEFAULT);
        Optional<File> customLocation = FileUtils.getAbsoluteFile(dialogSettings.get(SETTINGS_KEY_CUSTOM_LOCATION));

        this.configuration.setUseDefaultLocation(useDefaultLocation);
        this.configuration.setCustomLocation(customLocation.orNull());

        // store the values every time they change
        saveBooleanPropertyWhenChanged(dialogSettings, SETTINGS_KEY_LOCATION_USE_DEFAULT, this.configuration.getUseDefaultLocation());
        saveFilePropertyWhenChanged(dialogSettings, SETTINGS_KEY_CUSTOM_LOCATION, this.configuration.getCustomLocation());
    }

    private void saveBooleanPropertyWhenChanged(final IDialogSettings settings, final String settingsKey, final Property<Boolean> target) {
        target.addValidationListener(new ValidationListener() {

            @Override
            public void validationTriggered(Property<?> source, Optional<String> validationErrorMessage) {
                settings.put(settingsKey, target.getValue());
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

    public ProjectCreationConfiguration getConfiguration() {
        return this.configuration;
    }

}
