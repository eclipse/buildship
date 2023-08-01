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

import com.google.common.collect.ImmutableList;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;

import org.eclipse.buildship.core.internal.util.file.FileUtils;
import org.eclipse.buildship.ui.internal.UiPlugin;
import org.eclipse.buildship.ui.internal.UiPluginConstants;
import org.eclipse.buildship.ui.internal.i18n.UiMessages;
import org.eclipse.buildship.ui.internal.util.file.DirectoryDialogSelectionListener;
import org.eclipse.buildship.ui.internal.util.layout.LayoutUtils;
import org.eclipse.buildship.ui.internal.util.selection.TargetWidgetsInvertingSelectionListener;
import org.eclipse.buildship.ui.internal.util.widget.UiBuilder;
import org.eclipse.buildship.ui.internal.util.workbench.WorkingSetUtils;

/**
 * Page on the {@link org.eclipse.buildship.ui.internal.wizard.project.ProjectCreationWizard} declaring the project name and project location.
 */
public final class NewGradleProjectWizardPage extends AbstractWizardPage {

    private final ProjectCreationConfiguration creationConfiguration;

    private Text projectNameText;
    private Button useDefaultWorkspaceLocationButton;
    private Text customLocationText;
    private WorkingSetConfigurationWidget workingSetConfigurationWidget;

    public NewGradleProjectWizardPage(ProjectImportConfiguration importConfiguration, ProjectCreationConfiguration creationConfiguration) {
        super("NewGradleProject", ProjectWizardMessages.Title_NewGradleProjectWizardPage, ProjectWizardMessages.InfoMessage_NewGradleProjectWizardPageDefault, //$NON-NLS-1$
                importConfiguration, ImmutableList.of(creationConfiguration.getProjectName(), creationConfiguration.getCustomLocation(), creationConfiguration.getTargetProjectDir(), importConfiguration.getWorkingSets()));
        this.creationConfiguration = creationConfiguration;
    }

    @Override
    protected void createWidgets(Composite root) {
        root.setLayout(LayoutUtils.newGridLayout(3));
        createContent(root);
        bindToConfiguration();
    }

    private void createContent(Composite root) {
        UiBuilder.UiBuilderFactory uiBuilderFactory = getUiBuilderFactory();

        // project name container
        Composite projectNameComposite = new Composite(root, SWT.NONE);
        GridLayoutFactory.swtDefaults().extendedMargins(0, 0, 0, 10).numColumns(2).applyTo(projectNameComposite);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).span(3, SWT.DEFAULT).applyTo(projectNameComposite);

        // project name label
        uiBuilderFactory.newLabel(projectNameComposite).alignLeft().text(ProjectWizardMessages.Label_ProjectName).control();

        // project name text field
        this.projectNameText = uiBuilderFactory.newText(projectNameComposite).alignFillHorizontal().control();

