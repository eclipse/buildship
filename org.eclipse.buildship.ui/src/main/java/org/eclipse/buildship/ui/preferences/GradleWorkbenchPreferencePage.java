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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.ConfigurationManager;
import org.eclipse.buildship.core.configuration.WorkspaceConfiguration;
import org.eclipse.buildship.core.i18n.CoreMessages;
import org.eclipse.buildship.core.util.binding.Validators;
import org.eclipse.buildship.core.util.file.FileUtils;
import org.eclipse.buildship.core.util.variable.ExpressionUtils;
import org.eclipse.buildship.ui.launch.LaunchMessages;
import org.eclipse.buildship.ui.util.font.FontUtils;
import org.eclipse.buildship.ui.util.widget.GradleUserHomeComposite;
import org.eclipse.buildship.ui.util.widget.HoverText;

/**
 * The main workspace preference page for Buildship. Currently only used to configure the Gradle
 * User Home.
 */
public class GradleWorkbenchPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private final Font defaultFont;
    private final Validator<File> gradleUserHomeValidator;

    private GradleUserHomeComposite gradleUserHomeComposite;
    private Button offlineModeCheckbox;
    private Button buildScansCheckbox;

    public GradleWorkbenchPreferencePage() {
        this.defaultFont = FontUtils.getDefaultDialogFont();
        this.gradleUserHomeValidator = Validators.optionalDirectoryValidator(CoreMessages.Preference_Label_GradleUserHome);
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite page = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        page.setLayout(layout);

        this.gradleUserHomeComposite = new GradleUserHomeComposite(page, SWT.NONE);

        createOfflineModeCheckbox(page);
        createBuildScansCheckbox(page);

        initFields();
        addListeners();

        return page;
    }

    private void createOfflineModeCheckbox(Composite parent) {
        this.offlineModeCheckbox = new Button(parent, SWT.CHECK);
        this.offlineModeCheckbox.setText(CoreMessages.Preference_Label_OfflineMode);
    }

    private void createBuildScansCheckbox(Composite parent) {
        this.buildScansCheckbox = new Button(parent, SWT.CHECK);
        this.buildScansCheckbox.setText(CoreMessages.Preference_Label_BuildScans);
        HoverText.createAndAttach(this.buildScansCheckbox, CoreMessages.Preference_Label_BuildScansHover);
    }

    private void validate() {
        String resolvedGradleUserHome = getResolvedGradleUserHome();
        File gradleUserHome = FileUtils.getAbsoluteFile(resolvedGradleUserHome).orNull();
        Optional<String> error = this.gradleUserHomeValidator.validate(gradleUserHome);
        setValid(!error.isPresent());
        setErrorMessage(error.orNull());
    }

    private String getResolvedGradleUserHome() {
        String gradleUserHomeExpression = Strings.emptyToNull(this.gradleUserHomeComposite.getGradleUserHomeText().getText());

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
        WorkspaceConfiguration config = CorePlugin.configurationManager().loadWorkspaceConfiguration();
        File gradleUserHome = config.getGradleUserHome();
        this.gradleUserHomeComposite.getGradleUserHomeText().setText(gradleUserHome == null ? "" : gradleUserHome.getPath());
        this.offlineModeCheckbox.setSelection(config.isOffline());
        this.buildScansCheckbox.setSelection(config.isBuildScansEnabled());
    }

    private void addListeners() {
        this.gradleUserHomeComposite.getGradleUserHomeText().addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent event) {
                validate();
            }
        });
    }

    @Override
    public boolean performOk() {
        String gradleUserHome = this.gradleUserHomeComposite.getGradleUserHomeText().getText();
        ConfigurationManager manager = CorePlugin.configurationManager();
        WorkspaceConfiguration workspaceConfig = new WorkspaceConfiguration(gradleUserHome.isEmpty() ? null : new File(gradleUserHome), this.offlineModeCheckbox.getSelection(),
                this.buildScansCheckbox.getSelection());
        manager.saveWorkspaceConfiguration(workspaceConfig);
        return super.performOk();
    }

    @Override
    protected void performDefaults() {
        this.gradleUserHomeComposite.getGradleUserHomeText().setText("");
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
