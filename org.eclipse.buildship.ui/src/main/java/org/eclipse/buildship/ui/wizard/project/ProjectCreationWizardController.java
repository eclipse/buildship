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

import com.google.common.base.Optional;
import com.gradleware.tooling.toolingutils.binding.Property;
import com.gradleware.tooling.toolingutils.binding.ValidationListener;
import com.gradleware.tooling.toolingutils.binding.Validator;
import org.eclipse.buildship.core.util.binding.Validators;
import org.eclipse.buildship.core.util.file.FileUtils;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.INewWizard;

import java.io.File;

/**
 * Controller class for the {@link org.eclipse.buildship.ui.wizard.project.ProjectImportWizard}. Contains all non-UI related calculations
 * the wizard has to perform.
 */
public final class ProjectCreationWizardController {

    // keys to load/store project properties in the dialog setting
    private static final String SETTINGS_KEY_LOCATION_USE_DEFAULT = "location_use_default"; //$NON-NLS-1$

    private final ProjectCreationConfiguration configuration;

    public ProjectCreationWizardController(INewWizard projectCreationWizard) {
        // assemble configuration object that serves as the extra data model of the creation wizard
        Validator<String> projectNameValidator = Validators.uniqueWorkspaceProjectNameValidator(ProjectWizardMessages.Label_ProjectName);
        Validator<Boolean> useDefaultLocationValidator = Validators.nullValidator();
        Validator<File> targetProjectDirValidator = Validators.onlyParentDirectoryExistsValidator(ProjectWizardMessages.Label_CustomLocation, ProjectWizardMessages.Message_TargetProjectDirectory);

        this.configuration = new ProjectCreationConfiguration(projectNameValidator, useDefaultLocationValidator, targetProjectDirValidator);

        // initialize values from the persisted dialog settings
        IDialogSettings dialogSettings = projectCreationWizard.getDialogSettings();
        Optional<Boolean> useDefaultLocation = Optional.fromNullable(dialogSettings.getBoolean(SETTINGS_KEY_LOCATION_USE_DEFAULT));

        this.configuration.setUseDefaultLocation(useDefaultLocation.or(Boolean.TRUE));

        // store the values every time they change
        saveBooleanPropertyWhenChanged(dialogSettings, SETTINGS_KEY_LOCATION_USE_DEFAULT, this.configuration.getUseDefaultLocation());
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
