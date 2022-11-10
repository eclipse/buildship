/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.wizard.project;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import org.eclipse.buildship.core.internal.workspace.NewProjectHandler;
import org.eclipse.buildship.ui.internal.HelpContext;
import org.eclipse.buildship.ui.internal.UiPlugin;
import org.eclipse.buildship.ui.internal.util.workbench.WorkingSetUtils;

/**
 * Eclipse wizard for importing Gradle projects into the workspace.
 */
public final class ProjectImportWizard extends AbstractProjectWizard implements IImportWizard {

    /**
     * The section name declaration for {@link org.eclipse.jface.dialogs.DialogSettings} where the import wizard stores its
     * preferences.
     *
     * @see org.eclipse.jface.dialogs.DialogSettings#getOrCreateSection(IDialogSettings, String)
     */
    private static final String PROJECT_IMPORT_DIALOG_SETTINGS = "org.eclipse.buildship.ui.wizard.project.import"; //$NON-NLS-1$

    // the pages to display in the wizard
    private final GradleProjectWizardPage gradleProjectPage;
    private final GradleOptionsWizardPage gradleOptionsPage;
    private final ProjectPreviewWizardPage projectPreviewPage;

    // the controller that contains the wizard logic
    private final ProjectImportWizardController controller;

    /**
     * Creates a new instance and uses the {@link org.eclipse.jface.dialogs.DialogSettings}.
     */
    @SuppressWarnings("UnusedDeclaration")
    public ProjectImportWizard() {
        this(getOrCreateDialogSection(UiPlugin.getInstance().getDialogSettings()));
    }

    /**
     * Creates a new instance and uses the given {@link org.eclipse.jface.dialogs.DialogSettings}.
     *
     * @param dialogSettings the dialog settings to store/retrieve dialog preferences
     */
    public ProjectImportWizard(IDialogSettings dialogSettings) {
        // store the dialog settings on the wizard and use them to retrieve / persist the most
        // recent values entered by the user
        setDialogSettings(dialogSettings);

        // instantiate the controller for this wizard
        this.controller = new ProjectImportWizardController(this);

        // instantiate the pages and pass the configuration object that serves as the data model of the wizard
        final ProjectImportConfiguration configuration = this.controller.getConfiguration();
        this.gradleProjectPage = new GradleProjectWizardPage(configuration);
        this.gradleOptionsPage = new GradleOptionsWizardPage(configuration);
        this.projectPreviewPage = new ProjectPreviewWizardPage(this.controller.getConfiguration());
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
        addPage(this.gradleProjectPage);
        addPage(this.gradleOptionsPage);
        addPage(this.projectPreviewPage);

        // show progress bar when getContainer().run() is called
        setNeedsProgressMonitor(true);

        // enable help on all wizard pages
        setHelpAvailable(true);
    }

    @Override
    public boolean performFinish() {
        return this.controller.performImportProject(getContainer(), NewProjectHandler.IMPORT_AND_MERGE);
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
