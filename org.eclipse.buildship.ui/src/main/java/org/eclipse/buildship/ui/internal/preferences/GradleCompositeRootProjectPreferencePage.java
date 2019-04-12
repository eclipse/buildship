/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.internal.preferences;

import org.eclipse.buildship.ui.internal.util.layout.LayoutUtils;
import org.eclipse.buildship.ui.internal.wizard.workspacecomposite.WorkspaceCompositeWizardMessages;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Preference page for composite root project.
 *
 * @author Sebastian Kuzniarz
 */

public final class GradleCompositeRootProjectPreferencePage extends PropertyPage {

    public static final String PAGE_ID = "org.eclipse.buildship.ui.compositeRootProjectProperties";

    private Text workspaceCompositeRootProjectLabel;
    private Text overrideCheckboxLabel;
    private Button overrideSettingsCheckbox;
    private Button selectRootProject;
    private Composite rootProjectSettingsComposite;
    private Label rootProjectLabel;
    
    private Layout createLayout() {
        GridLayout layout = LayoutUtils.newGridLayout(2);
        layout.horizontalSpacing = 4;
        layout.verticalSpacing = 4;
        return layout;
    }
    
    @Override
    protected Control createContents(Composite parent) {
    	
    	this.rootProjectSettingsComposite = new Composite(parent, SWT.NONE);
    	rootProjectSettingsComposite.setLayout(createLayout());

        this.overrideSettingsCheckbox = new Button(rootProjectSettingsComposite, SWT.CHECK);
        this.overrideSettingsCheckbox.setText("Use project as composite root");
        GridDataFactory.swtDefaults().applyTo(rootProjectSettingsComposite);
        
        Label line = new Label(rootProjectSettingsComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1).applyTo(line);
        
        // composite root container
        Composite workspaceCompositeNameComposite = new Composite(rootProjectSettingsComposite, SWT.NONE);
        GridLayoutFactory.swtDefaults().extendedMargins(0, 0, 0, 10).numColumns(3).applyTo(workspaceCompositeNameComposite);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).span(3, SWT.DEFAULT).applyTo(workspaceCompositeNameComposite);

        // root project label
        this.rootProjectLabel = new Label(workspaceCompositeNameComposite, SWT.NONE);
        this.rootProjectLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        this.rootProjectLabel.setText(WorkspaceCompositeWizardMessages.Label_RootProject);

        // root project text field
        this.workspaceCompositeRootProjectLabel = new Text(workspaceCompositeNameComposite, SWT.BORDER);
        this.workspaceCompositeRootProjectLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        // root project select button
        this.selectRootProject = new Button(workspaceCompositeNameComposite, SWT.PUSH);
        this.selectRootProject.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        this.selectRootProject.setText(WorkspaceCompositeWizardMessages.Button_Select_RootProject);
        return rootProjectSettingsComposite;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public boolean performOk() {
       return true;
    }
}
