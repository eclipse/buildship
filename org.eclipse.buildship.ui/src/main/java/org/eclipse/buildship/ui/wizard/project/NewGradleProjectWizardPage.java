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

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingutils.binding.Property;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;

import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration;
import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.buildship.ui.UiPluginConstants;
import org.eclipse.buildship.ui.i18n.UiMessages;
import org.eclipse.buildship.ui.util.file.DirectoryDialogSelectionListener;
import org.eclipse.buildship.ui.util.widget.UiBuilder;

/**
 * First Wizard page for the new Gradle project wizard.
 *
 */
public final class NewGradleProjectWizardPage extends AbstractWizardPage {

    private Text projectNameText;
    private Button defaultWorkspaceLocationButton;
    private Combo projectDirCombo;
    private Button projectDirBrowseButton;
    private WorkingSetConfigurationWidget workingSetConfigurationComposite;

    public NewGradleProjectWizardPage(ProjectImportConfiguration configuration) {
        super("NewGradleProject", ProjectWizardMessages.Title_NewGradleProjectWizardPage, ProjectWizardMessages.InfoMessage_NewGradleProjectWizardPageDefault, //$NON-NLS-1$
                configuration, ImmutableList.<Property<?>> of(configuration.getNewProjectName(), configuration.getNewProjectLocation()));
    }

    @Override
    protected void createWidgets(Composite root) {
        GridLayoutFactory.swtDefaults().numColumns(3).applyTo(root);
        createContent(root);
        initializeForm();
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
        this.defaultWorkspaceLocationButton = new Button(locationGroup, SWT.CHECK);
        GridDataFactory.swtDefaults().span(3, SWT.DEFAULT).applyTo(this.defaultWorkspaceLocationButton);
        this.defaultWorkspaceLocationButton.setText(ProjectWizardMessages.Button_UseDefaultLocation);

        // project custom location label
        uiBuilderFactory.newLabel(locationGroup).alignLeft().text(ProjectWizardMessages.Label_CustomLocation);

        // project custom location combo for typing an alternative project path, which also provides recently used paths
        this.projectDirCombo = new Combo(locationGroup, SWT.BORDER);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(this.projectDirCombo);

        // browse button for file chooser
        this.projectDirBrowseButton = uiBuilderFactory.newButton(locationGroup).alignLeft().text(UiMessages.Button_Label_Browse).control();
        this.projectDirBrowseButton.addSelectionListener(new DirectoryDialogSelectionListener(root.getShell(), this.projectDirCombo, ProjectWizardMessages.Label_ProjectRootDirectory));

        // working set container
        Group workingSetGroup = new Group(root, SWT.NONE);
        workingSetGroup.setText(ProjectWizardMessages.Group_Label_WorkingSets);
        GridLayoutFactory.swtDefaults().applyTo(workingSetGroup);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).span(3, SWT.DEFAULT).applyTo(workingSetGroup);

        this.workingSetConfigurationComposite = new WorkingSetConfigurationWidget(new String[] { UiPluginConstants.RESOURCE, UiPluginConstants.JAVA }, UiPlugin.getInstance().getDialogSettings());
        this.workingSetConfigurationComposite.createContent(workingSetGroup);
    }

    private void initializeForm() {
        // project name
        String projectName = getConfiguration().getNewProjectName().getValue();
        this.projectNameText.setText(MoreObjects.firstNonNull(projectName, ""));

        // default location checkbox
        Boolean useDefaultLocation = getConfiguration().getUseWorkspaceLocation().getValue();
        this.defaultWorkspaceLocationButton.setSelection(useDefaultLocation);

        // list of previous locations
        List<String> locations = getConfiguration().getPossibleLocations().getValue();
        if (locations.isEmpty()) {
            IPath location = ResourcesPlugin.getWorkspace().getRoot().getLocation();
            this.projectDirCombo.setText(location.toOSString());
        } else {
            this.projectDirCombo.setItems(locations.toArray(new String[locations.size()]));
            this.projectDirCombo.setText(locations.get(0));
        }
        this.projectDirCombo.setEnabled(!useDefaultLocation);

        // location selector button
        this.projectDirBrowseButton.setEnabled(!useDefaultLocation);
    }

    private void bindToConfiguration() {
        this.projectNameText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                collectAndUpdateConfiguration();
            }
        });
        this.projectDirCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                collectAndUpdateConfiguration();
            }
        });
        this.defaultWorkspaceLocationButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean useDefaultLocation = NewGradleProjectWizardPage.this.defaultWorkspaceLocationButton.getSelection();
                NewGradleProjectWizardPage.this.projectDirCombo.setEnabled(!useDefaultLocation);
                NewGradleProjectWizardPage.this.projectDirBrowseButton.setEnabled(!useDefaultLocation);
                collectAndUpdateConfiguration();
            }
        });
        getConfiguration().setWorkingSets(toWorkingSetNames(ImmutableList.copyOf(this.workingSetConfigurationComposite.getSelectedWorkingSets())));
        this.workingSetConfigurationComposite.addWorkingSetChangeListener(new WorkingSetChangedListener() {

            @Override
            public void workingSetsChanged(List<IWorkingSet> workingSets) {
                getConfiguration().setWorkingSets(toWorkingSetNames(workingSets));
            }
        });
    }

    @Override
    protected String getPageContextInformation() {
        return ProjectWizardMessages.InfoMessage_NewGradleProjectWizardPageContext;
    }

    private void collectAndUpdateConfiguration() {
        boolean useDefaultLocation = NewGradleProjectWizardPage.this.defaultWorkspaceLocationButton.getSelection();
        File newProjectLocation = calculateProjectLocation();
        getConfiguration().getNewProjectName().setValue(NewGradleProjectWizardPage.this.projectNameText.getText());
        getConfiguration().getNewProjectLocation().setValue(newProjectLocation);
        getConfiguration().getUseWorkspaceLocation().setValue(useDefaultLocation);
        getConfiguration().getProjectDir().setValue(newProjectLocation);
    }

    private File calculateProjectLocation() {
        String projectName = NewGradleProjectWizardPage.this.projectNameText.getText();
        if (NewGradleProjectWizardPage.this.defaultWorkspaceLocationButton.getSelection()) {
            IPath location = ResourcesPlugin.getWorkspace().getRoot().getLocation();
            return new File(location.toOSString(), projectName);
        } else {
            return new File(NewGradleProjectWizardPage.this.projectDirCombo.getText(), projectName);
        }
    }
}
