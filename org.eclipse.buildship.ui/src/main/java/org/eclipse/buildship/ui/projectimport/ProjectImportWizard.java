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

package org.eclipse.buildship.ui.projectimport;

import com.gradleware.tooling.toolingutils.distribution.PublishedGradleVersions;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration;
import org.eclipse.buildship.ui.UiPlugin;

/**
 * Eclipse wizard for importing Gradle projects into the workspace.
 */
public final class ProjectImportWizard extends Wizard implements INewWizard {

    /**
     * The section name declaration for {@link org.eclipse.jface.dialogs.DialogSettings} where the import wizard stores its
     * preferences.
     *
     * @see org.eclipse.jface.dialogs.DialogSettings#getOrCreateSection(IDialogSettings, String)
     */
    private static final String IMPORT_DIALOG_SETTINGS = "org.eclipse.buildship.ui.projectimport"; //$NON-NLS-1$

    // the pages to display in the wizard
    private final GradleProjectWizardPage gradleProjectPage;
    private final GradleOptionsWizardPage gradleDistributionPage;
    private final ProjectPreviewWizardPage projectPreviewPage;

    // the controller that contains the wizard logic
    private final ProjectImportWizardController controller;

    /**
     * Creates a new instance and uses the {@link org.eclipse.jface.dialogs.DialogSettings} from {@link org.eclipse.buildship.ui.UiPlugin} and the
     * {@link PublishedGradleVersions} from the {@link CorePlugin}.
     */
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

        // instantiate the pages and pass the configuration object that serves as the data model of
        // the wizard
        ProjectImportConfiguration configuration = this.controller.getConfiguration();
        this.gradleProjectPage = new GradleProjectWizardPage(configuration);
        this.gradleDistributionPage = new GradleOptionsWizardPage(configuration, publishedGradleVersions);
        this.projectPreviewPage = new ProjectPreviewWizardPage(this.controller);
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        // do nothing
    }

    @Override
    public String getWindowTitle() {
        return ProjectImportMessages.Title_GradleProjectWizardPage;
    }

    @Override
    public void addPages() {
        // assign wizard pages to this wizard
        addPage(this.gradleProjectPage);
        addPage(this.gradleDistributionPage);
        addPage(this.projectPreviewPage);

        // show progress bar when getContainer().run() is called
        setNeedsProgressMonitor(true);

        // enable help on all wizard pages
        setHelpAvailable(true);
    }

    @Override
    public boolean performFinish() {
        return this.controller.performImportProject();
    }

    private static IDialogSettings getOrCreateDialogSection(IDialogSettings dialogSettings) {
        // in Eclipse 3.6 the method DialogSettings#getOrCreateSection does not exist
        IDialogSettings section = dialogSettings.getSection(IMPORT_DIALOG_SETTINGS);
        if (section == null) {
            section = dialogSettings.addNewSection(IMPORT_DIALOG_SETTINGS);
        }
        return section;
    }

}