        // project location container
        Group locationGroup = uiBuilderFactory.newGroup(root).text(ProjectWizardMessages.Group_Label_ProjectLocation).control();
        GridLayoutFactory.swtDefaults().numColumns(3).applyTo(locationGroup);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).span(3, SWT.DEFAULT).applyTo(locationGroup);

        // project custom location check button to enable/disable the default workspace location
        this.useDefaultWorkspaceLocationButton = uiBuilderFactory.newCheckbox(locationGroup).text(ProjectWizardMessages.Button_UseDefaultLocation).control();
        this.useDefaultWorkspaceLocationButton.setSelection(this.creationConfiguration.getUseDefaultLocation().getValue());
        GridDataFactory.swtDefaults().span(3, SWT.DEFAULT).applyTo(this.useDefaultWorkspaceLocationButton);

        // project custom location label
        uiBuilderFactory.newLabel(locationGroup).alignLeft().text(ProjectWizardMessages.Label_CustomLocation);

        // project custom location combo for typing an alternative project path, which also provides recently used paths
        this.customLocationText = uiBuilderFactory.newText(locationGroup).text(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString()).control();
        this.customLocationText.setEnabled(!this.useDefaultWorkspaceLocationButton.getSelection());
        this.customLocationText.setText(this.useDefaultWorkspaceLocationButton.getSelection() ?
                ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString() :
                FileUtils.getAbsolutePath(this.creationConfiguration.getCustomLocation().getValue()).or(""));
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(this.customLocationText);

        // browse button for file chooser
        Button customLocationBrowseButton = uiBuilderFactory.newButton(locationGroup).alignLeft().text(UiMessages.Button_Label_Browse).control();
        customLocationBrowseButton.setEnabled(!this.useDefaultWorkspaceLocationButton.getSelection());
        customLocationBrowseButton.addSelectionListener(new DirectoryDialogSelectionListener(root.getShell(), this.customLocationText, ProjectWizardMessages.Label_ProjectRootDirectory));

        // working set container
        Group workingSetGroup = uiBuilderFactory.newGroup(root).text(ProjectWizardMessages.Group_Label_WorkingSets).control();
        GridLayoutFactory.swtDefaults().applyTo(workingSetGroup);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).span(3, SWT.DEFAULT).applyTo(workingSetGroup);

        this.workingSetConfigurationWidget = new WorkingSetConfigurationWidget(new String[]{UiPluginConstants.RESOURCE, UiPluginConstants.JAVA}, UiPlugin.getInstance().getDialogSettings());
        this.workingSetConfigurationWidget.createContent(workingSetGroup);
        this.workingSetConfigurationWidget.modifyCurrentWorkingSetItem(WorkingSetUtils.toWorkingSets(getConfiguration().getWorkingSets().getValue()));
        this.workingSetConfigurationWidget.getWorkingSetsEnabledButton().setSelection(getConfiguration().getApplyWorkingSets().getValue());
        this.workingSetConfigurationWidget.getWorkingSetsCombo().setEnabled(getConfiguration().getApplyWorkingSets().getValue());
        this.workingSetConfigurationWidget.getWorkingSetsSelectButton().setEnabled(getConfiguration().getApplyWorkingSets().getValue());

        // add listener to deal with the enabling of the widgets that are part of the location group
        this.useDefaultWorkspaceLocationButton.addSelectionListener(new TargetWidgetsInvertingSelectionListener(this.useDefaultWorkspaceLocationButton, this.customLocationText, customLocationBrowseButton));
    }

    private void bindToConfiguration() {
        this.projectNameText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                updateLocation();
            }
        });
        this.useDefaultWorkspaceLocationButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String location = NewGradleProjectWizardPage.this.useDefaultWorkspaceLocationButton.getSelection() ?
                        ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString() :
                        FileUtils.getAbsolutePath(NewGradleProjectWizardPage.this.creationConfiguration.getCustomLocation().getValue()).or("");
                NewGradleProjectWizardPage.this.customLocationText.setText(location);
                updateLocation();
            }
        });
        this.customLocationText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                updateLocation();
            }
        });
        this.workingSetConfigurationWidget.addWorkingSetChangeListener(new WorkingSetChangedListener() {

            @Override
            public void workingSetsChanged(List<IWorkingSet> workingSets) {
                List<String> workingSetNames = WorkingSetUtils.toWorkingSetNames(workingSets);
                getConfiguration().setWorkingSets(workingSetNames);
            }
        });
        this.workingSetConfigurationWidget.getWorkingSetsEnabledButton().addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean selected = NewGradleProjectWizardPage.this.workingSetConfigurationWidget.getWorkingSetsEnabledButton().getSelection();
                getConfiguration().setApplyWorkingSets(selected);
            }
        });
    }

    private void updateLocation() {
        File parentLocation = this.useDefaultWorkspaceLocationButton.getSelection() ?
                ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile() :
                FileUtils.getAbsoluteFile(this.customLocationText.getText()).orNull();
        File projectDir = parentLocation != null ? new File(parentLocation, this.projectNameText.getText()) : null;

        // always update project name last to ensure project name validation errors have precedence in the UI
        getConfiguration().getProjectDir().setValue(projectDir);
        this.creationConfiguration.setTargetProjectDir(projectDir);
        if (!this.useDefaultWorkspaceLocationButton.getSelection()) {
            this.creationConfiguration.setCustomLocation(FileUtils.getAbsoluteFile(this.customLocationText.getText()).orNull());
        }
        this.creationConfiguration.setUseDefaultLocation(this.useDefaultWorkspaceLocationButton.getSelection());
        this.creationConfiguration.setProjectName(this.projectNameText.getText());
    }
}
