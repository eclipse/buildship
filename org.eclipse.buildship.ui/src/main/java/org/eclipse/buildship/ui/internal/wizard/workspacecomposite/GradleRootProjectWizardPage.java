/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.buildship.ui.internal.wizard.workspacecomposite;

import java.io.File;

import com.google.common.collect.ImmutableList;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;

import org.eclipse.buildship.core.internal.util.binding.Property;
import org.eclipse.buildship.ui.internal.util.file.DirectoryDialogSelectionListener;
import org.eclipse.buildship.ui.internal.util.layout.LayoutUtils;

/**
 * Page in the {@link WorkspaceCompositeCreationWizard} for setting a project as composite root.
 */
@SuppressWarnings("unused")
public final class GradleRootProjectWizardPage extends AbstractCompositeWizardPage {

    private final CompositeRootProjectConfiguration rootProjectConfiguration;

    private Text workspaceCompositeRootProjectText;
    private Text overrideRootProjectCheckboxLabel;
    private Button overrideRootProjectCheckbox;
    private Button selectRootProject;
    private Label rootProjectLabel;

    public GradleRootProjectWizardPage(CompositeConfiguration configuration, CompositeRootProjectConfiguration rootProjectConfiguration) {
        this(WorkspaceCompositeWizardMessages.Title_CompositeRootWizardPage,
             WorkspaceCompositeWizardMessages.InfoMessage_CompositeRootWizardPageDefault,
             WorkspaceCompositeWizardMessages.InfoMessage_CompositeRootWizardPageContext, configuration, rootProjectConfiguration);
    }

    public GradleRootProjectWizardPage(String title, String defaultMessage,
            String pageContextInformation, CompositeConfiguration configuration, CompositeRootProjectConfiguration rootProjectConfiguration) {
        super("CompositeRootProject", title, defaultMessage, configuration, ImmutableList.<Property<?>>of(rootProjectConfiguration.getUseCompositeRoot(), rootProjectConfiguration.getRootProject())); //$NON-NLS-1$

        this.rootProjectConfiguration = rootProjectConfiguration;
    }

    @Override
    protected void createWidgets(Composite root) {
        root.setLayout(createLayout());
        createContent(root);
        addListeners();
    }

    private void initValues() {
        this.overrideRootProjectCheckbox.setSelection(this.rootProjectConfiguration.getUseCompositeRoot().getValue());
        this.workspaceCompositeRootProjectText.setText(this.rootProjectConfiguration.getRootProject().getValue().getAbsolutePath());
    }

    private Layout createLayout() {
        GridLayout layout = LayoutUtils.newGridLayout(2);
        layout.horizontalSpacing = 4;
        layout.verticalSpacing = 4;
        return layout;
    }

    private void createContent(Composite root) {

        this.overrideRootProjectCheckbox = new Button(root, SWT.CHECK);
        this.overrideRootProjectCheckbox.setText("Use project as composite root");
        GridDataFactory.swtDefaults().applyTo(root);

        Label line = new Label(root, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1).applyTo(line);

        // composite root container
        Composite workspaceCompositeNameComposite = new Composite(root, SWT.NONE);
        GridLayoutFactory.swtDefaults().extendedMargins(0, 0, 0, 10).numColumns(3).applyTo(workspaceCompositeNameComposite);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).span(3, SWT.DEFAULT).applyTo(workspaceCompositeNameComposite);

        // root project label
        this.rootProjectLabel = new Label(workspaceCompositeNameComposite, SWT.NONE);
        this.rootProjectLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        this.rootProjectLabel.setText(WorkspaceCompositeWizardMessages.Label_RootProject);

        this.rootProjectLabel.setEnabled(false);

        // root project text field
        this.workspaceCompositeRootProjectText = new Text(workspaceCompositeNameComposite, SWT.BORDER);
        this.workspaceCompositeRootProjectText.setEnabled(false);
        this.workspaceCompositeRootProjectText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        // root project select button
        this.selectRootProject = new Button(workspaceCompositeNameComposite, SWT.PUSH);
        this.selectRootProject.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        this.selectRootProject.setEnabled(false);
        this.selectRootProject.setText(WorkspaceCompositeWizardMessages.Button_Select_RootProject);

    }

    private File getRootProject() {
        String rootProjectString = this.workspaceCompositeRootProjectText.getText();
        return rootProjectString.isEmpty() ? null : new File(rootProjectString);
    }

    private void addListeners() {
        if (this.overrideRootProjectCheckbox != null) {
            this.overrideRootProjectCheckbox.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    getConfiguration().getProjectAsCompositeRoot().setValue(GradleRootProjectWizardPage.this.overrideRootProjectCheckbox.getSelection());
                    updateEnablement();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    getConfiguration().getProjectAsCompositeRoot().setValue(GradleRootProjectWizardPage.this.overrideRootProjectCheckbox.getSelection());
                    updateEnablement();
                }
            });

            this.workspaceCompositeRootProjectText.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    getConfiguration().getRootProject().setValue(GradleRootProjectWizardPage.this.getRootProject());

                }
            });

            this.selectRootProject.addSelectionListener(new DirectoryDialogSelectionListener(this.getShell(), this.workspaceCompositeRootProjectText, "Root project"));
        }
    }

    public void updateEnablement() {
        if (this.overrideRootProjectCheckbox != null) {
            boolean enabled = this.overrideRootProjectCheckbox.getSelection();
            this.rootProjectLabel.setEnabled(enabled);
            this.workspaceCompositeRootProjectText.setEnabled(enabled);
            this.selectRootProject.setEnabled(enabled);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    protected String getPageContextInformation() {
        // TODO Auto-generated method stub
        return null;
    }
}
