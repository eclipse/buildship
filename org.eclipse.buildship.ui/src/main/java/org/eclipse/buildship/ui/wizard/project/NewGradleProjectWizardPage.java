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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingutils.binding.Property;
import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration;
import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.buildship.ui.UiPluginConstants;
import org.eclipse.buildship.ui.i18n.UiMessages;
import org.eclipse.buildship.ui.util.file.DirectoryDialogSelectionListener;
import org.eclipse.buildship.ui.util.layout.LayoutUtils;
import org.eclipse.buildship.ui.util.selection.TargetWidgetsInvertingSelectionListener;
import org.eclipse.buildship.ui.util.widget.UiBuilder;
import org.eclipse.buildship.ui.util.workbench.WorkingSetUtils;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkingSet;

import java.io.File;
import java.util.List;

/**
 * Page on the {@link org.eclipse.buildship.ui.wizard.project.ProjectCreationWizard} declaring the project name and project location.
 */
public final class NewGradleProjectWizardPage extends AbstractWizardPage {

    private final Property<String> projectNameProperty;
    private final Property<File> targetProjectDirProperty;

    private Text projectNameText;
    private Button useDefaultWorkspaceLocationButton;
    private Combo customLocationCombo;
    private WorkingSetConfigurationWidget workingSetConfigurationWidget;

    public NewGradleProjectWizardPage(ProjectImportConfiguration configuration, Property<String> projectNameProperty, Property<File> targetProjectDirProperty) {
        super("NewGradleProject", ProjectWizardMessages.Title_NewGradleProjectWizardPage, ProjectWizardMessages.InfoMessage_NewGradleProjectWizardPageDefault, //$NON-NLS-1$
                configuration, ImmutableList.of(projectNameProperty, targetProjectDirProperty, configuration.getWorkingSets()));
        this.projectNameProperty = projectNameProperty;
        this.targetProjectDirProperty = targetProjectDirProperty;
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
        Group locationGroup = new Group(root, SWT.NONE);
        locationGroup.setText(ProjectWizardMessages.Group_Label_ProjectLocation);
        GridLayoutFactory.swtDefaults().numColumns(3).applyTo(locationGroup);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).span(3, SWT.DEFAULT).applyTo(locationGroup);

        // project custom location check button to enable/disable the default workspace location
        this.useDefaultWorkspaceLocationButton = new Button(locationGroup, SWT.CHECK);
        this.useDefaultWorkspaceLocationButton.setText(ProjectWizardMessages.Button_UseDefaultLocation);
        this.useDefaultWorkspaceLocationButton.setSelection(true);
        GridDataFactory.swtDefaults().span(3, SWT.DEFAULT).applyTo(this.useDefaultWorkspaceLocationButton);

        // project custom location label
        uiBuilderFactory.newLabel(locationGroup).alignLeft().text(ProjectWizardMessages.Label_CustomLocation);

        // project custom location combo for typing an alternative project path, which also provides recently used paths
        this.customLocationCombo = new Combo(locationGroup, SWT.BORDER);
        this.customLocationCombo.setText(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
        this.customLocationCombo.setEnabled(!this.useDefaultWorkspaceLocationButton.getSelection());
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(this.customLocationCombo);

        // browse button for file chooser
        Button customLocationBrowseButton = uiBuilderFactory.newButton(locationGroup).alignLeft().control();
        customLocationBrowseButton.setText(UiMessages.Button_Label_Browse);
        customLocationBrowseButton.setEnabled(!this.useDefaultWorkspaceLocationButton.getSelection());
        customLocationBrowseButton.addSelectionListener(new DirectoryDialogSelectionListener(root.getShell(), this.customLocationCombo, ProjectWizardMessages.Label_ProjectRootDirectory));

        // working set container
        Group workingSetGroup = new Group(root, SWT.NONE);
        workingSetGroup.setText(ProjectWizardMessages.Group_Label_WorkingSets);
        GridLayoutFactory.swtDefaults().applyTo(workingSetGroup);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).span(3, SWT.DEFAULT).applyTo(workingSetGroup);

        this.workingSetConfigurationWidget = new WorkingSetConfigurationWidget(new String[]{UiPluginConstants.RESOURCE, UiPluginConstants.JAVA}, UiPlugin.getInstance().getDialogSettings());
        this.workingSetConfigurationWidget.createContent(workingSetGroup);
        this.workingSetConfigurationWidget.setWorkingSets(WorkingSetUtils.toWorkingSets(getConfiguration().getWorkingSets().getValue()));
        this.workingSetConfigurationWidget.getWorkingSetsEnabledButton().setSelection(getConfiguration().getApplyWorkingSets().getValue());
        this.workingSetConfigurationWidget.getWorkingSetsCombo().setEnabled(getConfiguration().getApplyWorkingSets().getValue());

        // add listener to deal with the enabling of the widgets that are part of the location group
        this.useDefaultWorkspaceLocationButton.addSelectionListener(new TargetWidgetsInvertingSelectionListener(this.useDefaultWorkspaceLocationButton, this.customLocationCombo, customLocationBrowseButton));
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
                updateLocation();
            }
        });
        this.customLocationCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                updateLocation();
            }
        });
        this.workingSetConfigurationWidget.addWorkingSetChangeListener(new WorkingSetChangedListener() {

            @Override
            public void workingSetsChanged(List<IWorkingSet> workingSets) {
                List<String> workingSetNames = toWorkingSetNames(workingSets);
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
                new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString()) :
                !Strings.isNullOrEmpty(this.customLocationCombo.getText()) ? new File(this.customLocationCombo.getText()) : null;
        File projectDir = parentLocation != null ? new File(parentLocation, this.projectNameText.getText()) : null;

        // always update project name last to ensure project name validation errors have precedence in the UI
        getConfiguration().getProjectDir().setValue(projectDir);
        this.targetProjectDirProperty.setValue(projectDir);
        this.projectNameProperty.setValue(NewGradleProjectWizardPage.this.projectNameText.getText());
    }

    @Override
    protected String getPageContextInformation() {
        return ProjectWizardMessages.InfoMessage_NewGradleProjectWizardPageContext;
    }

}
