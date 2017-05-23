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

import com.gradleware.tooling.toolingclient.GradleDistribution;
import com.gradleware.tooling.toolingutils.binding.Validator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.WorkspaceConfiguration;
import org.eclipse.buildship.core.i18n.CoreMessages;
import org.eclipse.buildship.core.util.binding.Validators;
import org.eclipse.buildship.core.util.file.FileUtils;
import org.eclipse.buildship.core.util.gradle.GradleDistributionValidator;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper;
import org.eclipse.buildship.core.util.variable.ExpressionUtils;
import org.eclipse.buildship.ui.launch.LaunchMessages;
import org.eclipse.buildship.ui.util.font.FontUtils;
import org.eclipse.buildship.ui.util.widget.GradleDistributionGroup.DistributionChangedListener;
import org.eclipse.buildship.ui.util.widget.GradleProjectSettingsComposite;

/**
 * The main workspace preference page for Buildship. Currently only used to configure the Gradle
 * User Home.
 */
public final class GradleWorkbenchPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    public static final String PAGE_ID = "org.eclipse.buildship.ui.preferences";

    private final Font defaultFont;
    private final Validator<File> gradleUserHomeValidator;
    private final Validator<GradleDistributionWrapper> gradleDistributionValidator;

    private GradleProjectSettingsComposite gradleProjectSettingsComposite;

    public GradleWorkbenchPreferencePage() {
        this.defaultFont = FontUtils.getDefaultDialogFont();
        this.gradleUserHomeValidator = Validators.optionalDirectoryValidator(CoreMessages.Preference_Label_GradleUserHome);
        this.gradleDistributionValidator = GradleDistributionValidator.gradleDistributionValidator();
    }

    @Override
    protected Control createContents(Composite parent) {
        this.gradleProjectSettingsComposite = GradleProjectSettingsComposite.withoutOverrideCheckbox(parent);

        initValues();
        addListeners();

        return this.gradleProjectSettingsComposite;
    }

    private void initValues() {
        WorkspaceConfiguration config = CorePlugin.configurationManager().loadWorkspaceConfiguration();
        GradleDistributionWrapper distributionWrapper = GradleDistributionWrapper.from(config.getGradleDisribution());
        File gradleUserHome = config.getGradleUserHome();
        String gradleUserHomePath = gradleUserHome == null ? "" : gradleUserHome.getPath();

        this.gradleProjectSettingsComposite.getGradleDistributionGroup().setGradleDistribution(distributionWrapper);
        this.gradleProjectSettingsComposite.getGradleUserHomeGroup().getGradleUserHomeText().setText(gradleUserHomePath);
        this.gradleProjectSettingsComposite.getOfflineModeCheckbox().setSelection(config.isOffline());
        this.gradleProjectSettingsComposite.getBuildScansCheckbox().setSelection(config.isBuildScansEnabled());
    }

    private void addListeners() {
        this.gradleProjectSettingsComposite.getGradleUserHomeGroup().getGradleUserHomeText().addModifyListener(new GradleUserHomeValidatingListener());
        this.gradleProjectSettingsComposite.getGradleDistributionGroup().addDistributionChangedListener(new GradleDistribuionValidatingListener());
    }

    @Override
    public boolean performOk() {
        GradleDistribution distribution = this.gradleProjectSettingsComposite.getGradleDistributionGroup().getGradleDistribution().toGradleDistribution();
        String gradleUserHomeString = this.gradleProjectSettingsComposite.getGradleUserHomeGroup().getGradleUserHomeText().getText();
        File gradleUserHome = gradleUserHomeString.isEmpty() ? null : new File(gradleUserHomeString);
        boolean offlineMode = this.gradleProjectSettingsComposite.getOfflineModeCheckbox().getSelection();
        boolean buildScansEnabled = this.gradleProjectSettingsComposite.getBuildScansCheckbox().getSelection();
        WorkspaceConfiguration workspaceConfig = new WorkspaceConfiguration(distribution, gradleUserHome, offlineMode, buildScansEnabled);
        CorePlugin.configurationManager().saveWorkspaceConfiguration(workspaceConfig);
        return super.performOk();
    }

    @Override
    protected void performDefaults() {
        this.gradleProjectSettingsComposite.getGradleUserHomeGroup().getGradleUserHomeText().setText("");
        this.gradleProjectSettingsComposite.getGradleDistributionGroup().setGradleDistribution(GradleDistributionWrapper.from(GradleDistribution.fromBuild()));
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

    /**
     * Validates the Gradle distribution.
     */
    private class GradleDistribuionValidatingListener implements DistributionChangedListener {

        @Override
        public void distributionUpdated(GradleDistributionWrapper distribution) {
            Optional<String> error = GradleWorkbenchPreferencePage.this.gradleDistributionValidator.validate(distribution);
            setValid(!error.isPresent());
            setErrorMessage(error.orNull());
        }
    }

    /**
     * Validates the Gradle user home value.
     */
    private class GradleUserHomeValidatingListener implements ModifyListener {

        @Override
        public void modifyText(ModifyEvent e) {
            String resolvedGradleUserHome = getResolvedGradleUserHome();
            File gradleUserHome = FileUtils.getAbsoluteFile(resolvedGradleUserHome).orNull();
            Optional<String> error = GradleWorkbenchPreferencePage.this.gradleUserHomeValidator.validate(gradleUserHome);
            setValid(!error.isPresent());
            setErrorMessage(error.orNull());
        }

        private String getResolvedGradleUserHome() {
            String gradleUserHomeExpression = Strings.emptyToNull(GradleWorkbenchPreferencePage.this.gradleProjectSettingsComposite.getGradleUserHomeGroup().getGradleUserHomeText().getText());

            String gradleUserHomeResolved = null;
            try {
                gradleUserHomeResolved = ExpressionUtils.decode(gradleUserHomeExpression);
            } catch (CoreException e) {
                setErrorMessage(NLS.bind(LaunchMessages.ErrorMessage_CannotResolveExpression_0, gradleUserHomeExpression));
                setValid(false);
            }
            return gradleUserHomeResolved;
        }
    }

}
