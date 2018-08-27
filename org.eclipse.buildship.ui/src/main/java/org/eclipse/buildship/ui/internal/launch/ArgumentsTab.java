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

package org.eclipse.buildship.ui.internal.launch;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import org.eclipse.buildship.core.internal.i18n.CoreMessages;
import org.eclipse.buildship.core.internal.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.core.internal.util.collections.CollectionsUtils;
import org.eclipse.buildship.ui.internal.PluginImage.ImageState;
import org.eclipse.buildship.ui.internal.PluginImages;

/**
 * Specifies the JVM arguments and program arguments to apply when executing tasks via the run
 * configurations.
 */
public final class ArgumentsTab extends AbstractLaunchConfigurationTab {

    private Text argumentsText;
    private Text jvmArgumentsText;

    @Override
    public String getName() {
        return LaunchMessages.Tab_Name_Arguments;
    }

    @Override
    public Image getImage() {
        return PluginImages.RUN_CONFIG_ARGUMENTS.withState(ImageState.ENABLED).getImage();
    }

    @Override
    public void createControl(Composite root) {
        Composite parent = new Composite(root, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        parent.setLayout(layout);
        setControl(parent);

        Group argumentsGroup = createGroup(parent, CoreMessages.RunConfiguration_Label_Arguments + ":"); //$NON-NLS-1$
        createArgumentsSelectionControl(argumentsGroup);

        Group jvmArgumentsGroup = createGroup(parent, CoreMessages.RunConfiguration_Label_JvmArguments + ":"); //$NON-NLS-1$
        createJvmArgumentsSelectionControl(jvmArgumentsGroup);
    }

    private Group createGroup(Composite parent, String groupName) {
        Group group = new Group(parent, SWT.NONE);
        group.setText(groupName);
        group.setLayout(new GridLayout());
        group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        return group;
    }

    private void createArgumentsSelectionControl(Composite container) {
        this.argumentsText = createTextControl(container);
        createVariablesSelectorButton(container, this.argumentsText);
    }

    private void createJvmArgumentsSelectionControl(Composite container) {
        this.jvmArgumentsText = createTextControl(container);
        createVariablesSelectorButton(container, this.jvmArgumentsText);
    }

    private Text createTextControl(Composite container) {
        Text textControl = new Text(container, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        GridData textLayoutData = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
        textLayoutData.heightHint = 65;
        textControl.setLayoutData(textLayoutData);
        textControl.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        return textControl;
    }

    private void createVariablesSelectorButton(Composite container, final Text target) {
        Composite buttonContainer = new Composite(container, SWT.NONE);
        GridLayout buttonContainerLayout = new GridLayout(1, false);
        buttonContainerLayout.marginHeight = 1;
        buttonContainerLayout.marginWidth = 0;
        buttonContainer.setLayout(buttonContainerLayout);
        buttonContainer.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

        Button selectVariableButton = new Button(buttonContainer, SWT.NONE);
        selectVariableButton.setText(LaunchMessages.Button_Label_SelectVariables);
        selectVariableButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
                dialog.open();
                String variable = dialog.getVariableExpression();
                if (variable != null) {
                    target.insert(variable);
                }
            }
        });
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        GradleRunConfigurationAttributes configurationAttributes = GradleRunConfigurationAttributes.from(configuration);
        this.argumentsText.setText(CollectionsUtils.joinWithSpace(configurationAttributes.getArgumentExpressions()));
        this.jvmArgumentsText.setText(CollectionsUtils.joinWithSpace(configurationAttributes.getJvmArgumentExpressions()));
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        GradleRunConfigurationAttributes.applyArgumentExpressions(CollectionsUtils.splitBySpace(this.argumentsText.getText()), configuration);
        GradleRunConfigurationAttributes.applyJvmArgumentExpressions(CollectionsUtils.splitBySpace(this.jvmArgumentsText.getText()), configuration);
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        // leave the controls empty
    }

}
