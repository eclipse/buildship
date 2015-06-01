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

import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure;
import com.gradleware.tooling.toolingmodel.util.Pair;
import com.gradleware.tooling.toolingutils.binding.Property;
import com.gradleware.tooling.toolingutils.binding.ValidationListener;
import com.gradleware.tooling.toolingutils.binding.Validator;
import org.gradle.tooling.ProgressListener;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.FutureCallback;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.core.util.binding.GradleConnectionValidators;
import org.eclipse.buildship.core.util.gradle.GradleDistributionValidator;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper.DistributionType;
import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration;
import org.eclipse.buildship.core.projectimport.ProjectImportJob;
import org.eclipse.buildship.core.projectimport.ProjectPreviewJob;
import org.eclipse.buildship.core.util.collections.CollectionsUtils;
import org.eclipse.buildship.core.util.file.FileUtils;
import org.eclipse.buildship.ui.taskview.TaskView;
import org.eclipse.buildship.ui.util.workbench.WorkbenchUtils;
import org.eclipse.buildship.ui.view.execution.ExecutionsView;

/**
 * Controller class for the {@link ProjectImportWizard}. Contains all non-UI related calculations
 * the wizard has to perform.
 */
public final class ProjectImportWizardController {

    // keys to load/store project properties in the dialog setting
    private static final String SETTINGS_KEY_PROJECT_DIR = "project_location"; //$NON-NLS-1$
    private static final String SETTINGS_KEY_GRADLE_DISTRIBUTION_TYPE = "gradle_distribution_type"; //$NON-NLS-1$
    private static final String SETTINGS_KEY_GRADLE_DISTRIBUTION_CONFIGURATION = "gradle_distribution_configuration"; //$NON-NLS-1$
    private static final String SETTINGS_KEY_GRADLE_USER_HOME = "gradle_user_home"; //$NON-NLS-1$
    private static final String SETTINGS_KEY_JAVA_HOME = "java_home"; //$NON-NLS-1$
    private static final String SETTINGS_KEY_JVM_ARGUMENTS = "jvm_arguments"; //$NON-NLS-1$
    private static final String SETTINGS_KEY_ARGUMENTS = "arguments"; //$NON-NLS-1$
    private static final String SETTINGS_KEY_WORKING_SETS = "working_sets"; //$NON-NLS-1$

    private final ProjectImportConfiguration configuration;

