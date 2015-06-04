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
import java.util.List;

import com.google.common.collect.ImmutableList;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
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

import org.eclipse.buildship.core.launch.RunGradleTasksJob;
import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration;
import org.eclipse.buildship.core.util.file.FileUtils;
import org.eclipse.buildship.ui.HelpContext;
import org.eclipse.buildship.ui.UiPlugin;

/**
 * Page in the {@link ProjectCreationWizard} specifying the name of the Gradle project folder to create.
 */
public final class ProjectCreationWizard extends Wizard implements INewWizard, HelpContextIdProvider {

    /**
     * The section name declaration for {@link org.eclipse.jface.dialogs.DialogSettings} where the
     * creation wizard stores its preferences.
     *
     * @see org.eclipse.jface.dialogs.DialogSettings#getOrCreateSection(IDialogSettings, String)
     */
    private static final String PROJECT_CREATION_DIALOG_SETTINGS = "org.eclipse.buildship.ui.wizard.project.creation"; //$NON-NLS-1$

    /**
     * The Gradle tasks to run to initialize a new Gradle Java project, i.e. <code>gradle init --type java-library'</code>.
     */
    private static final ImmutableList<String> GRADLE_INIT_TASK_CMD_LINE = ImmutableList.of("init", "--type", "java-library");

    // the pages to display in the wizard
    private final NewGradleProjectWizardPage newGradleProjectPage;
    private final ProjectPreviewWizardPage projectPreviewPage;

    // the controller that contains the wizard logic
    private final ProjectImportWizardController controller;
    private final IPageChangedListener pageChangeListener;

    /**
     * Creates a new instance and uses the {@link org.eclipse.jface.dialogs.DialogSettings} from
     * {@link org.eclipse.buildship.ui.UiPlugin}.
     */
    @SuppressWarnings("UnusedDeclaration")
    public ProjectCreationWizard() {
        this(getOrCreateDialogSection(UiPlugin.getInstance().getDialogSettings()));
    }

    /**
     * Creates a new instance and uses the given {@link org.eclipse.jface.dialogs.DialogSettings}.
     *
     * @param dialogSettings the dialog settings to store/retrieve dialog preferences
     */
    public ProjectCreationWizard(IDialogSettings dialogSettings) {
        // store the dialog settings on the wizard and use them to retrieve / persist the most
        // recent values entered by the user
        setDialogSettings(dialogSettings);

        // instantiate the controller for this wizard
        this.controller = new ProjectImportWizardController(this);
        this.pageChangeListener = new ProjectCreatingPageChangedListener(this);

        // instantiate the pages and pass the configuration object that serves as the data model of the wizard
        ProjectImportConfiguration configuration = this.controller.getConfiguration();
        this.newGradleProjectPage = new NewGradleProjectWizardPage(configuration);
        this.projectPreviewPage = new ProjectPreviewWizardPage(this.controller,
                ProjectWizardMessages.Title_NewGradleProjectPreviewWizardPage,
                ProjectWizardMessages.InfoMessage_NewGradleProjectPreviewWizardPageDefault,
                ProjectWizardMessages.InfoMessage_NewGradleProjectPreviewWizardPageContext);
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
        // nothing to do
    }

    @Override
    public String getWindowTitle() {
        return ProjectWizardMessages.Title_NewGradleProjectWizardPage;
    }

    @Override
    public void addPages() {
        // assign wizard pages to this wizard
        addPage(this.newGradleProjectPage);
        addPage(this.projectPreviewPage);

        // show progress bar when getContainer().run() is called
        setNeedsProgressMonitor(true);

        // disable help on all wizard pages
        setHelpAvailable(false);
    }

    @Override
    public boolean performFinish() {
        performInitNewProject(true, GRADLE_INIT_TASK_CMD_LINE);
        return true;
    }

    @Override
    public boolean performCancel() {
        // if the projectPreviewPage is active the project has already been created and
        // needs to be removed
        IWizardPage currentPage = getContainer().getCurrentPage();
        if (this.projectPreviewPage.equals(currentPage)) {
            File projectDir = ProjectCreationWizard.this.controller.getConfiguration().getProjectDir().getValue();
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
     * Listens to page changes and either creates or deletes the new project depending from
     * which page to which page the user is switching.
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
            if (this.projectCreationWizard.newGradleProjectPage.equals(this.previousPage) && this.projectCreationWizard.projectPreviewPage.equals(event.getSelectedPage())) {
                this.projectCreationWizard.performInitNewProject(false, GRADLE_INIT_TASK_CMD_LINE);
            } else if (this.projectCreationWizard.projectPreviewPage.equals(this.previousPage) && this.projectCreationWizard.newGradleProjectPage.equals(event.getSelectedPage())) {
                // user moved back, so we need to delete the previously created Gradle project
                File projectDir = this.projectCreationWizard.controller.getConfiguration().getProjectDir().getValue();
                if (projectDir != null) {
                    FileUtils.deleteRecursively(projectDir);
                }
            }
            this.previousPage = (IWizardPage) event.getSelectedPage();
        }

    }

    private boolean performInitNewProject(final boolean doImport, List<String> gradleTaskToRun) {
        ProjectImportConfiguration configuration = this.controller.getConfiguration();
        File projectDir = configuration.getProjectDir().getValue();
        if (!projectDir.exists()) {
            if (projectDir.mkdir()) {
                RunGradleTasksJob runGradleTasksJob = new RunGradleTasksJob(gradleTaskToRun, configuration.getProjectDir().getValue(), configuration.getGradleDistribution().getValue().toGradleDistribution(), configuration.getGradleUserHome().getValue(), configuration.getJavaHome().getValue(), configuration.getJvmArguments().getValue(), configuration.getArguments().getValue());
                if (doImport) {
                    runGradleTasksJob.addJobChangeListener(new JobChangeAdapter() {

                        @Override
                        public void done(IJobChangeEvent event) {
                            if (event.getResult().isOK()) {
                                ProjectCreationWizard.this.controller.performImportProject();
                            }
                        }
                    });
                }
                runGradleTasksJob.schedule();
            } else {
                return false;
            }
        } else {
            if (doImport) {
                this.controller.performImportProject();
            }
        }
        return true;
    }

}
