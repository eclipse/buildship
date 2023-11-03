/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.wizard.workspacecomposite;

import org.eclipse.buildship.ui.internal.UiPlugin;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

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
    private static final String PROJECT_CREATION_DIALOG_SETTINGS = "org.eclipse.buildship.ui.wizard.composite.creation"; //$NON-NLS-1$

    // the pages to display in the wizard
    private final GradleCreateWorkspaceCompositeWizardPage newGradleWorkspaceCompositePage;
    private final GradleImportOptionsWizardPage compositeImportOptionsPage;
    private final GradleRootProjectWizardPage compositeRootProjectPage;
    
    // the controller that contain the wizard logic
    private final CompositeImportWizardController importController;
    private final CompositeCreationWizardController creationController;
    private final CompositeRootProjectWizardController rootProjectController;
    
    private IWorkingSet composite;
    
    // working set manager
    private IWorkingSetManager workingSetManager;

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

        // store the dialog settings on the wizard and use them to retrieve / persist the most
        // recent values entered by the user
        setDialogSettings(dialogSettings);
        
        this.workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();

        // instantiate the controllers for this wizard
        this.importController = new CompositeImportWizardController(this);
        this.creationController = new CompositeCreationWizardController(this);
        this.rootProjectController = new CompositeRootProjectWizardController(this);

        // instantiate the pages and pass the configuration objects that serve as
        // the data models of the wizard
        final CompositeConfiguration compositeConfiguration = this.importController.getConfiguration();
        final CompositeCreationConfiguration creationConfiguration = this.creationController.getConfiguration();
        final CompositeRootProjectConfiguration rootProjectConfiguration = this.rootProjectController.getConfiguration();
        this.newGradleWorkspaceCompositePage = new GradleCreateWorkspaceCompositeWizardPage(compositeConfiguration, creationConfiguration);
        this.compositeImportOptionsPage = new GradleImportOptionsWizardPage(compositeConfiguration);
        this.compositeRootProjectPage = new GradleRootProjectWizardPage(compositeConfiguration, rootProjectConfiguration);
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
    	boolean finished = this.importController.performCreateComposite(getContainer(), this.workingSetManager);
    	composite = this.importController.getWorkingSet();
        return finished;
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
  
    public IWorkingSet getComposite() {
    	return this.composite;
    }
}
