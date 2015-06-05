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
import java.util.List;

import com.google.common.collect.ImmutableList;

import org.eclipse.buildship.ui.util.workbench.WorkingSetUtils;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;

import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration;
import org.eclipse.buildship.core.util.file.FileUtils;
import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.buildship.ui.UiPluginConstants;
import org.eclipse.buildship.ui.i18n.UiMessages;
import org.eclipse.buildship.ui.util.file.DirectoryDialogSelectionListener;
import org.eclipse.buildship.ui.util.layout.LayoutUtils;
import org.eclipse.buildship.ui.util.widget.UiBuilder;

/**
 * Page in the {@link ProjectImportWizard} specifying the Gradle root project folder to import.
 */
public final class GradleProjectWizardPage extends AbstractWizardPage {

    private Text projectDirText;
    private WorkingSetConfigurationWidget workingSetConfigurationWidget;

    public GradleProjectWizardPage(ProjectImportConfiguration configuration) {
        super("GradleProject", ProjectWizardMessages.Title_GradleProjectWizardPage, ProjectWizardMessages.InfoMessage_GradleProjectWizardPageDefault, //$NON-NLS-1$
                configuration, ImmutableList.of(configuration.getProjectDir(), configuration.getWorkingSets()));
    }

    @Override
    protected void createWidgets(Composite root) {
        root.setLayout(LayoutUtils.newGridLayout(3));
        createContent(root);
        bindToConfiguration();
    }

    private void createContent(Composite root) {
        UiBuilder.UiBuilderFactory uiBuilderFactory = getUiBuilderFactory();

        // project directory container
        Composite projectDirectoryComposite = new Composite(root, SWT.NONE);
        GridLayoutFactory.swtDefaults().numColumns(3).extendedMargins(0, 0, 0, 10).applyTo(projectDirectoryComposite);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).span(3, SWT.DEFAULT).applyTo(projectDirectoryComposite);

        // project directory label
        uiBuilderFactory.newLabel(projectDirectoryComposite).alignLeft().text(ProjectWizardMessages.Label_ProjectRootDirectory).control();

        // project directory text field
        File projectDir = getConfiguration().getProjectDir().getValue();
        String projectDirValue = FileUtils.getAbsolutePath(projectDir).orNull();
        this.projectDirText = uiBuilderFactory.newText(projectDirectoryComposite).alignFillHorizontal().text(projectDirValue).control();

        // browse button for file chooser
        Button projectDirBrowseButton = uiBuilderFactory.newButton(projectDirectoryComposite).alignLeft().text(UiMessages.Button_Label_Browse).control();
        projectDirBrowseButton.addSelectionListener(new DirectoryDialogSelectionListener(root.getShell(), this.projectDirText, ProjectWizardMessages.Label_ProjectRootDirectory));

        // working set container
        Group workingSetGroup = new Group(root, SWT.NONE);
        workingSetGroup.setText(ProjectWizardMessages.Group_Label_WorkingSets);
        GridLayoutFactory.swtDefaults().applyTo(workingSetGroup);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).span(3, SWT.DEFAULT).applyTo(workingSetGroup);

        this.workingSetConfigurationWidget = new WorkingSetConfigurationWidget(new String[] { UiPluginConstants.RESOURCE, UiPluginConstants.JAVA }, UiPlugin.getInstance().getDialogSettings());
        this.workingSetConfigurationWidget.createContent(workingSetGroup);
        this.workingSetConfigurationWidget.setWorkingSets(WorkingSetUtils.toWorkingSets(getConfiguration().getWorkingSets().getValue()));
    }

    private void bindToConfiguration() {
        this.projectDirText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                getConfiguration().setProjectDir(FileUtils.getAbsoluteFile(GradleProjectWizardPage.this.projectDirText.getText()).orNull());
            }
        });
        this.workingSetConfigurationWidget.addWorkingSetChangeListener(new WorkingSetChangedListener() {

            @Override
            public void workingSetsChanged(List<IWorkingSet> workingSets) {
                getConfiguration().setWorkingSets(toWorkingSetNames(workingSets));
            }
        });
    }

    @Override
    protected String getPageContextInformation() {
        return ProjectWizardMessages.InfoMessage_GradleProjectWizardPageContext;
    }

}
