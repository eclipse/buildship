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

import org.gradle.tooling.ProgressListener;

import com.google.common.util.concurrent.FutureCallback;

import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniGradleBuild;
import com.gradleware.tooling.toolingmodel.util.Pair;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration;
import org.eclipse.buildship.core.projectimport.ProjectPreviewJob;
import org.eclipse.buildship.core.util.gradle.PublishedGradleVersionsWrapper;
import org.eclipse.buildship.core.util.progress.AsyncHandler;
import org.eclipse.buildship.core.workspace.NewProjectHandler;
import org.eclipse.buildship.ui.HelpContext;
import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.buildship.ui.util.workbench.WorkingSetUtils;

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

    /**
     * Creates a new instance and uses the {@link org.eclipse.jface.dialogs.DialogSettings} from {@link org.eclipse.buildship.ui.UiPlugin} and the
     * {@link com.gradleware.tooling.toolingutils.distribution.PublishedGradleVersions} from the {@link CorePlugin}.
     */
    @SuppressWarnings("UnusedDeclaration")
    public ProjectImportWizard() {
        this(getOrCreateDialogSection(UiPlugin.getInstance().getDialogSettings()),
                CorePlugin.publishedGradleVersions());
    }

    /**
     * Creates a new instance and uses the given {@link org.eclipse.jface.dialogs.DialogSettings} and
     * {@link com.gradleware.tooling.toolingutils.distribution.PublishedGradleVersions}.
     *
     * @param dialogSettings the dialog settings to store/retrieve dialog preferences
     * @param publishedGradleVersions the published Gradle versions
     */
    public ProjectImportWizard(IDialogSettings dialogSettings, PublishedGradleVersionsWrapper publishedGradleVersions) {
        super(PREF_SHOW_WELCOME_PAGE);

        // store the dialog settings on the wizard and use them to retrieve / persist the most
        // recent values entered by the user
        setDialogSettings(dialogSettings);

        // instantiate the controller for this wizard
        this.controller = new ProjectImportWizardController(this);

        // instantiate the pages and pass the configuration object that serves as the data model of the wizard
        final ProjectImportConfiguration configuration = this.controller.getConfiguration();
        WelcomePageContent welcomePageContent = WelcomePageContentFactory.createImportWizardWelcomePageContent();
        this.welcomeWizardPage = new GradleWelcomeWizardPage(configuration, welcomePageContent);
        this.gradleProjectPage = new GradleProjectWizardPage(configuration);
        this.gradleOptionsPage = new GradleOptionsWizardPage(configuration, publishedGradleVersions);
        this.projectPreviewPage = new ProjectPreviewWizardPage(this.controller.getConfiguration(),
                new ProjectPreviewWizardPage.ProjectPreviewLoader() {
                    @Override
                    public Job loadPreview(FutureCallback<Pair<OmniBuildEnvironment, OmniGradleBuild>> resultHandler, List<ProgressListener> listeners) {
                        ProjectPreviewJob projectPreviewJob = new ProjectPreviewJob(configuration, listeners, AsyncHandler.NO_OP, resultHandler);
                        projectPreviewJob.schedule();
                        return projectPreviewJob;
                    }
                });
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

    @Override
    public boolean performFinish() {
        return this.controller.performImportProject(AsyncHandler.NO_OP, new UserDelegatedDescriptorHandler());
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

    /**
     * Asks the user whether he wants to keep .project files or overwrite them. Asks only once per multi-project build and remembers the decision.
     */
    private final class UserDelegatedDescriptorHandler implements NewProjectHandler {

        private Boolean overwriteDescriptors;

        @Override
        public boolean shouldImport(OmniEclipseProject projectModel) {
            return true;
        }

        @Override
        public boolean shouldOverwriteDescriptor(IProjectDescription descriptor, OmniEclipseProject projectModel) {
            if (this.overwriteDescriptors == null) {
                askUser();
            }
            return this.overwriteDescriptors;
        }

        @Override
        public void afterImport(IProject project, OmniEclipseProject projectModel) {
        }

        private void askUser() {
            PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
                @Override
                public void run() {
                    MessageDialog dialog = new MessageDialog(
                            getShell(),
                            ProjectWizardMessages.Existing_Descriptors_Overwrite_Dialog_Header,
                            null,
                            ProjectWizardMessages.Existing_Descriptors_Overwrite_Message,
                            MessageDialog.QUESTION,
                            new String[]{ProjectWizardMessages.Existing_Descriptors_Overwrite, ProjectWizardMessages.Existing_Descriptors_Keep},
                            0
                       );
                       int choice = dialog.open();
                       UserDelegatedDescriptorHandler.this.overwriteDescriptors = choice == 0;
                }
            });
        }

    }

}
