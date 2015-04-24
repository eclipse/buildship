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

package org.eclipse.buildship.ui.launch;

import java.io.File;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import com.gradleware.tooling.toolingutils.binding.Validator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.core.gradle.GradleConnectionValidators;
import org.eclipse.buildship.core.i18n.CoreMessages;
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.core.util.file.FileUtils;
import org.eclipse.buildship.core.util.variable.ExpressionUtils;
import org.eclipse.buildship.ui.util.file.DirectoryDialogSelectionListener;
import org.eclipse.buildship.ui.util.font.FontUtils;
import org.eclipse.buildship.ui.util.widget.UiBuilder;

/**
 * Specifies the Gradle user home to apply when executing tasks via the run configurations.
 */
public final class GradleUserHomeTab extends AbstractLaunchConfigurationTab {

    private final Font defaultFont;
    private final UiBuilder.UiBuilderFactory builderFactory;
    private final Validator<File> gradleUserHomeValidator;

    private Text gradleUserHomeText;

    public GradleUserHomeTab() {
        this.defaultFont = FontUtils.getDefaultDialogFont();
        this.builderFactory = new UiBuilder.UiBuilderFactory(this.defaultFont);
        this.gradleUserHomeValidator = GradleConnectionValidators.optionalDirectoryValidator(CoreMessages.RunConfiguration_Label_GradleUserHome);
    }

    @Override
    public String getName() {
        return LaunchMessages.Tab_Name_GradleUserHome;
    }

    @Override
    public Image getImage() {
        return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER).createImage();
    }

    @Override
    public void createControl(Composite parent) {
        Composite page = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        page.setLayout(layout);
        setControl(page);

        Group gradleUserHomeGroup = createGroup(page, CoreMessages.RunConfiguration_Label_GradleUserHome + ":");
        createGradleUserHomeSelectionControl(gradleUserHomeGroup);
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
                updateLaunchConfigurationDialog();
            }
        });

        Button gradleUserHomeBrowseButton = this.builderFactory.newButton(root).alignLeft().text(LaunchMessages.Button_Label_Browse).control();
        gradleUserHomeBrowseButton.addSelectionListener(new DirectoryDialogSelectionListener(root.getShell(), this.gradleUserHomeText,
                CoreMessages.RunConfiguration_Label_GradleUserHome));
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        GradleRunConfigurationAttributes configurationAttributes = GradleRunConfigurationAttributes.from(configuration);
        this.gradleUserHomeText.setText(Strings.nullToEmpty(configurationAttributes.getGradleUserHomeExpression()));
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        GradleRunConfigurationAttributes.applyGradleUserHomeExpression(Strings.emptyToNull(this.gradleUserHomeText.getText()), configuration);
    }

    @SuppressWarnings("Contract")
    @Override
    public boolean isValid(ILaunchConfiguration launchConfig) {
        String gradleUserHomeExpression = Strings.emptyToNull(this.gradleUserHomeText.getText());

        String gradleUserHomeResolved;
        try {
            gradleUserHomeResolved = ExpressionUtils.decode(gradleUserHomeExpression);
        } catch (CoreException e) {
            setErrorMessage(NLS.bind(LaunchMessages.ErrorMessage_CannotResolveExpression_0, gradleUserHomeExpression));
            return false;
        }

        File gradleUserHome = FileUtils.getAbsoluteFile(gradleUserHomeResolved).orNull();
        Optional<String> error = this.gradleUserHomeValidator.validate(gradleUserHome);
        setErrorMessage(error.orNull());
        return !error.isPresent();
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        // leave the controls empty
    }

    @Override
    public void dispose() {
        this.defaultFont.dispose();
        super.dispose();
    }

}
