/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.internal.preferences;

import org.eclipse.buildship.ui.internal.util.font.FontUtils;
import org.eclipse.buildship.ui.internal.util.layout.LayoutUtils;
import org.eclipse.buildship.ui.internal.util.widget.UiBuilder;
import org.eclipse.buildship.ui.internal.wizard.workspacecomposite.WorkspaceCompositeWizardMessages;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Preference page for Gradle projects.
 *
 * @author Sebastian Kuzniarz
 */

public final class GradleCompositeRootProjectPreferencePage extends PropertyPage {

    public static final String PAGE_ID = "org.eclipse.buildship.ui.compositeRootProjectProperties";

    private final Font defaultFont;
    private final UiBuilder.UiBuilderFactory builderFactory;
	private Text workspaceCompositeRootProjectLabel;
    private Text overrideCheckboxLabel;
    private Button overrideSettingsCheckbox;
    private Composite rootProjectSettingsComposite;
    
    public GradleCompositeRootProjectPreferencePage() {
    	this.defaultFont = FontUtils.getDefaultDialogFont();
        this.builderFactory = new UiBuilder.UiBuilderFactory(this.defaultFont);
    }
    
    private Layout createLayout() {
        GridLayout layout = LayoutUtils.newGridLayout(2);
        layout.horizontalSpacing = 4;
        layout.verticalSpacing = 4;
        return layout;
    }
    
    @Override
    protected Control createContents(Composite parent) {
    	
    	this.rootProjectSettingsComposite = builderFactory.newComposite(parent).control();
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
        builderFactory.newLabel(workspaceCompositeNameComposite).alignLeft().text(WorkspaceCompositeWizardMessages.Label_RootProject).control();

        // root project text field
        this.workspaceCompositeRootProjectLabel = builderFactory.newText(workspaceCompositeNameComposite).alignFillHorizontal().control();
        
        // root project select button
        builderFactory.newButton(workspaceCompositeNameComposite).alignRight().text(WorkspaceCompositeWizardMessages.Button_Select_RootProject).control();
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
