/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 *     Sebastian Kuzniarz (Diebold Nixdorf Inc.) - adaptation and customization for workspace composite wizard 
 */

package org.eclipse.buildship.ui.internal.wizard.workspacecomposite;

import org.eclipse.buildship.ui.internal.util.layout.LayoutUtils;
import org.eclipse.buildship.ui.internal.util.widget.GradleProjectGroup;
import org.eclipse.buildship.ui.internal.util.widget.UiBuilder;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * Page on the {@link org.eclipse.buildship.ui.internal.wizard.project.ProjectCreationWizard} declaring the project name and project location.
 */
public final class GradleCreateWorkspaceCompositeWizardPage extends AbstractWizardPage {
	@SuppressWarnings("unused")
    private Text workspaceCompositeNameText;
	private GradleProjectGroup gradleProjectCheckboxtreeComposite;

    public GradleCreateWorkspaceCompositeWizardPage() {
    	super("NewGradleWorkspaceComposite", WorkspaceCompositeWizardMessages.Title_NewGradleWorkspaceCompositeWizardPage, WorkspaceCompositeWizardMessages.InfoMessage_NewGradleWorkspaceCompositeWizardPageDefault); //$NON-NLS-1$
	}

	@Override
    protected void createWidgets(Composite root) {
        root.setLayout(LayoutUtils.newGridLayout(3));
        createContent(root);
    }

    private void createContent(Composite root) {
        UiBuilder.UiBuilderFactory uiBuilderFactory = getUiBuilderFactory();

        // composite name container
        Composite workspaceCompositeNameComposite = new Composite(root, SWT.FILL);
        GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 5).numColumns(2).applyTo(workspaceCompositeNameComposite);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).span(3, SWT.DEFAULT).applyTo(workspaceCompositeNameComposite);

        // composite name label
        uiBuilderFactory.newLabel(workspaceCompositeNameComposite).alignLeft().text(WorkspaceCompositeWizardMessages.Label_CompositeName).control();

        // composite name text field
        this.workspaceCompositeNameText = uiBuilderFactory.newText(workspaceCompositeNameComposite).alignFillHorizontal().control();
        
        this.gradleProjectCheckboxtreeComposite = new GradleProjectGroup(root);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).span(3, SWT.DEFAULT).applyTo(this.gradleProjectCheckboxtreeComposite);

    }

    @Override
    protected String getPageContextInformation() {
        return WorkspaceCompositeWizardMessages.InfoMessage_NewGradleWorkspaceCompositeWizardPageContext;
    }

}
