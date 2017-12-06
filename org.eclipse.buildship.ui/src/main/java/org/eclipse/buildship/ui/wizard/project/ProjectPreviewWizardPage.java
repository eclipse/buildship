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
import java.lang.reflect.InvocationTargetException;

import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.GradleConnector;
import org.gradle.util.GradleVersion;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.OmniGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniGradleProjectStructure;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.util.Pair;
import com.gradleware.tooling.toolingutils.binding.Property;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
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
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.configuration.BuildConfiguration;
import org.eclipse.buildship.core.gradle.MissingFeatures;
import org.eclipse.buildship.core.i18n.CoreMessages;
import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration;
import org.eclipse.buildship.core.util.gradle.GradleDistributionFormatter;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper;
import org.eclipse.buildship.core.util.progress.ToolingApiStatus;
import org.eclipse.buildship.core.util.progress.ToolingApiStatus.ToolingApiStatusType;
import org.eclipse.buildship.core.workspace.ModelProvider;
import org.eclipse.buildship.ui.util.font.FontUtils;
import org.eclipse.buildship.ui.util.layout.LayoutUtils;
import org.eclipse.buildship.ui.util.widget.UiBuilder;

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
    private Tree projectPreviewTree;

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
        this.gradleUserHomeLabel.setText(CoreMessages.Value_Unknown);
        this.javaHomeLabel.setText(CoreMessages.Value_Unknown);
        updateGradleVersionWarningLabel();
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
        ProjectPreviewWizardPage.this.gradleVersionLabel.getParent().layout();
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
                    SubMonitor progress = SubMonitor.convert(monitor);
                    progress.setTaskName("Loading project preview");
                    progress.setWorkRemaining(3);

                    try {
                        CancellationTokenSource tokenSource = GradleConnector.newCancellationTokenSource();
                        BuildConfiguration buildConfig = getConfiguration().toBuildConfig();

                        NewGradleProjectInitializer.initProjectIfNotExists(buildConfig, tokenSource, progress.newChild(1));
                        OmniBuildEnvironment buildEnvironment = fetchBuildEnvironment(buildConfig, tokenSource, progress.newChild(1));
                        OmniGradleBuild gradleBuild = fetchGradleBuildStructure(buildConfig, tokenSource, progress.newChild(1));

                        updateSummary(buildEnvironment);
                        populateTree(gradleBuild);

                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });
        } catch (InvocationTargetException e) {
            Throwable throwable = e.getTargetException() == null ? e : e.getTargetException();
            ToolingApiStatus status = ToolingApiStatus.from("Project preview", throwable);
            if (ToolingApiStatusType.BUILD_CANCELLED.getCode() == status.getCode()) {
                displayCancellationWarning();
            } else {
                status.handleDefault();
                clearTree();
            }
        } catch (InterruptedException ignored) {
            displayCancellationWarning();
        }
    }

    private void updateSummary(final OmniBuildEnvironment buildEnvironment) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                if (!getControl().isDisposed()) {
                    // update Gradle user home
                    if (buildEnvironment.getGradle().getGradleUserHome().isPresent()) {
                        String gradleUserHome = buildEnvironment.getGradle().getGradleUserHome().get().getAbsolutePath();
                        ProjectPreviewWizardPage.this.gradleUserHomeLabel.setText(gradleUserHome);
                    }

                    // update Gradle version
                    String gradleVersion = buildEnvironment.getGradle().getGradleVersion();
                    ProjectPreviewWizardPage.this.gradleVersionLabel.setText(gradleVersion);
                    updateGradleVersionWarningLabel();

                    // update Java home
                    String javaHome = buildEnvironment.getJava().getJavaHome().getAbsolutePath();
                    ProjectPreviewWizardPage.this.javaHomeLabel.setText(javaHome);
                }
            }
        });
    }

    private void populateTree(final OmniGradleBuild buildStructure) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                if (!getControl().isDisposed()) {
                    ProjectPreviewWizardPage.this.projectPreviewTree.removeAll();
                    populateRecursively(buildStructure, ProjectPreviewWizardPage.this.projectPreviewTree);
                }
            }
        });
    }

    private void clearTree() {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                if (!getControl().isDisposed()) {
                    ProjectPreviewWizardPage.this.projectPreviewTree.removeAll();
                }
            }
        });
    }

    private void displayCancellationWarning() {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                if (!getControl().isDisposed()) {
                    ProjectPreviewWizardPage.this.projectPreviewTree.removeAll();
                    TreeItem root = new TreeItem(ProjectPreviewWizardPage.this.projectPreviewTree, SWT.NONE);
                    root.setText("Preview cancelled");

                }
            }
        });
    }

    private void populateRecursively(OmniGradleBuild gradleBuild, Tree parent) {
        OmniGradleProjectStructure rootProject = gradleBuild.getRootProject();
        TreeItem rootTreeItem = new TreeItem(ProjectPreviewWizardPage.this.projectPreviewTree, SWT.NONE);
        rootTreeItem.setExpanded(true);
        rootTreeItem.setText(rootProject.getName());
        populateRecursively(rootProject, rootTreeItem);
        for (OmniGradleBuild includedBuilds : gradleBuild.getIncludedBuilds()) {
            populateRecursively(includedBuilds, parent);
        }
    }

    private void populateRecursively(OmniGradleProjectStructure gradleProjectStructure, TreeItem parent) {
        for (OmniGradleProjectStructure childProject : gradleProjectStructure.getChildren()) {
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

    private static OmniBuildEnvironment fetchBuildEnvironment(BuildConfiguration buildConfig, CancellationTokenSource tokenSource, IProgressMonitor monitor) {
        ModelProvider modelProvider = CorePlugin.gradleWorkspaceManager().getGradleBuild(buildConfig).getModelProvider();
        return modelProvider.fetchBuildEnvironment(FetchStrategy.FORCE_RELOAD, tokenSource, monitor);
    }

    private static OmniGradleBuild fetchGradleBuildStructure(BuildConfiguration buildConfig, CancellationTokenSource tokenSource, IProgressMonitor monitor) {
        ModelProvider modelProvider = CorePlugin.gradleWorkspaceManager().getGradleBuild(buildConfig).getModelProvider();
        return modelProvider.fetchGradleBuild(FetchStrategy.FORCE_RELOAD, tokenSource, monitor);
    }

}
