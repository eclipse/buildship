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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.eclipse.core.projectimport.ProjectImportConfiguration;
import com.gradleware.tooling.eclipse.core.util.file.FileUtils;
import com.gradleware.tooling.eclipse.ui.util.file.DirectoryDialogSelectionListener;
import com.gradleware.tooling.eclipse.ui.util.layout.LayoutUtils;
import com.gradleware.tooling.eclipse.ui.util.widget.UiBuilder;
import com.gradleware.tooling.toolingutils.binding.Property;

/**
 * Third page on the {@link ProjectImportWizard} to specify all advanced configurations options.
 */
public final class AdvancedOptionsWizardPage extends AbstractWizardPage {

    private Text javaHomeText;
    private Text gradleUserHomeText;
    private Text jvmArgumentsText;
    private Text programArgumentsText;

    public AdvancedOptionsWizardPage(ProjectImportConfiguration configuration) {
        super("AdvancedOptions", ProjectImportMessages.Title_AdvancedOptionsWizardPage, ProjectImportMessages.InfoMessage_AdvancedOptionsWizardPageDefault, //$NON-NLS-1$
                configuration, ImmutableList.<Property<?>> of(configuration.getJavaHome(), configuration.getGradleUserHome(), configuration.getJvmArguments()));
    }

    @Override
    protected void createWidgets(Composite root) {
        root.setLayout(LayoutUtils.newGridLayout(3));
        createContent(root);
        bindToConfiguration();
    }

    private void createContent(Composite root) {
        UiBuilder.UiBuilderFactory uiBuilderFactory = getUiBuilderFactory();

        // first line: gradle user home
        uiBuilderFactory.newLabel(root).alignLeft().text(ProjectImportMessages.Label_GradleUserHome);

        File gradleUserHome = getConfiguration().getGradleUserHome().getValue();
        String gradleUserHomeString = FileUtils.getAbsolutePath(gradleUserHome).orNull();
        this.gradleUserHomeText = uiBuilderFactory.newText(root).alignFillHorizontal().text(gradleUserHomeString).control();

        Button gradleUserHomeButton = uiBuilderFactory.newButton(root).alignLeft().text(ProjectImportMessages.Button_Label_Browse).control();
        gradleUserHomeButton.addSelectionListener(new DirectoryDialogSelectionListener(root.getShell(), this.gradleUserHomeText, ProjectImportMessages.Label_GradleUserHome));

        // second line: java home
        uiBuilderFactory.newLabel(root).text(ProjectImportMessages.Label_JavaHome).alignLeft().control();

        File javaHome = getConfiguration().getJavaHome().getValue();
        String javaHomeString = FileUtils.getAbsolutePath(javaHome).orNull();
        this.javaHomeText = uiBuilderFactory.newText(root).alignFillHorizontal().text(javaHomeString).control();

        Button javaHomeButton = uiBuilderFactory.newButton(root).alignLeft().text(ProjectImportMessages.Button_Label_Browse).control();
        javaHomeButton.addSelectionListener(new DirectoryDialogSelectionListener(root.getShell(), this.javaHomeText, ProjectImportMessages.Label_JavaHome));

        // third line: jvm arguments
        uiBuilderFactory.newLabel(root).alignLeft().text(ProjectImportMessages.Label_JvmArguments);

        String jvmArguments = getConfiguration().getJvmArguments().getValue();
        String jvmArgumentsString = Strings.nullToEmpty(jvmArguments);
        this.jvmArgumentsText = uiBuilderFactory.newText(root).alignFillHorizontal().text(jvmArgumentsString).control();

        uiBuilderFactory.span(root);

        // fourth line: program arguments
        uiBuilderFactory.newLabel(root).alignLeft().text(ProjectImportMessages.Label_ProgramArguments);

        String arguments = getConfiguration().getArguments().getValue();
        String argumentsString = Strings.nullToEmpty(arguments);
        this.programArgumentsText = uiBuilderFactory.newText(root).alignFillHorizontal().text(argumentsString).control();

        uiBuilderFactory.span(root);
    }

    private void bindToConfiguration() {
        this.gradleUserHomeText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                getConfiguration().setGradleUserHome(FileUtils.getAbsoluteFile(AdvancedOptionsWizardPage.this.gradleUserHomeText.getText()).orNull());
            }
        });
        this.javaHomeText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                getConfiguration().setJavaHome(FileUtils.getAbsoluteFile(AdvancedOptionsWizardPage.this.javaHomeText.getText()).orNull());
            }
        });
        this.jvmArgumentsText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                getConfiguration().setJvmArguments(Strings.emptyToNull(AdvancedOptionsWizardPage.this.jvmArgumentsText.getText()));
            }
        });
        this.programArgumentsText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                getConfiguration().setArguments(Strings.emptyToNull(AdvancedOptionsWizardPage.this.programArgumentsText.getText()));
            }
        });
    }

}
