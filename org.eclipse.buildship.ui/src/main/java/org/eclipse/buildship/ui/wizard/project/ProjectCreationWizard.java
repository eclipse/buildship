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

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration;
import org.eclipse.buildship.core.projectimport.ProjectPreviewJob;
import org.eclipse.buildship.core.util.file.FileUtils;
import org.eclipse.buildship.core.util.gradle.PublishedGradleVersionsWrapper;
import org.eclipse.buildship.core.util.progress.AsyncHandler;
import org.eclipse.buildship.ui.HelpContext;
import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.buildship.ui.util.workbench.WorkingSetUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
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
import org.gradle.tooling.ProgressListener;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.gradleware.tooling.toolingclient.BuildLaunchRequest;
import com.gradleware.tooling.toolingclient.GradleDistribution;
import com.gradleware.tooling.toolingclient.LaunchableConfig;
import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.util.Pair;

/**
 * Page in the {@link ProjectCreationWizard} specifying the name of the Gradle project folder to
 * create.
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
     * The Gradle tasks to run to initialize a new Gradle Java project, i.e.
     * <code>gradle init --type java-library'</code>.
     */
    private static final ImmutableList<String> GRADLE_INIT_TASK_CMD_LINE = ImmutableList.of("init", "--type", "java-library");

    // the pages to display in the wizard
    private NewGradleProjectWizardPage newGradleProjectPage;
    private GradleOptionsWizardPage gradleOptionsPage;
    private ProjectPreviewWizardPage projectPreviewPage;

    // the controllers that contain the wizard logic
    private final ProjectImportWizardController importController;
    private final ProjectCreationWizardController creationController;
    private final IPageChangedListener pageChangeListener;

    private PublishedGradleVersionsWrapper publishedGradleVersions;

    /**
     * Creates a new instance and uses the {@link org.eclipse.jface.dialogs.DialogSettings} from
     * {@link org.eclipse.buildship.ui.UiPlugin}.
     */
    @SuppressWarnings("UnusedDeclaration")
    public ProjectCreationWizard() {
        this(getOrCreateDialogSection(UiPlugin.getInstance().getDialogSettings()),
                CorePlugin.publishedGradleVersions());
    }

    /**
     * Creates a new instance and uses the given {@link org.eclipse.jface.dialogs.DialogSettings}.
     *
     * @param dialogSettings the dialog settings to store/retrieve dialog preferences
     */
    public ProjectCreationWizard(IDialogSettings dialogSettings,
            PublishedGradleVersionsWrapper publishedGradleVersions) {
        this.publishedGradleVersions = publishedGradleVersions;
        // store the dialog settings on the wizard and use them to retrieve / persist the most
        // recent values entered by the user
        setDialogSettings(dialogSettings);

        // instantiate the controllers for this wizard
        this.importController = new ProjectImportWizardController(this);
        this.creationController = new ProjectCreationWizardController(this);
        this.pageChangeListener = new ProjectCreatingPageChangedListener(this);
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
        // instantiate the pages and pass the configuration objects that serve
        // as the data models of the wizard
        final ProjectImportConfiguration importConfiguration = this.importController.getConfiguration();
        ProjectCreationConfiguration creationConfiguration = this.creationController.getConfiguration();

        // assign wizard pages to this wizard
        this.newGradleProjectPage = new NewGradleProjectWizardPage(importConfiguration, creationConfiguration);
        addPage(this.newGradleProjectPage);
        this.gradleOptionsPage = new GradleOptionsWizardPage(importConfiguration, this.publishedGradleVersions);
        addPage(this.gradleOptionsPage);
        this.projectPreviewPage = new ProjectPreviewWizardPage(importConfiguration,
                new ProjectPreviewWizardPage.ProjectPreviewLoader() {
                    @Override
                    public Job loadPreview(
                            FutureCallback<Pair<OmniBuildEnvironment, OmniGradleBuildStructure>> resultHandler,
                            List<ProgressListener> listeners) {
                        ProjectPreviewJob projectPreviewJob = new ProjectPreviewJob(importConfiguration, listeners,
                                new NewGradleProjectInitializer(importConfiguration), resultHandler);
                        projectPreviewJob.schedule();
                        return projectPreviewJob;
                    }
                }, ProjectWizardMessages.Title_NewGradleProjectPreviewWizardPage,
                ProjectWizardMessages.InfoMessage_NewGradleProjectPreviewWizardPageDefault,
                ProjectWizardMessages.InfoMessage_NewGradleProjectPreviewWizardPageContext);
        addPage(this.projectPreviewPage);

        // show progress bar when getContainer().run() is called
        setNeedsProgressMonitor(true);

        // disable help on all wizard pages
        setHelpAvailable(false);
    }

    @Override
    public boolean performFinish() {
        return this.importController.performImportProject(new NewGradleProjectInitializer(this.importController.getConfiguration()));
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
            if (this.projectCreationWizard.projectPreviewPage.equals(this.previousPage) && this.projectCreationWizard.newGradleProjectPage.equals(event.getSelectedPage())) {
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

    /**
     * Initializes a new Gradle project from the given configuration.
     */
    private static final class NewGradleProjectInitializer implements AsyncHandler {

        private final FixedRequestAttributes fixedAttributes;

        private NewGradleProjectInitializer(ProjectImportConfiguration configuration) {
            this.fixedAttributes = configuration.toFixedAttributes();
        }

        @Override
        public void run(IProgressMonitor monitor) {
            monitor.beginTask("Init Gradle project", IProgressMonitor.UNKNOWN);
            try {
                File projectDir = this.fixedAttributes.getProjectDir().getAbsoluteFile();
                if (!projectDir.exists()) {
                    if (projectDir.mkdir()) {
                        // prepare the request
                        List<String> tasks = GRADLE_INIT_TASK_CMD_LINE;
                        GradleDistribution gradleDistribution = this.fixedAttributes.getGradleDistribution();
                        File gradleUserHome = FileUtils.getAbsoluteFile(this.fixedAttributes.getGradleUserHome()).orNull();
                        File javaHome = FileUtils.getAbsoluteFile(this.fixedAttributes.getJavaHome()).orNull();
                        List<String> jvmArguments = this.fixedAttributes.getJvmArguments();
                        List<String> arguments = this.fixedAttributes.getArguments();

                        // configure the request
                        BuildLaunchRequest request = CorePlugin.toolingClient().newBuildLaunchRequest(LaunchableConfig.forTasks(tasks));
                        request.projectDir(projectDir);
                        request.gradleDistribution(gradleDistribution);
                        request.gradleUserHomeDir(gradleUserHome);
                        request.javaHomeDir(javaHome);
                        request.jvmArguments(jvmArguments.toArray(new String[jvmArguments.size()]));
                        request.arguments(arguments.toArray(new String[arguments.size()]));

                        // launch the build
                        request.executeAndWait();
                    }
                }
            } finally {
                monitor.done();
            }
        }
    }

}
