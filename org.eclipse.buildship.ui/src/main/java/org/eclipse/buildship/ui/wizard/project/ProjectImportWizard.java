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

import java.util.List;

import com.google.common.util.concurrent.FutureCallback;
import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure;
import com.gradleware.tooling.toolingmodel.util.Pair;
import com.gradleware.tooling.toolingutils.distribution.PublishedGradleVersions;
import org.eclipse.core.runtime.jobs.Job;
import org.gradle.tooling.ProgressListener;
import org.osgi.service.prefs.BackingStoreException;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration;
import org.eclipse.buildship.ui.HelpContext;
import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.buildship.ui.util.workbench.WorkingSetUtils;

/**
 * Eclipse wizard for importing Gradle projects into the workspace.
 */
public final class ProjectImportWizard extends Wizard implements IImportWizard, HelpContextIdProvider {

    /**
     * The section name declaration for {@link org.eclipse.jface.dialogs.DialogSettings} where the import wizard stores its
     * preferences.
     *
     * @see org.eclipse.jface.dialogs.DialogSettings#getOrCreateSection(IDialogSettings, String)
     */
    private static final String PROJECT_IMPORT_DIALOG_SETTINGS = "org.eclipse.buildship.ui.wizard.project.import"; //$NON-NLS-1$

    /**
     * Preference key that flags whether the welcome page should be shown as part of the import wizard.
     */
    private static final String PREF_SHOW_WELCOME_PAGE = "org.eclipse.buildship.ui.wizard.project.import.showWelcomePage"; //$NON-NLS-1$

    // the pages to display in the wizard
    private final GradleWelcomeWizardPage welcomeWizardPage;
    private final GradleProjectWizardPage gradleProjectPage;
    private final GradleOptionsWizardPage gradleOptionsPage;
    private final ProjectPreviewWizardPage projectPreviewPage;

    // the controller that contains the wizard logic
    private final ProjectImportWizardController controller;

    // state bit storing that the wizard is blocked to finish globally
    private boolean finishGloballyEnabled;

    /**
     * Creates a new instance and uses the {@link org.eclipse.jface.dialogs.DialogSettings} from {@link org.eclipse.buildship.ui.UiPlugin} and the
     * {@link PublishedGradleVersions} from the {@link CorePlugin}.
     */
    @SuppressWarnings("UnusedDeclaration")
    public ProjectImportWizard() {
        this(getOrCreateDialogSection(UiPlugin.getInstance().getDialogSettings()), CorePlugin.publishedGradleVersions());
    }

    /**
     * Creates a new instance and uses the given {@link org.eclipse.jface.dialogs.DialogSettings} and
     * {@link PublishedGradleVersions}.
     *
     * @param dialogSettings the dialog settings to store/retrieve dialog preferences
     * @param publishedGradleVersions the published Gradle versions
     */
    public ProjectImportWizard(IDialogSettings dialogSettings, PublishedGradleVersions publishedGradleVersions) {
        // store the dialog settings on the wizard and use them to retrieve / persist the most
        // recent values entered by the user
        setDialogSettings(dialogSettings);

        // instantiate the controller for this wizard
        this.controller = new ProjectImportWizardController(this);

        // instantiate the pages and pass the configuration object that serves as the data model of the wizard
        ProjectImportConfiguration configuration = this.controller.getConfiguration();
        this.welcomeWizardPage = new GradleWelcomeWizardPage(configuration);
        this.gradleProjectPage = new GradleProjectWizardPage(configuration);
        this.gradleOptionsPage = new GradleOptionsWizardPage(configuration, publishedGradleVersions);
        this.projectPreviewPage = new ProjectPreviewWizardPage(this.controller.getConfiguration(), new ProjectPreviewWizardPage.ProjectPreviewLoader() {
            @Override
            public Job loadPreview(FutureCallback<Pair<OmniBuildEnvironment, OmniGradleBuildStructure>> resultHandler, List<ProgressListener> listeners) {
                return ProjectImportWizard.this.controller.performPreviewProject(resultHandler, listeners);
            }
        });

        // the wizard must not be finishable unless this global flag is enabled
        this.finishGloballyEnabled = true;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        List<String> workingSetNames = WorkingSetUtils.getSelectedWorkingSetNames(selection);
        if (!workingSetNames.isEmpty()) {
            this.controller.getConfiguration().setApplyWorkingSets(true);
            this.controller.getConfiguration().setWorkingSets(workingSetNames);
        }
    }

    @Override
    public String getWindowTitle() {
        return ProjectWizardMessages.Title_GradleProjectWizardPage;
    }

    @Override
    public void addPages() {
        // assign wizard pages to this wizard
        if (isShowWelcomePage()) {
            addPage(this.welcomeWizardPage);
        }
        addPage(this.gradleProjectPage);
        addPage(this.gradleOptionsPage);
        addPage(this.projectPreviewPage);

        // show progress bar when getContainer().run() is called
        setNeedsProgressMonitor(true);

        // enable help on all wizard pages
        setHelpAvailable(true);
    }

    public boolean isShowWelcomePage() {
        // store the in the configuration scope to have the same settings for all workspaces
        @SuppressWarnings("deprecation")
        ConfigurationScope configurationScope = new ConfigurationScope();
        IEclipsePreferences node = configurationScope.getNode(UiPlugin.PLUGIN_ID);
        return node.getBoolean(PREF_SHOW_WELCOME_PAGE, true);
    }

    public void setWelcomePageEnabled(boolean value) {
        @SuppressWarnings("deprecation")
        ConfigurationScope configurationScope = new ConfigurationScope();
        IEclipsePreferences node = configurationScope.getNode(UiPlugin.PLUGIN_ID);
        node.putBoolean(PREF_SHOW_WELCOME_PAGE, value);
        try {
            node.flush();
        } catch (BackingStoreException e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }

    @Override
    public boolean performFinish() {
        return this.controller.performImportProject();
    }

    @Override
    public boolean canFinish() {
        // the wizard can finish if all pages are complete and the finish is globally enabled
        return super.canFinish() && this.finishGloballyEnabled;
    }

    public void setFinishGloballyEnabled(boolean finishGloballyEnabled) {
        this.finishGloballyEnabled = finishGloballyEnabled;
    }

    @Override
    public String getHelpContextId() {
        return HelpContext.PROJECT_IMPORT;
    }

    private static IDialogSettings getOrCreateDialogSection(IDialogSettings dialogSettings) {
        // in Eclipse 3.6 the method DialogSettings#getOrCreateSection does not exist
        IDialogSettings section = dialogSettings.getSection(PROJECT_IMPORT_DIALOG_SETTINGS);
        if (section == null) {
            section = dialogSettings.addNewSection(PROJECT_IMPORT_DIALOG_SETTINGS);
        }
        return section;
    }

}
