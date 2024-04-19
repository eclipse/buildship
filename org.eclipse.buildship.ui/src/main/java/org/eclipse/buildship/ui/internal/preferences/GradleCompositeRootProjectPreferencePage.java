/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.buildship.ui.internal.preferences;

import java.io.File;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.PropertyPage;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.CompositeConfiguration;
import org.eclipse.buildship.core.internal.configuration.ConfigurationManager;
import org.eclipse.buildship.core.internal.configuration.DefaultCompositeConfiguration;
import org.eclipse.buildship.core.internal.util.binding.Validator;
import org.eclipse.buildship.core.internal.util.binding.Validators;
import org.eclipse.buildship.ui.internal.util.file.DirectoryDialogSelectionListener;
import org.eclipse.buildship.ui.internal.util.layout.LayoutUtils;
import org.eclipse.buildship.ui.internal.wizard.workspacecomposite.WorkspaceCompositeWizardMessages;

/**
 * Preference page for composite root project.
 *
 * @author Sebastian Kuzniarz
 */

public final class GradleCompositeRootProjectPreferencePage extends PropertyPage implements IWorkbenchPropertyPage {

    public static final String PAGE_ID = "org.eclipse.buildship.ui.compositeRootProjectProperties";

    private Text workspaceCompositeRootProjectLabel;
    private Button projectAsCompositeRootCheckbox;
    private Button selectRootProject;
    private Composite rootProjectSettingsComposite;
    private Label rootProjectLabel;
    private final Validator<File> rootProjectValidator;
    CompositeConfiguration compositeConfig;

    private Layout createLayout() {
        GridLayout layout = LayoutUtils.newGridLayout(2);
        layout.horizontalSpacing = 4;
        layout.verticalSpacing = 4;
        return layout;
    }

    public GradleCompositeRootProjectPreferencePage() {
        this.rootProjectValidator = Validators.optionalDirectoryValidator("Root project");
    }

    @Override
    protected Control createContents(Composite parent) {
        this.rootProjectSettingsComposite = buildRootProjectSettingsComposite(parent);
        addListeners();
        initValues();
        return this.rootProjectSettingsComposite;
    }

    private Composite buildRootProjectSettingsComposite(Composite parent) {
        Composite rootProjectComposite = new Composite(parent, SWT.WRAP);
        rootProjectComposite.setLayout(createLayout());

        this.projectAsCompositeRootCheckbox = new Button(rootProjectComposite, SWT.CHECK);
        this.projectAsCompositeRootCheckbox.setText("Use project as composite root");
        GridDataFactory.swtDefaults().applyTo(rootProjectComposite);

        Label line = new Label(rootProjectComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1).applyTo(line);

        // composite root container
        Composite workspaceCompositeNameComposite = new Composite(rootProjectComposite, SWT.NONE);
        GridLayoutFactory.swtDefaults().extendedMargins(0, 0, 0, 10).numColumns(3).applyTo(workspaceCompositeNameComposite);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).span(3, SWT.DEFAULT).applyTo(workspaceCompositeNameComposite);

        // root project label
        this.rootProjectLabel = new Label(workspaceCompositeNameComposite, SWT.NONE);
        this.rootProjectLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        this.rootProjectLabel.setText(WorkspaceCompositeWizardMessages.Label_RootProject);
        this.rootProjectLabel.setEnabled(false);

        // root project text field
        this.workspaceCompositeRootProjectLabel = new Text(workspaceCompositeNameComposite, SWT.BORDER);
        this.workspaceCompositeRootProjectLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        this.workspaceCompositeRootProjectLabel.setEnabled(false);

        // root project select button
        this.selectRootProject = new Button(workspaceCompositeNameComposite, SWT.PUSH);
        this.selectRootProject.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        this.selectRootProject.setText(WorkspaceCompositeWizardMessages.Button_Select_RootProject);
        this.selectRootProject.setEnabled(false);
        return rootProjectComposite;
    }

    private void initValues() {
        IWorkingSet composite = getTargetComposite();

        this.compositeConfig = CorePlugin.configurationManager().loadCompositeConfiguration(composite);
        boolean useProjectAsCompositeRoot = this.compositeConfig.projectAsCompositeRoot();

        this.projectAsCompositeRootCheckbox.setSelection(useProjectAsCompositeRoot);
        this.workspaceCompositeRootProjectLabel.setText(this.compositeConfig.getRootProject().toString());
        updateEnablement();
    }

    private void addListeners() {
        if (this.projectAsCompositeRootCheckbox != null) {
            this.projectAsCompositeRootCheckbox.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    updateEnablement();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    updateEnablement();
                }
            });

            File rootProjectDir = this.workspaceCompositeRootProjectLabel.getText().isEmpty() ? null: new File(this.workspaceCompositeRootProjectLabel.getText());
            this.workspaceCompositeRootProjectLabel.addModifyListener(new ValidatingListener<>(this, () -> rootProjectDir, this.rootProjectValidator));

            this.selectRootProject.addSelectionListener(new DirectoryDialogSelectionListener(this.getShell(), this.workspaceCompositeRootProjectLabel, "Root project"));
        }
    }

    public void updateEnablement() {
        if (this.projectAsCompositeRootCheckbox != null) {
            boolean enabled = this.projectAsCompositeRootCheckbox.getSelection();
            this.rootProjectLabel.setEnabled(enabled);
            this.selectRootProject.setEnabled(enabled);
            this.workspaceCompositeRootProjectLabel.setEnabled(enabled);
        }
    }

    @SuppressWarnings("cast")
    private IWorkingSet getTargetComposite() {
        return (IWorkingSet) Platform.getAdapterManager().getAdapter(getElement(), IWorkingSet.class);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public boolean performOk() {
        IWorkingSet composite = getTargetComposite();
        ConfigurationManager manager = CorePlugin.configurationManager();
        CompositeConfiguration currentConfig = manager.loadCompositeConfiguration(composite);

        CompositeConfiguration compConf = new DefaultCompositeConfiguration(currentConfig.getCompositeDir(),
                                                                            composite.getElements(),
                                                                            currentConfig.getBuildConfiguration(),
                                                                            this.projectAsCompositeRootCheckbox.getSelection(),
                                                                            new File(this.workspaceCompositeRootProjectLabel.getText()));
        manager.saveCompositeConfiguration(compConf);
        return true;
    }
}
