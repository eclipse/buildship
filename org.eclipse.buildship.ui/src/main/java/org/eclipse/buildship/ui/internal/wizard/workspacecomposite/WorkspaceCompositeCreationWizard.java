/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 *     Sebastian Kuzniarz (Diebold Nixdorf Inc.) - adaptation and customization for workspace composite wizard 
 */

package org.eclipse.buildship.ui.internal.wizard.workspacecomposite;

import org.eclipse.buildship.ui.internal.UiPlugin;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Eclipse wizard for creating Gradle composites in the workspace.
 */
public final class WorkspaceCompositeCreationWizard extends AbstractWorkspaceCompositeWizard implements INewWizard {

    /**
     * The section name declaration for {@link org.eclipse.jface.dialogs.DialogSettings} where the import wizard stores its
     * preferences.
     *
     * @see org.eclipse.jface.dialogs.DialogSettings#getOrCreateSection(IDialogSettings, String)
     */
    private static final String PROJECT_CREATION_DIALOG_SETTINGS = "org.eclipse.buildship.ui.wizard.project.creation"; //$NON-NLS-1$

    /**
     * Preference key that flags whether the welcome page should be shown as part of the creation wizard.
     */
    private static final String PREF_SHOW_WELCOME_PAGE = "org.eclipse.buildship.ui.wizard.project.creation.showWelcomePage"; //$NON-NLS-1$

    // the pages to display in the wizard
    private final GradleCreateWorkspaceCompositeWizardPage newGradleWorkspaceCompositePage;
    private final GradleImportOptionsWizardPage compositeImportOptionsPage;
    private final GradleRootProjectWizardPage compositeRootProjectPage;

    /**
     * Creates a new instance and uses the {@link org.eclipse.jface.dialogs.DialogSettings} from {@link org.eclipse.buildship.ui.internal.UiPlugin}..
     */
    public WorkspaceCompositeCreationWizard() {
        this(getOrCreateDialogSection(UiPlugin.getInstance().getDialogSettings()));
    }

    /**
     * Creates a new instance and uses the given {@link org.eclipse.jface.dialogs.DialogSettings}.
     *
     * @param dialogSettings          the dialog settings to store/retrieve dialog preferences
     */
    public WorkspaceCompositeCreationWizard(IDialogSettings dialogSettings) {
        super(PREF_SHOW_WELCOME_PAGE);

        // store the dialog settings on the wizard and use them to retrieve / persist the most
        // recent values entered by the user
        setDialogSettings(dialogSettings);

        // instantiate the pages and pass the configuration objects that serve as
        // the data models of the wizard
        this.newGradleWorkspaceCompositePage = new GradleCreateWorkspaceCompositeWizardPage();
        this.compositeImportOptionsPage = new GradleImportOptionsWizardPage(
                WorkspaceCompositeWizardMessages.Title_NewGradleImportOptionsWizardPage,
                WorkspaceCompositeWizardMessages.InfoMessage_NewGradleWorkspaceCompositeOptionsWizardPageDefault,
                WorkspaceCompositeWizardMessages.InfoMessage_NewGradleWorkspaceCompositeImportOptionsWizardPageContext);
        this.compositeRootProjectPage = new GradleRootProjectWizardPage(
                WorkspaceCompositeWizardMessages.Title_NewGradleCompositeRootWizardPage,
                WorkspaceCompositeWizardMessages.InfoMessage_NewGradleWorkspaceCompositePreviewWizardPageDefault,
                WorkspaceCompositeWizardMessages.InfoMessage_NewGradleWorkspaceCompositeCompositeRootWizardPageContext);
    }

    @Override
    public void setContainer(IWizardContainer wizardContainer) {
        super.setContainer(wizardContainer);
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
    }

    @Override
    public String getWindowTitle() {
        return WorkspaceCompositeWizardMessages.Title_NewGradleWorkspaceCompositeWizardPage;
    }

    @Override
    public void addPages() {
        // assign wizard pages to this wizard
        addPage(this.newGradleWorkspaceCompositePage);
        addPage(this.compositeImportOptionsPage);
        addPage(this.compositeRootProjectPage);

        // disable help on all wizard pages
        setHelpAvailable(false);
    }

    @Override
    public boolean performFinish() {
        return false;
    }

    @Override
    public boolean performCancel() {
        return true;
    }

    @Override
    public String getHelpContextId() {
        return "";
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    private static IDialogSettings getOrCreateDialogSection(IDialogSettings dialogSettings) {
        IDialogSettings section = dialogSettings.getSection(PROJECT_CREATION_DIALOG_SETTINGS);
        if (section == null) {
            section = dialogSettings.addNewSection(PROJECT_CREATION_DIALOG_SETTINGS);
        }
        return section;
    }
}
