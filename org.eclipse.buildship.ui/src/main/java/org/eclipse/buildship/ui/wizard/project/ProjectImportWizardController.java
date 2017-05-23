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
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingutils.binding.Property;
import com.gradleware.tooling.toolingutils.binding.ValidationListener;
import com.gradleware.tooling.toolingutils.binding.Validator;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.BuildConfiguration;
import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration;
import org.eclipse.buildship.core.util.binding.Validators;
import org.eclipse.buildship.core.util.collections.CollectionsUtils;
import org.eclipse.buildship.core.util.file.FileUtils;
import org.eclipse.buildship.core.util.gradle.GradleDistributionValidator;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper.DistributionType;
import org.eclipse.buildship.core.util.progress.AsyncHandler;
import org.eclipse.buildship.core.workspace.GradleBuild;
import org.eclipse.buildship.core.workspace.NewProjectHandler;
import org.eclipse.buildship.ui.util.workbench.WorkbenchUtils;
import org.eclipse.buildship.ui.util.workbench.WorkingSetUtils;
import org.eclipse.buildship.ui.view.execution.ExecutionsView;
import org.eclipse.buildship.ui.view.task.TaskView;

/**
 * Controller class for the {@link ProjectImportWizard}. Contains all non-UI related calculations
 * the wizard has to perform.
 */
public class ProjectImportWizardController {

    // keys to load/store project properties in the dialog setting
    private static final String SETTINGS_KEY_PROJECT_DIR = "project_location"; //$NON-NLS-1$
    private static final String SETTINGS_KEY_GRADLE_DISTRIBUTION_TYPE = "gradle_distribution_type"; //$NON-NLS-1$
    private static final String SETTINGS_KEY_GRADLE_DISTRIBUTION_CONFIGURATION = "gradle_distribution_configuration"; //$NON-NLS-1
    private static final String SETTINGS_KEY_APPLY_WORKING_SETS = "apply_working_sets"; //$NON-NLS-1$
    private static final String SETTINGS_KEY_WORKING_SETS = "working_sets"; //$NON-NLS-1$

    private final ProjectImportConfiguration configuration;

    public ProjectImportWizardController(IWizard projectImportWizard) {
        // assemble configuration object that serves as the data model of the wizard
        Validator<File> projectDirValidator = Validators.requiredDirectoryValidator(ProjectWizardMessages.Label_ProjectRootDirectory);
        Validator<GradleDistributionWrapper> gradleDistributionValidator = GradleDistributionValidator.gradleDistributionValidator();
        Validator<Boolean> applyWorkingSetsValidator = Validators.nullValidator();
        Validator<List<String>> workingSetsValidator = Validators.nullValidator();
        Validator<File> gradleUserHomeValidator = Validators.optionalDirectoryValidator("Gradle user home");

        this.configuration = new ProjectImportConfiguration(projectDirValidator, gradleDistributionValidator, gradleUserHomeValidator, applyWorkingSetsValidator, workingSetsValidator);

        // initialize values from the persisted dialog settings
        IDialogSettings dialogSettings = projectImportWizard.getDialogSettings();
        Optional<File> projectDir = FileUtils.getAbsoluteFile(dialogSettings.get(SETTINGS_KEY_PROJECT_DIR));
        Optional<String> gradleDistributionType = Optional.fromNullable(Strings.emptyToNull(dialogSettings.get(SETTINGS_KEY_GRADLE_DISTRIBUTION_TYPE)));
        Optional<String> gradleDistributionConfiguration = Optional.fromNullable(Strings.emptyToNull(dialogSettings.get(SETTINGS_KEY_GRADLE_DISTRIBUTION_CONFIGURATION)));
        boolean applyWorkingSets = dialogSettings.get(SETTINGS_KEY_APPLY_WORKING_SETS) != null && dialogSettings.getBoolean(SETTINGS_KEY_APPLY_WORKING_SETS);
        List<String> workingSets = ImmutableList.copyOf(CollectionsUtils.nullToEmpty(dialogSettings.getArray(SETTINGS_KEY_WORKING_SETS)));

        this.configuration.setProjectDir(projectDir.orNull());
        this.configuration.setGradleDistribution(createGradleDistribution(gradleDistributionType, gradleDistributionConfiguration));
        this.configuration.setApplyWorkingSets(applyWorkingSets);
        this.configuration.setWorkingSets(workingSets);

        // store the values every time they change
        saveFilePropertyWhenChanged(dialogSettings, SETTINGS_KEY_PROJECT_DIR, this.configuration.getProjectDir());
        saveGradleWrapperPropertyWhenChanged(dialogSettings, this.configuration.getGradleDistribution());
        saveBooleanPropertyWhenChanged(dialogSettings, SETTINGS_KEY_APPLY_WORKING_SETS, this.configuration.getApplyWorkingSets());
        saveStringArrayPropertyWhenChanged(dialogSettings, SETTINGS_KEY_WORKING_SETS, this.configuration.getWorkingSets());
    }

