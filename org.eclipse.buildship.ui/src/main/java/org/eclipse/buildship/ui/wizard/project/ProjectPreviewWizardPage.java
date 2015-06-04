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

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.ui.PlatformUI;
import org.gradle.tooling.ProgressListener;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;

import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure;
import com.gradleware.tooling.toolingmodel.OmniGradleProjectStructure;
import com.gradleware.tooling.toolingmodel.util.Pair;
import com.gradleware.tooling.toolingutils.binding.Property;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.util.gradle.GradleDistributionFormatter;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper;
import org.eclipse.buildship.core.i18n.CoreMessages;
import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration;
import org.eclipse.buildship.core.util.progress.DelegatingProgressListener;
import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.buildship.ui.util.font.FontUtils;
import org.eclipse.buildship.ui.util.layout.LayoutUtils;
import org.eclipse.buildship.ui.util.widget.UiBuilder;

/**
 * Page in the {@link ProjectImportWizard} showing a preview about the project about to be
 * imported.
 */
public final class ProjectPreviewWizardPage extends AbstractWizardPage {

    private final ProjectImportWizardController controller;
    private final Font keyFont;
    private final Font valueFont;
    private final String pageContextInformation;

    private Label projectDirLabel;
    private Label gradleUserHomeLabel;
    private Label gradleDistributionLabel;
    private Label gradleVersionLabel;
    private Label javaHomeLabel;
    private Label jvmArgumentsLabel;
    private Label argumentsLabel;
    private Tree projectPreviewTree;

    public ProjectPreviewWizardPage(ProjectImportWizardController controller) {
        this(controller, ProjectWizardMessages.Title_PreviewImportWizardPage, ProjectWizardMessages.InfoMessage_PreviewImportWizardPageDefault, ProjectWizardMessages.InfoMessage_GradlePreviewWizardPageContext);
    }

    public ProjectPreviewWizardPage(ProjectImportWizardController controller, String title, String defaultMessage, String pageContextInformation) {
        super("ProjectPreview", title, defaultMessage, controller.getConfiguration(), ImmutableList.<Property<?>>of()); //$NON-NLS-1$
        this.controller = controller;
        this.keyFont = FontUtils.getCustomDialogFont(SWT.BOLD);
        this.valueFont = FontUtils.getCustomDialogFont(SWT.NONE);
        this.pageContextInformation = pageContextInformation;
    }

    @Override
    protected void createWidgets(Composite root) {
        root.setLayout(createLayout());
        createContent(root);
    }

    private Layout createLayout() {
        GridLayout layout = LayoutUtils.newGridLayout(2);
        layout.horizontalSpacing = 4;
        layout.verticalSpacing = 4;
        return layout;
    }

    private void createContent(Composite root) {
        createSummaryLabels(root);
        createPreviewGroup(root);
        updatePreviewLabels(getConfiguration());
    }

    private void createSummaryLabels(Composite container) {
        UiBuilder.UiBuilderFactory uiBuilderFactory = getUiBuilderFactory();

        uiBuilderFactory.newLabel(container).text(ProjectWizardMessages.Label_ProjectRootDirectory + ":").font(this.keyFont).alignLeft(); //$NON-NLS-1$
        this.projectDirLabel = uiBuilderFactory.newLabel(container).alignFillHorizontal().disabled().font(this.valueFont).control();

        createSpacingRow(container, 2);

        uiBuilderFactory.newLabel(container).text(ProjectWizardMessages.Label_GradleUserHome + ":").font(this.keyFont).alignLeft(); //$NON-NLS-1$
        this.gradleUserHomeLabel = uiBuilderFactory.newLabel(container).alignFillHorizontal().disabled().font(this.valueFont).control();

        uiBuilderFactory.newLabel(container).text(ProjectWizardMessages.Label_GradleDistribution + ":").font(this.keyFont).alignLeft(); //$NON-NLS-1$
        this.gradleDistributionLabel = uiBuilderFactory.newLabel(container).alignFillHorizontal().disabled().font(this.valueFont).control();

        uiBuilderFactory.newLabel(container).text(ProjectWizardMessages.Label_GradleVersion + ":").font(this.keyFont).alignLeft(); //$NON-NLS-1$
        this.gradleVersionLabel = uiBuilderFactory.newLabel(container).alignFillHorizontal().disabled().font(this.valueFont).control();

        createSpacingRow(container, 2);

        uiBuilderFactory.newLabel(container).text(ProjectWizardMessages.Label_JavaHome + ":").font(this.keyFont).alignLeft(); //$NON-NLS-1$
        this.javaHomeLabel = uiBuilderFactory.newLabel(container).alignFillHorizontal().disabled().font(this.valueFont).control();

        uiBuilderFactory.newLabel(container).text(ProjectWizardMessages.Label_JvmArguments + ":").font(this.keyFont).alignLeft(); //$NON-NLS-1$
        this.jvmArgumentsLabel = uiBuilderFactory.newLabel(container).alignFillHorizontal().disabled().font(this.valueFont).control();

        createSpacingRow(container, 2);

        uiBuilderFactory.newLabel(container).text(ProjectWizardMessages.Label_ProgramArguments + ":").font(this.keyFont).alignLeft(); //$NON-NLS-1$
        this.argumentsLabel = uiBuilderFactory.newLabel(container).alignFillHorizontal().disabled().font(this.valueFont).control();
    }

