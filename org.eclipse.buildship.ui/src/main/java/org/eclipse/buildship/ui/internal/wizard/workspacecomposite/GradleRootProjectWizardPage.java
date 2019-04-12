/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 *     Sebastian Kuzniarz (Diebold Nixdorf Inc.) - adaptation and customization for workspace composite wizard 
 */

package org.eclipse.buildship.ui.internal.wizard.workspacecomposite;

import org.eclipse.buildship.ui.internal.util.layout.LayoutUtils;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;

/**
 * Page in the {@link WorkspaceCompositeCreationWizard} for setting a project as composite root 
 */
@SuppressWarnings("unused")
public final class GradleRootProjectWizardPage extends AbstractWizardPage {

    private Text workspaceCompositeRootProjectLabel;
    private Text overrideCheckboxLabel;
    private Button overrideSettingsCheckbox;
    private Button selectRootProject;
    private Label rootProjectLabel;

    public GradleRootProjectWizardPage() {
        this(WorkspaceCompositeWizardMessages.Title_CompositeRootWizardPage,
             WorkspaceCompositeWizardMessages.InfoMessage_CompositeRootWizardPageDefault,
             WorkspaceCompositeWizardMessages.InfoMessage_CompositeRootWizardPageContext);
    }

    public GradleRootProjectWizardPage(String title, String defaultMessage,
            String pageContextInformation) {
        super("CompositeRootProject", title, defaultMessage); //$NON-NLS-1$
    }

    @Override
    protected void createWidgets(Composite root) {
        root.setLayout(createLayout());
        createContent(root);
    }

    private Layout createLayout() {
        GridLayout layout = LayoutUtils.newGridLayout(2);
        layout.horizontalSpacing = 4;
        layout.verticalSpacing = 4;
        return layout;
    }

    private void createContent(Composite root) { 

        this.overrideSettingsCheckbox = new Button(root, SWT.CHECK);
        this.overrideSettingsCheckbox.setText("Use project as composite root");
        GridDataFactory.swtDefaults().applyTo(root);
        
        Label line = new Label(root, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1).applyTo(line);
        
        // composite root container
        Composite workspaceCompositeNameComposite = new Composite(root, SWT.NONE);
        GridLayoutFactory.swtDefaults().extendedMargins(0, 0, 0, 10).numColumns(3).applyTo(workspaceCompositeNameComposite);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).span(3, SWT.DEFAULT).applyTo(workspaceCompositeNameComposite);

        // root project label
        rootProjectLabel = new Label(workspaceCompositeNameComposite, SWT.NONE);
        rootProjectLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        rootProjectLabel.setText(WorkspaceCompositeWizardMessages.Label_RootProject);

        // root project text field
        this.workspaceCompositeRootProjectLabel = new Text(workspaceCompositeNameComposite, SWT.BORDER);
        this.workspaceCompositeRootProjectLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        // root project select button
        selectRootProject = new Button(workspaceCompositeNameComposite, SWT.PUSH);
        selectRootProject.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        selectRootProject.setText(WorkspaceCompositeWizardMessages.Button_Select_RootProject);
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
