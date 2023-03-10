/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.launch;

import java.io.File;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.osgi.util.NLS;
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
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.GradleProjectNature;
import org.eclipse.buildship.core.internal.i18n.CoreMessages;
import org.eclipse.buildship.core.internal.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.core.internal.util.binding.Validator;
import org.eclipse.buildship.core.internal.util.binding.Validators;
import org.eclipse.buildship.core.internal.util.file.FileUtils;
import org.eclipse.buildship.core.internal.util.variable.ExpressionUtils;
import org.eclipse.buildship.ui.internal.PluginImage.ImageState;
import org.eclipse.buildship.ui.internal.PluginImages;
import org.eclipse.buildship.ui.internal.util.file.DirectoryDialogSelectionListener;
import org.eclipse.buildship.ui.internal.util.widget.StringListEditor;
import org.eclipse.buildship.ui.internal.util.widget.StringListEditor.StringListChangeListener;

/**
 * Specifies a root project and a list of tasks to execute via the run configurations.
 */
public final class ProjectTab extends AbstractLaunchConfigurationTab {

    private final Validator<File> workingDirValidator;

    private StringListEditor tasksList;
    private Text workingDirectoryText;

    public ProjectTab() {
        this.workingDirValidator = Validators.requiredDirectoryValidator(CoreMessages.RunConfiguration_Label_WorkingDirectory);
    }

    @Override
    public String getName() {
        return LaunchMessages.Tab_Name_GradleTasks;
    }

    @Override
    public Image getImage() {
        return PluginImages.RUN_CONFIG_TASKS.withState(ImageState.ENABLED).getImage();
    }

    @Override
    public void createControl(Composite root) {
        Composite parent = new Composite(root, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        parent.setLayout(layout);
        setControl(parent);

        Group tasksGroup = createGroup(parent, CoreMessages.RunConfiguration_Label_GradleTasks + ":"); //$NON-NLS-1$
        createTasksSelectionControl(tasksGroup);

        Group workingDirectoryGroup = createGroup(parent, CoreMessages.RunConfiguration_Label_WorkingDirectory + ":"); //$NON-NLS-1$
        createWorkingDirectorySelectionControl(workingDirectoryGroup);
    }

    private Group createGroup(Composite parent, String groupName) {
        Group group = new Group(parent, SWT.NONE);
        group.setText(groupName);
        group.setLayout(new GridLayout());
        group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        return group;
    }

    private void createTasksSelectionControl(Composite container) {
        this.tasksList = new StringListEditor(container, false, "task");
        GridData tasksTextLayoutData = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
        tasksTextLayoutData.heightHint = 50;
        this.tasksList.setLayoutData(tasksTextLayoutData);
        this.tasksList.addChangeListener(new DialogUpdater());
    }

    private void createWorkingDirectorySelectionControl(Composite container) {
        this.workingDirectoryText = new Text(container, SWT.SINGLE | SWT.BORDER);
        this.workingDirectoryText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        this.workingDirectoryText.addModifyListener(new DialogUpdater());

        Composite buttonContainer = new Composite(container, SWT.NONE);
        GridLayout buttonContainerLayout = new GridLayout(3, false);
        buttonContainerLayout.marginHeight = 1;
        buttonContainerLayout.marginWidth = 0;
        buttonContainer.setLayout(buttonContainerLayout);
        buttonContainer.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

        Button browseWorkspaceButton = new Button(buttonContainer, SWT.NONE);
        browseWorkspaceButton.setText(LaunchMessages.Button_Label_BrowseWorkspace);
        browseWorkspaceButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ListDialog projectDialog = new ListDialog(getShell());
                projectDialog.setTitle(LaunchMessages.Title_BrowseWorkspaceDialog);
                projectDialog.setContentProvider(new ArrayContentProvider() {

                    @Override
                    public Object[] getElements(Object input) {
                        return findAllGradleProjects();
                    }

                });
                projectDialog.setLabelProvider(new WorkbenchLabelProvider());
                projectDialog.setInput(ResourcesPlugin.getWorkspace().getRoot());

                // open the dialog and put the path of the selected project into the working directory text field
                projectDialog.open();
                Object[] result = projectDialog.getResult();
                if (result != null) {
                    if (result.length > 0) {
                        String locationInExpression = ExpressionUtils.encodeWorkspaceLocation((IProject) result[0]);
                        ProjectTab.this.workingDirectoryText.setText(locationInExpression);
                    } else {
                        ProjectTab.this.workingDirectoryText.setText(""); //$NON-NLS-1$
                    }
                }
            }
        });

        Button browseFilesystemButton = new Button(buttonContainer, SWT.NONE);
        browseFilesystemButton.setText(LaunchMessages.Button_Label_BrowseFilesystem);
        browseFilesystemButton.addSelectionListener(new DirectoryDialogSelectionListener(getShell(), this.workingDirectoryText, LaunchMessages.Title_BrowseFileSystemDialog));

        Button selectVariableButton = new Button(buttonContainer, SWT.NONE);
        selectVariableButton.setText(LaunchMessages.Button_Label_SelectVariables);
        selectVariableButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
                dialog.open();
                String variable = dialog.getVariableExpression();
                if (variable != null) {
                    ProjectTab.this.workingDirectoryText.insert(variable);
                }
            }
        });
    }

    private IProject[] findAllGradleProjects() {
        return FluentIterable.from(CorePlugin.workspaceOperations().getAllProjects()).filter(new Predicate<IProject>() {

            @Override
            public boolean apply(IProject project) {
                return GradleProjectNature.isPresentOn(project);
            }
        }).toArray(IProject.class);
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        GradleRunConfigurationAttributes configurationAttributes = GradleRunConfigurationAttributes.from(configuration);
        this.tasksList.setEntries(configurationAttributes.getTasks());
        this.workingDirectoryText.setText(Strings.nullToEmpty(configurationAttributes.getWorkingDirExpression()));
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        GradleRunConfigurationAttributes.applyTasks(this.tasksList.getEntries(), configuration);
        GradleRunConfigurationAttributes.applyWorkingDirExpression(this.workingDirectoryText.getText(), configuration);
    }

    @SuppressWarnings("Contract")
    @Override
    public boolean isValid(ILaunchConfiguration configuration) {
        String workingDirectoryExpression = this.workingDirectoryText.getText();

        String workingDirectoryResolved;
        try {
            workingDirectoryResolved = ExpressionUtils.decode(workingDirectoryExpression);
        } catch (CoreException e) {
            setErrorMessage(NLS.bind(LaunchMessages.ErrorMessage_CannotResolveExpression_0, workingDirectoryExpression));
            return false;
        }

        File workingDir = FileUtils.getAbsoluteFile(workingDirectoryResolved).orNull();
        Optional<String> error = this.workingDirValidator.validate(workingDir);
        setErrorMessage(error.orNull());
        return !error.isPresent();
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        // leave the controls empty
    }

    /**
     * Listener implementation to update the dialog buttons and messages.
     */
    private class DialogUpdater extends SelectionAdapter implements ModifyListener, StringListChangeListener {
        @Override
        public void widgetSelected(SelectionEvent e) {
            updateLaunchConfigurationDialog();
        }

        @Override
        public void modifyText(ModifyEvent e) {
            updateLaunchConfigurationDialog();
        }

        @Override
        public void onChange() {
            updateLaunchConfigurationDialog();
        }
    }
}
