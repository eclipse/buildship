/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 *     Sebastian Kuzniarz (Diebold Nixdorf Inc.) - refactored WizardHelper
 */

package org.eclipse.buildship.ui.internal.wizard.project;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.build.GradleEnvironment;
import org.gradle.tooling.model.gradle.BasicGradleProject;
import org.gradle.tooling.model.gradle.GradleBuild;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PageBook;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration;
import org.eclipse.buildship.core.internal.gradle.MissingFeatures;
import org.eclipse.buildship.core.internal.i18n.CoreMessages;
import org.eclipse.buildship.core.internal.operation.BaseToolingApiOperation;
import org.eclipse.buildship.core.internal.operation.ToolingApiOperations;
import org.eclipse.buildship.core.internal.operation.ToolingApiStatus;
import org.eclipse.buildship.core.internal.operation.ToolingApiStatus.ToolingApiStatusType;
import org.eclipse.buildship.core.internal.util.binding.Property;
import org.eclipse.buildship.core.internal.util.gradle.GradleVersion;
import org.eclipse.buildship.core.internal.util.gradle.Pair;
import org.eclipse.buildship.core.internal.workspace.FetchStrategy;
import org.eclipse.buildship.core.internal.workspace.InitializeNewProjectOperation;
import org.eclipse.buildship.core.internal.workspace.ModelProvider;
import org.eclipse.buildship.ui.internal.util.font.FontUtils;
import org.eclipse.buildship.ui.internal.util.gradle.GradleDistributionViewModel;
import org.eclipse.buildship.ui.internal.util.gradle.GradleDistributionViewModel.Type;
import org.eclipse.buildship.ui.internal.util.layout.LayoutUtils;
import org.eclipse.buildship.ui.internal.util.widget.UiBuilder;
import org.eclipse.buildship.ui.internal.wizard.WizardHelper;

/**
 * Page in the {@link ProjectImportWizard} showing a preview about the project about to be imported.
 */
public final class ProjectPreviewWizardPage extends AbstractWizardPage {

    private final Font keyFont;
    private final Font valueFont;
    private final String pageContextInformation;

    private Label projectDirLabel;
    private Label gradleUserHomeLabel;
    private Label gradleDistributionLabel;
    private Label gradleVersionLabel;
    private Label gradleVersionWarningLabel;
    private Label javaHomeLabel;
    private PageBook previewResultPages;
    private Tree previewResultSuccessTree;
    private Text previewResultErrorText;

    public ProjectPreviewWizardPage(ProjectImportConfiguration configuration) {
        this(configuration,
             ProjectWizardMessages.Title_PreviewImportWizardPage,
             ProjectWizardMessages.InfoMessage_GradlePreviewWizardPageDefault,
             ProjectWizardMessages.InfoMessage_GradlePreviewWizardPageContext);
    }

