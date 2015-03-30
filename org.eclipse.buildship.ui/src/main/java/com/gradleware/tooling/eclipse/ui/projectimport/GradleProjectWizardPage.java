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

package com.gradleware.tooling.eclipse.ui.projectimport;

import java.io.File;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.eclipse.core.projectimport.ProjectImportConfiguration;
import com.gradleware.tooling.eclipse.core.util.file.FileUtils;
import com.gradleware.tooling.eclipse.ui.util.file.DirectoryDialogSelectionListener;
import com.gradleware.tooling.eclipse.ui.util.layout.LayoutUtils;
import com.gradleware.tooling.eclipse.ui.util.widget.UiBuilder;
import com.gradleware.tooling.toolingutils.binding.Property;

/**
 * First page in the {@link ProjectImportWizard} specifying the Gradle root project folder to
 * import.
 */
public final class GradleProjectWizardPage extends AbstractWizardPage {

    private Text projectDirText;

    public GradleProjectWizardPage(ProjectImportConfiguration configuration) {
        super("GradleProject", ProjectImportMessages.Title_GradleProjectWizardPage, ProjectImportMessages.InfoMessage_GradleProjectWizardPageDefault, //$NON-NLS-1$
                configuration, ImmutableList.<Property<?>> of(configuration.getProjectDir()));
    }

    @Override
    protected void createWidgets(Composite root) {
        root.setLayout(LayoutUtils.newGridLayout(3));
        createContent(root);
        bindToConfiguration();
    }

    private void createContent(Composite root) {
        UiBuilder.UiBuilderFactory uiBuilderFactory = getUiBuilderFactory();

        // project directory label
        uiBuilderFactory.newLabel(root).alignLeft().text(ProjectImportMessages.Label_ProjectRootDirectory).control();

        // project directory text field
        File projectDir = getConfiguration().getProjectDir().getValue();
        String projectDirValue = FileUtils.getAbsolutePath(projectDir).orNull();
        this.projectDirText = uiBuilderFactory.newText(root).alignFillHorizontal().text(projectDirValue).control();

        // browse button for file chooser
        Button projectDirBrowseButton = uiBuilderFactory.newButton(root).alignLeft().text(ProjectImportMessages.Button_Label_Browse).control();
        projectDirBrowseButton.addSelectionListener(new DirectoryDialogSelectionListener(root.getShell(), this.projectDirText, ProjectImportMessages.Label_ProjectRootDirectory));
    }

    private void bindToConfiguration() {
        this.projectDirText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                getConfiguration().setProjectDir(FileUtils.getAbsoluteFile(GradleProjectWizardPage.this.projectDirText.getText()).orNull());
            }
        });
    }

}
