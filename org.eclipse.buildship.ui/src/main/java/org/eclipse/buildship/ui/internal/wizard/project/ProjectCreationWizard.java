/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.wizard.project;

import java.io.File;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import org.eclipse.buildship.core.internal.util.file.FileUtils;
import org.eclipse.buildship.core.internal.workspace.NewProjectHandler;
import org.eclipse.buildship.ui.internal.HelpContext;
import org.eclipse.buildship.ui.internal.UiPlugin;
import org.eclipse.buildship.ui.internal.util.workbench.WorkingSetUtils;

/**
 * Eclipse wizard for creating Gradle projects in the workspace.
 */
public final class ProjectCreationWizard extends AbstractProjectWizard implements INewWizard {

    /**
     * The section name declaration for {@link org.eclipse.jface.dialogs.DialogSettings} where the import wizard stores its
     * preferences.
     *
     * @see org.eclipse.jface.dialogs.DialogSettings#getOrCreateSection(IDialogSettings, String)
     */
    private static final String PROJECT_CREATION_DIALOG_SETTINGS = "org.eclipse.buildship.ui.wizard.project.creation"; //$NON-NLS-1$

    // the pages to display in the wizard
    private final NewGradleProjectWizardPage newGradleProjectPage;
    private final GradleOptionsWizardPage gradleOptionsPage;
    private final ProjectPreviewWizardPage projectPreviewPage;

    // the controllers that contain the wizard logic
    private final ProjectImportWizardController importController;
    private final ProjectCreationWizardController creationController;
    private final IPageChangedListener pageChangeListener;

    /**
     * Creates a new instance and uses the {@link org.eclipse.jface.dialogs.DialogSettings} from {@link org.eclipse.buildship.ui.internal.UiPlugin}..
     */
    @SuppressWarnings("UnusedDeclaration")
    public ProjectCreationWizard() {
        this(getOrCreateDialogSection(UiPlugin.getInstance().getDialogSettings()));
    }

    /**
     * Creates a new instance and uses the given {@link org.eclipse.jface.dialogs.DialogSettings}.
     *
     * @param dialogSettings          the dialog settings to store/retrieve dialog preferences
     */
    public ProjectCreationWizard(IDialogSettings dialogSettings) {
        // store the dialog settings on the wizard and use them to retrieve / persist the most
        // recent values entered by the user
        setDialogSettings(dialogSettings);

        // instantiate the controllers for this wizard
        this.importController = new ProjectImportWizardController(this);
        this.creationController = new ProjectCreationWizardController(this);
        this.pageChangeListener = new ProjectCreatingPageChangedListener(this);

        // instantiate the pages and pass the configuration objects that serve as
        // the data models of the wizard
        final ProjectImportConfiguration importConfiguration = this.importController.getConfiguration();
        ProjectCreationConfiguration creationConfiguration = this.creationController.getConfiguration();
        this.newGradleProjectPage = new NewGradleProjectWizardPage(importConfiguration, creationConfiguration);
        this.gradleOptionsPage = new GradleOptionsWizardPage(importConfiguration,
                ProjectWizardMessages.Title_NewGradleProjectOptionsWizardPage,
                ProjectWizardMessages.InfoMessage_NewGradleProjectOptionsWizardPageDefault);
        this.projectPreviewPage = new ProjectPreviewWizardPage(importConfiguration,
                ProjectWizardMessages.Title_NewGradleProjectPreviewWizardPage,
                ProjectWizardMessages.InfoMessage_NewGradleProjectPreviewWizardPageDefault);
    }

    @Override
    public void setContainer(IWizardContainer wizardContainer) {
        if (wizardContainer instanceof IPageChangeProvider) {
            IPageChangeProvider pageChangeProvider = (IPageChangeProvider) wizardContainer;
            pageChangeProvider.addPageChangedListener(this.pageChangeListener);
        }
        super.setContainer(wizardContainer);
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        List<String> workingSetNames = WorkingSetUtils.getSelectedWorkingSetNames(selection);
        if (!workingSetNames.isEmpty()) {
            this.importController.getConfiguration().setApplyWorkingSets(true);
            this.importController.getConfiguration().setWorkingSets(workingSetNames);
        }
    }

    @Override
    public String getWindowTitle() {
        return ProjectWizardMessages.Title_NewGradleProjectWizardPage;
    }

    @Override
    public void addPages() {
        // assign wizard pages to this wizard
        addPage(this.newGradleProjectPage);
        addPage(this.gradleOptionsPage);
        addPage(this.projectPreviewPage);

        // show progress bar when getContainer().run() is called
        setNeedsProgressMonitor(true);

        // disable help on all wizard pages
        setHelpAvailable(false);
    }

    @Override
    public boolean performFinish() {
        return this.importController.performImportProject(getContainer(), NewProjectHandler.IMPORT_AND_MERGE);
    }

    @Override
    public boolean performCancel() {
        // if the projectPreviewPage is active the project has already been created and
        // needs to be removed
        IWizardPage currentPage = getContainer().getCurrentPage();
        if (this.projectPreviewPage.equals(currentPage)) {
            File projectDir = ProjectCreationWizard.this.importController.getConfiguration().getProjectDir().getValue();
            if (projectDir != null) {
                FileUtils.deleteRecursively(projectDir);
            }
        }
        return true;
    }

    @Override
    public String getHelpContextId() {
        return HelpContext.PROJECT_CREATION;
    }

    @Override
    public void dispose() {
        if (getContainer() instanceof IPageChangeProvider) {
            IPageChangeProvider pageChangeProvider = (IPageChangeProvider) getContainer();
            pageChangeProvider.removePageChangedListener(this.pageChangeListener);
        }
        super.dispose();
    }

    private static IDialogSettings getOrCreateDialogSection(IDialogSettings dialogSettings) {
        // in Eclipse 3.6 the method DialogSettings#getOrCreateSection does not exist
        IDialogSettings section = dialogSettings.getSection(PROJECT_CREATION_DIALOG_SETTINGS);
        if (section == null) {
            section = dialogSettings.addNewSection(PROJECT_CREATION_DIALOG_SETTINGS);
        }
        return section;
    }

    /**
     * Listens to page changes and either creates or deletes the new project depending from which
     * page to which page the user is switching.
     */
    private static final class ProjectCreatingPageChangedListener implements IPageChangedListener {

        private final ProjectCreationWizard projectCreationWizard;
        private IWizardPage previousPage;

        private ProjectCreatingPageChangedListener(ProjectCreationWizard projectCreationWizard) {
            this.projectCreationWizard = projectCreationWizard;
            this.previousPage = projectCreationWizard.newGradleProjectPage;
        }

        @Override
        public void pageChanged(PageChangedEvent event) {
            if (this.projectCreationWizard.projectPreviewPage.equals(this.previousPage) && this.projectCreationWizard.gradleOptionsPage.equals(event.getSelectedPage())) {
                // user moved back, so we need to delete the previously created Gradle project
                File projectDir = this.projectCreationWizard.importController.getConfiguration().getProjectDir().getValue();
                if (projectDir != null) {
                    FileUtils.deleteRecursively(projectDir);

                    // at this point the new project wizard page has a validation error because the
                    // project location validator is triggered before the location directory is
                    // deleted. to clear the error and re-enable the wizard finish button, the page
                    // page completion is re-calculated and set here
                    this.projectCreationWizard.newGradleProjectPage.setPageComplete(this.projectCreationWizard.newGradleProjectPage.isPageComplete());
                }
            }
            this.previousPage = (IWizardPage) event.getSelectedPage();
        }

    }
}