    private void createPreviewGroup(Composite container) {
        UiBuilder.UiBuilderFactory uiBuilderFactory = getUiBuilderFactory();

        // add spacing between the summary labels and the preview tree
        createSpacingRow(container, 2);

        // create an empty row and then a label
        uiBuilderFactory.newLabel(container).text(ProjectWizardMessages.Label_ProjectStructure + ":").font(this.keyFont).alignLeft(); //$NON-NLS-1$

        // add the preview tree
        this.projectPreviewTree = uiBuilderFactory.newTree(container).alignFillBoth(2).control();
    }

    private void createSpacingRow(Composite container, int horizontalSpan) {
        GridData data = new GridData(SWT.LEFT, SWT.CENTER, false, false, horizontalSpan, 1);
        data.heightHint = 8;
        new Label(container, SWT.NONE).setLayoutData(data);
    }

    private void updatePreviewLabels(ProjectImportConfiguration configuration) {
        updateFileLabel(this.projectDirLabel, configuration.getProjectDir(), CoreMessages.Value_UseGradleDefault);
        updateGradleDistributionLabel(this.gradleDistributionLabel, configuration.getGradleDistribution(), CoreMessages.Value_UseGradleDefault);
        updateGradleVersionLabel(this.gradleVersionLabel, configuration.getGradleDistribution(), CoreMessages.Value_Unknown);
        updateFileLabel(this.gradleUserHomeLabel, configuration.getGradleUserHome(), CoreMessages.Value_UseGradleDefault);
        updateFileLabel(this.javaHomeLabel, configuration.getJavaHome(), CoreMessages.Value_UseGradleDefault);
        updateStringLabel(this.jvmArgumentsLabel, configuration.getJvmArguments(), CoreMessages.Value_None);
        updateStringLabel(this.argumentsLabel, configuration.getArguments(), CoreMessages.Value_None);
    }

    private void updateStringLabel(Label target, Property<String> source, String defaultMessage) {
        String string = Strings.emptyToNull(source.getValue());
        target.setText(string != null ? string : defaultMessage);
    }

    private void updateFileLabel(Label target, Property<File> source, String defaultMessage) {
        File file = source.getValue();
        target.setText(file != null ? file.getAbsolutePath() : defaultMessage);
    }

    private void updateGradleDistributionLabel(Label target, Property<GradleDistributionWrapper> gradleDistribution, String defaultMessage) {
        GradleDistributionWrapper gradleDistributionWrapper = gradleDistribution.getValue();
        target.setText(gradleDistributionWrapper != null ? GradleDistributionFormatter.toString(gradleDistributionWrapper) : defaultMessage);
    }