    public ProjectPreviewWizardPage(ProjectImportConfiguration configuration, String title, String defaultMessage,
            String pageContextInformation) {
        super("ProjectPreview", title, defaultMessage, configuration, ImmutableList.<Property<?>>of()); //$NON-NLS-1$
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
        Composite gradleVersionContainer = new Composite(container, SWT.NONE);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(gradleVersionContainer);
        GridLayoutFactory.swtDefaults().margins(0, 0).extendedMargins(0, 0, 0, 0).spacing(0, 0).numColumns(2).applyTo(gradleVersionContainer);

        this.gradleVersionLabel = uiBuilderFactory.newLabel(gradleVersionContainer).alignLeft().disabled().font(this.valueFont).control();
        this.gradleVersionWarningLabel = uiBuilderFactory.newLabel(gradleVersionContainer).alignLeft().control();
        this.gradleVersionWarningLabel.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK));
        this.gradleVersionWarningLabel.setCursor(gradleVersionContainer.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
        this.gradleVersionWarningLabel.setToolTipText(ProjectWizardMessages.Missing_Features_Tooltip);
        this.gradleVersionWarningLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseUp(MouseEvent e) {
                String version = ProjectPreviewWizardPage.this.gradleVersionLabel.getText();
                MissingFeatures limitations = new MissingFeatures(GradleVersion.version(version));
                FluentIterable<String> limitationMessages = FluentIterable.from(limitations.getMissingFeatures()).transform(new Function<Pair<GradleVersion, String>, String>() {

                    @Override
                    public String apply(Pair<GradleVersion, String> limitation) {
                        return limitation.getSecond();
                    }
                });
                String message = NLS.bind(ProjectWizardMessages.Missing_Features_Details_0_1, version, Joiner.on('\n').join(limitationMessages));
                MessageDialog.openInformation(getShell(), ProjectWizardMessages.Title_Dialog_Missing_Features, message);
            }
        });

        createSpacingRow(container, 2);

        uiBuilderFactory.newLabel(container).text(ProjectWizardMessages.Label_JavaHome + ":").font(this.keyFont).alignLeft(); //$NON-NLS-1$
        this.javaHomeLabel = uiBuilderFactory.newLabel(container).alignFillHorizontal().disabled().font(this.valueFont).control();
    }

    private void createPreviewGroup(Composite container) {
        UiBuilder.UiBuilderFactory uiBuilderFactory = getUiBuilderFactory();

        // add spacing between the summary labels and the preview tree
        createSpacingRow(container, 2);

        // create an empty row and then a label
        uiBuilderFactory.newLabel(container).text(ProjectWizardMessages.Label_ProjectStructure + ":").font(this.keyFont).alignLeft(); //$NON-NLS-1$

        // create an info icon explaining that the preview can deviate from actual values
        Label previewStructureInfoLabel = uiBuilderFactory.newLabel(container).alignLeft().control();
        previewStructureInfoLabel.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK));
        previewStructureInfoLabel.setCursor(container.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
        previewStructureInfoLabel.setToolTipText(ProjectWizardMessages.PreviewStructureInfo_Tooltip);
        previewStructureInfoLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseUp(MouseEvent e) {
                MessageDialog.openInformation(getShell(), ProjectWizardMessages.Title_Dialog_PreviewStructureInfo, ProjectWizardMessages.PreviewStructureInfo_Details);
            }
        });

        this.previewResultPages = new PageBook(container, SWT.NONE);
        GridDataFactory.fillDefaults().span(2, 1).grab(true, true).applyTo(this.previewResultPages);
        this.previewResultSuccessTree = new Tree(this.previewResultPages, SWT.NONE);
        this.previewResultErrorText = new Text(this.previewResultPages, SWT.MULTI | SWT.READ_ONLY | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        this.previewResultPages.showPage(this.previewResultSuccessTree);
    }

    private void createSpacingRow(Composite container, int horizontalSpan) {
        GridData data = new GridData(SWT.LEFT, SWT.CENTER, false, false, horizontalSpan, 1);
        data.heightHint = 8;
        new Label(container, SWT.NONE).setLayoutData(data);
    }

    private void updatePreviewLabels(ProjectImportConfiguration configuration) {
        updateFileLabel(this.projectDirLabel, configuration.getProjectDir(), CoreMessages.Value_UseGradleDefault);
        updateGradleDistributionLabel(this.gradleDistributionLabel, configuration.getDistribution(), CoreMessages.Value_UseGradleDefault);
        updateGradleVersionLabel(this.gradleVersionLabel, configuration.getDistribution(), CoreMessages.Value_Unknown);
        this.gradleUserHomeLabel.setText(CoreMessages.Value_Unknown);
        this.javaHomeLabel.setText(CoreMessages.Value_Unknown);
        updateGradleVersionWarningLabel();
    }

    private void updateFileLabel(Label target, Property<File> source, String defaultMessage) {
        File file = source.getValue();
        target.setText(file != null ? file.getAbsolutePath() : defaultMessage);
    }

    private void updateGradleDistributionLabel(Label target, Property<GradleDistributionViewModel> distributionProperty, String defaultMessage) {
        GradleDistributionViewModel distribution = distributionProperty.getValue();
        target.setText(distribution != null ? distribution.toString() : defaultMessage);
    }

    private void updateGradleVersionLabel(Label target, Property<GradleDistributionViewModel> gradleDistribution, String defaultMessage) {
        // set the version to 'unknown' until the effective version is
        // available from the BuildEnvironment model

        GradleDistributionViewModel distribution = gradleDistribution.getValue();
        if (distribution == null) {
            target.setText(defaultMessage);
            return;
        }

        Optional<Type> typeOrNull = distribution.getType();
        if (!typeOrNull.isPresent()) {
            target.setText(defaultMessage);
        } else {
            Type type = typeOrNull.get();
            switch (type) {
                case WRAPPER:
                case LOCAL_INSTALLATION:
                case REMOTE_DISTRIBUTION:
                    target.setText(defaultMessage);
                    break;
                case VERSION:
                    target.setText(distribution.getConfiguration());
                    break;
                default:
                    throw new GradlePluginsRuntimeException("Unrecognized Gradle distribution type: " + distribution.getType()); //$NON-NLS-1$
            }
        }

        // if the length of the text is changed and the version warning is visible then we have to
        // adjust their horizontal alignment
        target.getParent().layout();
    }

    private void updateGradleVersionWarningLabel() {
        try {
            GradleVersion version = GradleVersion.version(this.gradleVersionLabel.getText());
            MissingFeatures missingFeatures = new MissingFeatures(version);
            this.gradleVersionWarningLabel.setVisible(!missingFeatures.getMissingFeatures().isEmpty());
        } catch (IllegalArgumentException e) {
            this.gradleVersionWarningLabel.setVisible(false);
        }
        this.gradleVersionLabel.getParent().layout();
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
        IWizardContainer container = getContainer();
        if (container == null) {
            return;
        }

        try {
            container.run(true, true, new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
                    try {
                        BuildConfiguration buildConfig = getConfiguration().toInternalBuildConfiguration();
                        InitializeNewProjectOperation initializeOperation = new InitializeNewProjectOperation(buildConfig);
                        UpdatePreviewOperation updatePreviewOperation = new UpdatePreviewOperation(buildConfig);
                        CorePlugin.operationManager().run(ToolingApiOperations.concat(initializeOperation, updatePreviewOperation), monitor);
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });
        } catch (InvocationTargetException e) {
            ToolingApiStatus status = WizardHelper.containerExceptionToToolingApiStatus(e);
            if (ToolingApiStatusType.BUILD_CANCELLED.matches(status)) {
                previewCancelled();
            } else {
                previewFailed(status);
            }
        } catch (InterruptedException ignored) {
            previewCancelled();
        }
    }

    private void previewFinished(final BuildEnvironment buildEnvironment, final GradleBuild gradleBuild) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                setErrorMessage(null);
                updateSummary(buildEnvironment);
                showResultTree(gradleBuild);
            }
        });
    }

    private void previewCancelled() {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                setErrorMessage(ProjectWizardMessages.Preview_Cancelled);
                showEmptyResultTree();
            }
        });
    }

    private void previewFailed(final ToolingApiStatus status) {
        status.log();
        Throwable t = status.getException();
        final String stacktrace = t == null ? ProjectWizardMessages.Preview_No_Stacktrace : Throwables.getStackTraceAsString(t);
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                setErrorMessage(ProjectWizardMessages.Preview_Failed);
                showExceptionText(stacktrace);
            }
        });
    }

    private void updateSummary(BuildEnvironment buildEnvironment) {
        if (!getControl().isDisposed()) {
            // update Gradle user home
            String gradleUserHome = getGradleUserHomePath(buildEnvironment);
            ProjectPreviewWizardPage.this.gradleUserHomeLabel.setText(gradleUserHome);

            // update Gradle version
            String gradleVersion = buildEnvironment.getGradle().getGradleVersion();
            ProjectPreviewWizardPage.this.gradleVersionLabel.setText(gradleVersion);
            updateGradleVersionWarningLabel();

            // update Java home
            String javaHome = buildEnvironment.getJava().getJavaHome().getAbsolutePath();
            ProjectPreviewWizardPage.this.javaHomeLabel.setText(javaHome);
        }
    }

    private String getGradleUserHomePath(BuildEnvironment buildEnvironment) {
        File gradleUserHome = getGradleUserHome(buildEnvironment.getGradle());
        return gradleUserHome == null ? "" : gradleUserHome.getAbsolutePath();
    }

    private File getGradleUserHome(GradleEnvironment gradleEnvironment) {
        try {
            return gradleEnvironment.getGradleUserHome();
        } catch (Exception ignore) {
            return null;
        }
    }

    private void showResultTree(GradleBuild buildStructure) {
        if (!getControl().isDisposed()) {
            this.previewResultSuccessTree.removeAll();
            populateRecursively(buildStructure, this.previewResultSuccessTree);
            this.previewResultPages.showPage(this.previewResultSuccessTree);
        }
    }

    private void showEmptyResultTree() {
        if (!getControl().isDisposed()) {
            this.previewResultSuccessTree.removeAll();
            this.previewResultPages.showPage(this.previewResultSuccessTree);
        }
    }

    private void showExceptionText(String stackTrace) {
        if (!getControl().isDisposed()) {
            this.previewResultErrorText.setText(stackTrace);
            this.previewResultPages.showPage(this.previewResultErrorText);
        }
    }

    private void populateRecursively(GradleBuild gradleBuild, Tree parent) {
        BasicGradleProject rootProject = gradleBuild.getRootProject();
        TreeItem rootTreeItem = new TreeItem(this.previewResultSuccessTree, SWT.NONE);
        rootTreeItem.setExpanded(true);
        rootTreeItem.setText(rootProject.getName());
        populateRecursively(rootProject, rootTreeItem);
        for (GradleBuild includedBuilds : gradleBuild.getIncludedBuilds()) {
            populateRecursively(includedBuilds, parent);
        }
    }

    private void populateRecursively(BasicGradleProject gradleProject, TreeItem parent) {
        for (BasicGradleProject childProject : gradleProject.getChildren()) {
            TreeItem treeItem = new TreeItem(parent, SWT.NONE);
            treeItem.setText(childProject.getName());
            populateRecursively(childProject, treeItem);
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

    private static BuildEnvironment fetchBuildEnvironment(BuildConfiguration buildConfig, CancellationTokenSource tokenSource, IProgressMonitor monitor) {
        ModelProvider modelProvider = CorePlugin.internalGradleWorkspace().getGradleBuild(buildConfig).getModelProvider();
        return modelProvider.fetchModel(BuildEnvironment.class, FetchStrategy.FORCE_RELOAD, tokenSource, monitor);
    }

    private static GradleBuild fetchGradleBuildStructure(BuildConfiguration buildConfig, CancellationTokenSource tokenSource, IProgressMonitor monitor) {
        ModelProvider modelProvider = CorePlugin.internalGradleWorkspace().getGradleBuild(buildConfig).getModelProvider();
        return modelProvider.fetchModel(GradleBuild.class, FetchStrategy.FORCE_RELOAD, tokenSource, monitor);
    }

    /**
     * Loads the preview and presents the results on the UI.
     */
    private class UpdatePreviewOperation extends BaseToolingApiOperation {

        private final BuildConfiguration buildConfig;

        public UpdatePreviewOperation(BuildConfiguration buildConfig) {
            super("Update preview");
            this.buildConfig = buildConfig;
        }

        @Override
        public void runInToolingApi(CancellationTokenSource tokenSource, IProgressMonitor monitor) throws Exception {
            SubMonitor progress = SubMonitor.convert(monitor);
            progress.setWorkRemaining(2);

            BuildEnvironment buildEnvironment = fetchBuildEnvironment(this.buildConfig, tokenSource, progress.newChild(1));
            GradleBuild gradleBuild = fetchGradleBuildStructure(this.buildConfig, tokenSource, progress.newChild(1));
            previewFinished(buildEnvironment, gradleBuild);
        }

        @Override
        public ISchedulingRule getRule() {
            return ResourcesPlugin.getWorkspace().getRoot();
        }
    }
}