    public ProjectImportWizardController(IWizard projectImportWizard) {
        // assemble configuration object that serves as the data model of the wizard
        Validator<File> projectDirValidator = GradleConnectionValidators.requiredDirectoryValidator(ProjectWizardMessages.Label_ProjectRootDirectory);
        Validator<GradleDistributionWrapper> gradleDistributionValidator = GradleDistributionValidator.gradleDistributionValidator();
        Validator<File> gradleUserHomeValidator = GradleConnectionValidators.optionalDirectoryValidator(ProjectWizardMessages.Label_GradleUserHome);
        Validator<File> javaHomeValidator = GradleConnectionValidators.optionalDirectoryValidator(ProjectWizardMessages.Label_JavaHome);
        Validator<String> jvmArgumentsValidator = GradleConnectionValidators.nullValidator();
        Validator<String> argumentsValidator = GradleConnectionValidators.nullValidator();
        Validator<List<String>> workingSetsValidator = GradleConnectionValidators.nullValidator();

        this.configuration = new ProjectImportConfiguration(projectDirValidator, gradleDistributionValidator, gradleUserHomeValidator, javaHomeValidator,
                jvmArgumentsValidator, argumentsValidator, workingSetsValidator);

        // initialize values from the persisted dialog settings
        IDialogSettings dialogSettings = projectImportWizard.getDialogSettings();
        Optional<File> projectDir = FileUtils.getAbsoluteFile(dialogSettings.get(SETTINGS_KEY_PROJECT_DIR));
        Optional<String> gradleDistributionType = Optional.fromNullable(Strings.emptyToNull(dialogSettings.get(SETTINGS_KEY_GRADLE_DISTRIBUTION_TYPE)));
        Optional<String> gradleDistributionConfiguration = Optional.fromNullable(Strings.emptyToNull(dialogSettings.get(SETTINGS_KEY_GRADLE_DISTRIBUTION_CONFIGURATION)));
        Optional<File> gradleUserHome = FileUtils.getAbsoluteFile(dialogSettings.get(SETTINGS_KEY_GRADLE_USER_HOME));
        Optional<File> javaHome = FileUtils.getAbsoluteFile(dialogSettings.get(SETTINGS_KEY_JAVA_HOME));
        Optional<String> jvmArguments = Optional.fromNullable(Strings.emptyToNull(dialogSettings.get(SETTINGS_KEY_JVM_ARGUMENTS)));
        Optional<String> arguments = Optional.fromNullable(Strings.emptyToNull(dialogSettings.get(SETTINGS_KEY_ARGUMENTS)));
        List<String> workingSets = ImmutableList.copyOf(CollectionsUtils.nullToEmpty(dialogSettings.getArray(SETTINGS_KEY_WORKING_SETS)));

        this.configuration.setProjectDir(projectDir.orNull());
        this.configuration.setGradleDistribution(createGradleDistribution(gradleDistributionType, gradleDistributionConfiguration));
        this.configuration.setGradleUserHome(gradleUserHome.orNull());
        this.configuration.setJavaHome(javaHome.orNull());
        this.configuration.setJvmArguments(jvmArguments.orNull());
        this.configuration.setArguments(arguments.orNull());
        this.configuration.setWorkingSets(workingSets);

        // store the values every time they change
        saveFilePropertyWhenChanged(dialogSettings, SETTINGS_KEY_PROJECT_DIR, this.configuration.getProjectDir());
        saveGradleWrapperPropertyWhenChanged(dialogSettings, this.configuration.getGradleDistribution());
        saveFilePropertyWhenChanged(dialogSettings, SETTINGS_KEY_GRADLE_USER_HOME, this.configuration.getGradleUserHome());
        saveFilePropertyWhenChanged(dialogSettings, SETTINGS_KEY_JAVA_HOME, this.configuration.getJavaHome());
        saveStringPropertyWhenChanged(dialogSettings, SETTINGS_KEY_JVM_ARGUMENTS, this.configuration.getJvmArguments());
        saveStringPropertyWhenChanged(dialogSettings, SETTINGS_KEY_ARGUMENTS, this.configuration.getArguments());
        saveStringArrayPropertyWhenChanged(dialogSettings, SETTINGS_KEY_WORKING_SETS, this.configuration.getWorkingSets());
    }

    private GradleDistributionWrapper createGradleDistribution(Optional<String> gradleDistributionType, Optional<String> gradleDistributionConfiguration) {
        DistributionType distributionType = DistributionType.valueOf(gradleDistributionType.or(DistributionType.WRAPPER.name()));
        String distributionConfiguration = gradleDistributionConfiguration.orNull();
        return GradleDistributionWrapper.from(distributionType, distributionConfiguration);
    }

    private void saveStringPropertyWhenChanged(final IDialogSettings settings, final String settingsKey, final Property<String> target) {
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

    public Job performPreviewProject(FutureCallback<Optional<Pair<OmniBuildEnvironment, OmniGradleBuildStructure>>> resultHandler, List<ProgressListener> listeners) {
        // the ProjectPreviewJob requires atypical constructor arguments since it is run as part of
        // the wizard framework
        ProjectPreviewJob projectPreviewJob = new ProjectPreviewJob(this.configuration, listeners, resultHandler);
        projectPreviewJob.schedule();
        return projectPreviewJob;
    }

    public boolean performImportProject() {
        ProjectImportJob importJob = new ProjectImportJob(this.configuration);
        importJob.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(IJobChangeEvent event) {
                if (event.getResult().isOK()) {
                    ensureGradleViewsAreVisible();
                }
            }
        });
        importJob.schedule();
        return true;
    }

    private void ensureGradleViewsAreVisible() {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                WorkbenchUtils.showView(TaskView.ID, null, IWorkbenchPage.VIEW_ACTIVATE);
                WorkbenchUtils.showView(ExecutionsView.ID, null, IWorkbenchPage.VIEW_VISIBLE);
            }
        });
    }

}