    private void updateGradleVersionLabel(Label target, Property<GradleDistributionWrapper> gradleDistribution, String defaultMessage) {
        // set the version to 'unknown' until the effective version is
        // available from the BuildEnvironment model

        GradleDistributionWrapper gradleDistributionWrapper = gradleDistribution.getValue();
        if (gradleDistributionWrapper == null) {
            target.setText(defaultMessage);
            return;
        }

        switch (gradleDistributionWrapper.getType()) {
            case WRAPPER:
            case LOCAL_INSTALLATION:
            case REMOTE_DISTRIBUTION:
                target.setText(defaultMessage);
                break;
            case VERSION:
                target.setText(gradleDistributionWrapper.getConfiguration());
                break;
            default:
                throw new GradlePluginsRuntimeException("Unrecognized Gradle distribution type: " + gradleDistributionWrapper.getType()); //$NON-NLS-1$
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        if (visible) {
            // whenever the page becomes visible, set the initial preview labels to the values of
            // the wizard model
            updatePreviewLabels(getConfiguration());

            // schedule the loading of the project preview asynchronously, otherwise the UI will not
            // update until the job has finished
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

                @Override
                public void run() {
                    scheduleProjectPreviewJob();
                }
            });
        }
    }

    private void scheduleProjectPreviewJob() {
        final CountDownLatch latch = new CountDownLatch(1);
        final DelegatingProgressListener listener = new DelegatingProgressListener();
        final Job job = this.controller.performPreviewProject(new ProjectPreviewJobResultHandler(latch), ImmutableList.<ProgressListener> of(listener));

        try {
            // once cancellation has been requested by the user, do not block any longer
            // this way, the user can continue with the import wizard even if the preview is still
            // loading a no-longer-of-interest model in the background
            getContainer().run(true, true, new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) throws InterruptedException {
                    monitor.beginTask("Loading project preview", IProgressMonitor.UNKNOWN);
                    listener.setMonitor(monitor);
                    while (!latch.await(500, TimeUnit.MILLISECONDS)) {
                        // regularly check if the job was cancelled until
                        // the job has either finished successfully or failed
                        if (monitor.isCanceled()) {
                            job.cancel();
                            throw new InterruptedException();
                        }
                    }
                }
            });
        } catch (Exception e) {
            UiPlugin.logger().error("Failed to load preview.", e);
        }
    }

    /**
     * Updates the project preview once the necessary Gradle models have been loaded.
     */
    private final class ProjectPreviewJobResultHandler implements FutureCallback<Pair<OmniBuildEnvironment, OmniGradleBuildStructure>> {

        private final CountDownLatch latch;

        private ProjectPreviewJobResultHandler(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(Pair<OmniBuildEnvironment, OmniGradleBuildStructure> result) {
            // the job has already taken care of logging the success
            this.latch.countDown();

            updateSummary(result.getFirst());
            populateTree(result.getSecond());
        }

        @Override
        public void onFailure(Throwable t) {
            // the job has already taken care of logging and displaying the error
            this.latch.countDown();

            clearTree();
        }

        private void updateSummary(final OmniBuildEnvironment buildEnvironment) {
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

                @Override
                public void run() {
                    if (buildEnvironment.getGradle().getGradleUserHome().isPresent()) {
                        ProjectPreviewWizardPage.this.gradleUserHomeLabel.setText(buildEnvironment.getGradle().getGradleUserHome().get().getAbsolutePath());
                    }
                    ProjectPreviewWizardPage.this.gradleVersionLabel.setText(buildEnvironment.getGradle().getGradleVersion());
                    ProjectPreviewWizardPage.this.javaHomeLabel.setText(buildEnvironment.getJava().getJavaHome().getAbsolutePath());
                }
            });
        }

        private void populateTree(final OmniGradleBuildStructure buildStructure) {
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

                @Override
                public void run() {
                    ProjectPreviewWizardPage.this.projectPreviewTree.removeAll();

                    // populate the tree from the build structure
                    OmniGradleProjectStructure rootProjectStructure = buildStructure.getRootProject();
                    TreeItem rootTreeItem = new TreeItem(ProjectPreviewWizardPage.this.projectPreviewTree, SWT.NONE);
                    rootTreeItem.setText(rootProjectStructure.getName());
                    rootTreeItem.setExpanded(true);
                    populateRecursively(rootProjectStructure, rootTreeItem);
                }
            });
        }

        private void clearTree() {
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

                @Override
                public void run() {
                    ProjectPreviewWizardPage.this.projectPreviewTree.removeAll();
                }
            });
        }

        private void populateRecursively(OmniGradleProjectStructure gradleProjectStructure, TreeItem parent) {
            for (OmniGradleProjectStructure child : gradleProjectStructure.getChildren()) {
                TreeItem treeItem = new TreeItem(parent, SWT.NONE);
                treeItem.setText(child.getName());
                populateRecursively(child, treeItem);
            }
        }
    }

    @Override
    protected String getPageContextInformation() {
        return this.pageContextInformation;
    }

    @Override
    public void dispose() {
        this.keyFont.dispose();
        this.valueFont.dispose();
        super.dispose();
    }

}
