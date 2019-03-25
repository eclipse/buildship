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
import org.eclipse.buildship.ui.internal.util.widget.UiBuilder;
import org.eclipse.buildship.ui.internal.wizard.project.ProjectImportWizard;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;

/**
 * Page in the {@link ProjectImportWizard} showing a preview about the project about to be imported.
 */
@SuppressWarnings("unused")
public final class GradleRootProjectWizardPage extends AbstractWizardPage {

	private Text workspaceCompositeRootProjectLabel;
    private Text overrideCheckboxLabel;
    private Button overrideSettingsCheckbox;

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
        UiBuilder.UiBuilderFactory uiBuilderFactory = getUiBuilderFactory();

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
        uiBuilderFactory.newLabel(workspaceCompositeNameComposite).alignLeft().text(WorkspaceCompositeWizardMessages.Label_RootProject).control();

        // root project text field
        this.workspaceCompositeRootProjectLabel = uiBuilderFactory.newText(workspaceCompositeNameComposite).alignFillHorizontal().control();
        
        // root project select button
        uiBuilderFactory.newButton(workspaceCompositeNameComposite).alignRight().text(WorkspaceCompositeWizardMessages.Button_Select_RootProject).control();
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
