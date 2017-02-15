/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.preferences;

import java.io.File;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import com.gradleware.tooling.toolingutils.binding.Validator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.WorkspaceConfiguration;
import org.eclipse.buildship.core.i18n.CoreMessages;
import org.eclipse.buildship.core.util.binding.Validators;
import org.eclipse.buildship.core.util.file.FileUtils;
import org.eclipse.buildship.core.util.variable.ExpressionUtils;
import org.eclipse.buildship.ui.i18n.UiMessages;
import org.eclipse.buildship.ui.launch.LaunchMessages;
import org.eclipse.buildship.ui.util.file.DirectoryDialogSelectionListener;
import org.eclipse.buildship.ui.util.font.FontUtils;
import org.eclipse.buildship.ui.util.widget.UiBuilder;

/**
 * The main workspace preference page for Buildship. Currently only used to configure the Gradle
 * User Home.
 */
public class GradleWorkbenchPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private final Font defaultFont;
    private final UiBuilder.UiBuilderFactory builderFactory;
    private final Validator<File> gradleUserHomeValidator;

    private Text gradleUserHomeText;
    private Button offlineModeCheckbox;
    private Button buildScansCheckbox;

    public GradleWorkbenchPreferencePage() {
        this.defaultFont = FontUtils.getDefaultDialogFont();
        this.builderFactory = new UiBuilder.UiBuilderFactory(this.defaultFont);
        this.gradleUserHomeValidator = Validators.optionalDirectoryValidator(CoreMessages.Preference_Label_GradleUserHome);
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite page = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        page.setLayout(layout);

        Group gradleUserHomeGroup = createGroup(page, CoreMessages.Preference_Label_GradleUserHome + ":");
        createGradleUserHomeSelectionControl(gradleUserHomeGroup);
        createOfflineModeCheckbox(page);

        initFields();

        return page;
    }

    private void createOfflineModeCheckbox(Composite page) {
        this.offlineModeCheckbox = new Button(page, SWT.CHECK);
        this.offlineModeCheckbox.setText(CoreMessages.Preference_Label_OfflineMode);
        this.buildScansCheckbox = new Button(page, SWT.CHECK);
        this.buildScansCheckbox.setText(CoreMessages.Preference_Label_BuildScans);
    }

    private Group createGroup(Composite parent, String groupName) {
        Group group = new Group(parent, SWT.NONE);
        group.setText(groupName);
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        return group;
    }

    private void createGradleUserHomeSelectionControl(Composite root) {
        this.gradleUserHomeText = this.builderFactory.newText(root).alignFillHorizontal().control();
        this.gradleUserHomeText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent event) {
                validate();
            }
        });

        Button gradleUserHomeBrowseButton = this.builderFactory.newButton(root).alignLeft().text(UiMessages.Button_Label_Browse).control();
        DirectoryDialogSelectionListener directoryDialogListener = new DirectoryDialogSelectionListener(root.getShell(), this.gradleUserHomeText,
                CoreMessages.Preference_Label_GradleUserHome);
        gradleUserHomeBrowseButton.addSelectionListener(directoryDialogListener);
    }

    private void validate() {
        String resolvedGradleUserHome = getResolvedGradleUserHome();
        File gradleUserHome = FileUtils.getAbsoluteFile(resolvedGradleUserHome).orNull();
        Optional<String> error = this.gradleUserHomeValidator.validate(gradleUserHome);
        setValid(!error.isPresent());
        setErrorMessage(error.orNull());
    }

    private String getResolvedGradleUserHome() {
        String gradleUserHomeExpression = Strings.emptyToNull(this.gradleUserHomeText.getText());

        String gradleUserHomeResolved = null;
        try {
            gradleUserHomeResolved = ExpressionUtils.decode(gradleUserHomeExpression);
        } catch (CoreException e) {
            setErrorMessage(NLS.bind(LaunchMessages.ErrorMessage_CannotResolveExpression_0, gradleUserHomeExpression));
            setValid(false);
        }
        return gradleUserHomeResolved;
    }

    private void initFields() {
        WorkspaceConfiguration config = CorePlugin.workspaceConfigurationManager().loadWorkspaceConfiguration();
        File gradleUserHome = config.getGradleUserHome();
        this.gradleUserHomeText.setText(gradleUserHome == null ? "" : gradleUserHome.getPath());
        this.offlineModeCheckbox.setSelection(config.isOffline());
    }

    @Override
    public boolean performOk() {
        String gradleUserHome = this.gradleUserHomeText.getText();
        WorkspaceConfiguration config = new WorkspaceConfiguration(gradleUserHome.isEmpty() ? null : new File(gradleUserHome),
                                                                   this.offlineModeCheckbox.getSelection(),
                                                                   this.buildScansCheckbox.getSelection());
        CorePlugin.workspaceConfigurationManager().saveWorkspaceConfiguration(config);
        return super.performOk();
    }

    @Override
    protected void performDefaults() {
        this.gradleUserHomeText.setText("");
        super.performDefaults();
    }

    @Override
    public void dispose() {
        this.defaultFont.dispose();
        super.dispose();
    }

    @Override
    public void init(IWorkbench workbench) {
    }

}
