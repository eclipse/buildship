/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.wizard.project;

import java.io.File;

import com.google.common.collect.ImmutableList;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration;
import org.eclipse.buildship.core.util.file.FileUtils;
import org.eclipse.buildship.ui.UiPlugin;

/**
 * {@link INewWizard} for creating a new Gradle project.
 */
public class NewGradleProjectWizard extends Wizard implements INewWizard {

    /**
     * The section name declaration for {@link org.eclipse.jface.dialogs.DialogSettings} where the
     * import wizard stores its preferences.
     *
     * @see org.eclipse.jface.dialogs.DialogSettings#getOrCreateSection(IDialogSettings, String)
     */
    private static final String NEW_PROJECT_DIALOG_SETTINGS = "org.eclipse.buildship.ui.newproject"; //$NON-NLS-1$

    // the pages to display in the wizard
    private final GradleNewProjectWizardPage gradleProjectPage;

    // state bit storing that the wizard is blocked to finish globally
    private boolean finishGloballyEnabled;

    private ProjectImportWizardController controller;

    private ProjectPreviewWizardPage projectPreviewPage;

    /**
     * Creates a new instance and uses the {@link org.eclipse.jface.dialogs.DialogSettings} from
     * {@link org.eclipse.buildship.ui.UiPlugin}.
     */
    public NewGradleProjectWizard() {
        this(getOrCreateDialogSection(UiPlugin.getInstance().getDialogSettings()));
    }

    /**
     * Creates a new instance and uses the given {@link org.eclipse.jface.dialogs.DialogSettings}.
     *
     * @param dialogSettings the dialog settings to store/retrieve dialog preferences
     */
    public NewGradleProjectWizard(IDialogSettings dialogSettings) {
        // store the dialog settings on the wizard and use them to retrieve / persist the most
        // recent values entered by the user
        setDialogSettings(dialogSettings);

        // instantiate the controller for this wizard
        this.controller = new ProjectImportWizardController(this);

        // instantiate the pages and pass the configuration object that serves as the data model of
        // the wizard
        ProjectImportConfiguration configuration = this.controller.getConfiguration();
        // call "gradle init --type java-library" for a new Java project with Gradle
        configuration.setGradleTask(ImmutableList.of("init", "--type", "java-library"));

        this.gradleProjectPage = new GradleNewProjectWizardPage(configuration);
        this.projectPreviewPage = new ProjectPreviewWizardPage(this.controller);
        this.projectPreviewPage.setTitle("New Project Preview");
        this.projectPreviewPage.setMessage("Review the new project configuration before creating the Gradle project.");

        // the wizard must not be finishable unless this global flag is enabled
        this.finishGloballyEnabled = true;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection sselection) {
        // nothing to do
    }

    @Override
    public String getWindowTitle() {
        return "New Gradle Project";
    }

    @Override
    public void setContainer(IWizardContainer wizardContainer) {
        if (wizardContainer instanceof IPageChangeProvider) {
            IPageChangeProvider pageChangeProvider = (IPageChangeProvider) wizardContainer;
            pageChangeProvider.addPageChangedListener(this.runJavaInitOnPageChange);
        }
        super.setContainer(wizardContainer);
    }

    @Override
    public void dispose() {
        if (getContainer() instanceof IPageChangeProvider) {
            IPageChangeProvider pageChangeProvider = (IPageChangeProvider) getContainer();
            pageChangeProvider.removePageChangedListener(this.runJavaInitOnPageChange);
        }
        super.dispose();
    }

    @Override
    public void addPages() {
        // assign wizard pages to this wizard
        addPage(this.gradleProjectPage);
        addPage(this.projectPreviewPage);

        // show progress bar when getContainer().run() is called
        setNeedsProgressMonitor(true);
    }

    @Override
    public boolean performFinish() {
        this.controller.setWorkingSets(this.gradleProjectPage.getSelectedWorkingSets());
        this.controller.performInitNewProject(true);
        return true;
    }

    @Override
    public boolean performCancel() {
        IWizardPage currentPage = getContainer().getCurrentPage();
        // if the projectPreviewPage is active the project has already been created and needs to be
        // removed
        if (this.projectPreviewPage.equals(currentPage)) {
            File projectDir = NewGradleProjectWizard.this.controller.getConfiguration().getProjectDir().getValue();
            FileUtils.deleteDirectory(projectDir);
        }
        return true;
    }

    @Override
    public boolean canFinish() {
        // the wizard can finish if all pages are complete and the finish is globally enabled
        return super.canFinish() && this.finishGloballyEnabled;
    }

    public void setFinishGloballyEnabled(boolean finishGloballyEnabled) {
        this.finishGloballyEnabled = finishGloballyEnabled;
    }

    private static IDialogSettings getOrCreateDialogSection(IDialogSettings dialogSettings) {
        // in Eclipse 3.6 the method DialogSettings#getOrCreateSection does not exist
        IDialogSettings section = dialogSettings.getSection(NEW_PROJECT_DIALOG_SETTINGS);
        if (section == null) {
            section = dialogSettings.addNewSection(NEW_PROJECT_DIALOG_SETTINGS);
        }
        return section;
    }

    private IPageChangedListener runJavaInitOnPageChange = new IPageChangedListener() {

        private Object previousPage = NewGradleProjectWizard.this.gradleProjectPage;

        @Override
        public void pageChanged(PageChangedEvent event) {
            if (NewGradleProjectWizard.this.gradleProjectPage.equals(this.previousPage) && NewGradleProjectWizard.this.projectPreviewPage.equals(event.getSelectedPage())) {
                NewGradleProjectWizard.this.controller.performInitNewProject(false);
            } else if (NewGradleProjectWizard.this.projectPreviewPage.equals(this.previousPage) && NewGradleProjectWizard.this.gradleProjectPage.equals(event.getSelectedPage())) {
                // user moved back, so we need to delete the previously created Gradle project
                File projectDir = NewGradleProjectWizard.this.controller.getConfiguration().getProjectDir().getValue();
                FileUtils.deleteDirectory(projectDir);
            }
            this.previousPage = event.getSelectedPage();
        }
    };
}