    private GradleDistributionWrapper createGradleDistribution(Optional<String> gradleDistributionType, Optional<String> gradleDistributionConfiguration) {
        DistributionType distributionType = DistributionType.valueOf(gradleDistributionType.or(DistributionType.WRAPPER.name()));
        String distributionConfiguration = gradleDistributionConfiguration.orNull();
        return GradleDistributionWrapper.from(distributionType, distributionConfiguration);
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

    private void saveGradleWrapperPropertyWhenChanged(final IDialogSettings settings, final Property<GradleDistributionWrapper> target) {
        target.addValidationListener(new ValidationListener() {

            @Override
            public void validationTriggered(Property<?> source, Optional<String> validationErrorMessage) {
                settings.put(SETTINGS_KEY_GRADLE_DISTRIBUTION_TYPE, target.getValue().getType().name());
                settings.put(SETTINGS_KEY_GRADLE_DISTRIBUTION_CONFIGURATION, target.getValue().getConfiguration());
            }
        });
    }

    public ProjectImportConfiguration getConfiguration() {
        return this.configuration;
    }

    public boolean performImportProject(AsyncHandler initializer, NewProjectHandler newProjectHandler) {
        BuildConfiguration buildConfig = this.configuration.toBuildConfig();
        ImportWizardNewProjectHandler workingSetsAddingNewProjectHandler = new ImportWizardNewProjectHandler(newProjectHandler, this.configuration);
        GradleBuild build = CorePlugin.gradleWorkspaceManager().getGradleBuild(buildConfig);
        build.synchronize(workingSetsAddingNewProjectHandler, initializer);
        return true;
    }

    /**
     * A delegating {@link NewProjectHandler} which adds workingsets to the imported projects and
     * ensures that the Gradle views are visible.
     *
     * @author Stefan Oehme
     */
    private static final class ImportWizardNewProjectHandler implements NewProjectHandler {

        private final ProjectImportConfiguration configuration;
        private final NewProjectHandler importedBuildDelegate;

        private volatile boolean gradleViewsVisible;

        private ImportWizardNewProjectHandler(NewProjectHandler delegate, ProjectImportConfiguration configuration) {
            this.importedBuildDelegate = delegate;
            this.configuration = configuration;
        }

        @Override
        public boolean shouldImport(OmniEclipseProject projectModel) {
            return this.importedBuildDelegate.shouldImport(projectModel);
        }

        @Override
        public void afterImport(IProject project, OmniEclipseProject projectModel) {
            this.importedBuildDelegate.afterImport(project, projectModel);
            addWorkingSets(project);
            ensureGradleViewsAreVisible();
        }

        private void addWorkingSets(IProject project) {
            List<String> workingSetNames = this.configuration.getApplyWorkingSets().getValue() ? ImmutableList.copyOf(this.configuration.getWorkingSets().getValue())
                    : ImmutableList.<String> of();
            IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
            IWorkingSet[] workingSets = WorkingSetUtils.toWorkingSets(workingSetNames);
            workingSetManager.addToWorkingSets(project, workingSets);
        }

        private void ensureGradleViewsAreVisible() {
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

                @Override
                public void run() {
                    if (!ImportWizardNewProjectHandler.this.gradleViewsVisible) {
                        ImportWizardNewProjectHandler.this.gradleViewsVisible = true;
                        WorkbenchUtils.showView(TaskView.ID, null, IWorkbenchPage.VIEW_ACTIVATE);
                        WorkbenchUtils.showView(ExecutionsView.ID, null, IWorkbenchPage.VIEW_VISIBLE);
                    }
                }
            });
        }
    }

}
