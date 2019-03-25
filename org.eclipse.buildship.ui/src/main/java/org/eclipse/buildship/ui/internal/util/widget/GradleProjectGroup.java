/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sebastian Kuzniarz (Diebold Nixdorf Inc.) - initial UI implementation
 */

package org.eclipse.buildship.ui.internal.util.widget;

import org.eclipse.buildship.ui.internal.util.font.FontUtils;
import org.eclipse.buildship.ui.internal.util.gradle.GradleDistributionViewModel;
import org.eclipse.buildship.ui.internal.util.widget.UiBuilder.UiBuilderFactory;
import org.eclipse.buildship.ui.internal.wizard.workspacecomposite.WorkspaceCompositeWizardMessages;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

@SuppressWarnings("unused")
public class GradleProjectGroup extends Group {
	
    private Font font;
	private Button newGradleProject;
	private Button addExternalGradleProject;
	private Composite buttonComposite;
	private Tree gradleProjectTree;
	
	public GradleProjectGroup(Composite parent) {
        super(parent, SWT.NONE);
        setText(WorkspaceCompositeWizardMessages.Group_Label_GradleProjects);

        createWidgets();
    }
	
	public void createWidgets() {
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayoutFactory.swtDefaults().numColumns(4).applyTo(this);
		
		this.font = FontUtils.getDefaultDialogFont();
        UiBuilderFactory uiBuilder = new UiBuilder.UiBuilderFactory(this.font);
        
		this.gradleProjectTree = uiBuilder.newCheckboxTree(this).alignFillBoth(3).control();
		
		this.buttonComposite = uiBuilder.newComposite(this).alignFillVerticalTop().control();
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(buttonComposite);
		
		this.newGradleProject = uiBuilder.newButton(buttonComposite).text(WorkspaceCompositeWizardMessages.Button_New_GradleProject).alignFillHorizontal().control();
		this.addExternalGradleProject = uiBuilder.newButton(buttonComposite).text(WorkspaceCompositeWizardMessages.Button_Add_ExternalGradleProject).alignFillHorizontal().control();
		
		fillCheckboxTree();
	}

	
	private void fillCheckboxTree() {
		/*
		for (int i = 0; i < 4; i++) {
		      TreeItem iItem = new TreeItem(this.gradleProjectTree, 0);
		      iItem.setText("TreeItem (0) -" + i);
		      for (int j = 0; j < 4; j++) {
		        TreeItem jItem = new TreeItem(iItem, 0);
		        jItem.setText("TreeItem (1) -" + j);
		        for (int k = 0; k < 4; k++) {
		          TreeItem kItem = new TreeItem(jItem, 0);
		          kItem.setText("TreeItem (2) -" + k);
		          for (int l = 0; l < 4; l++) {
		            TreeItem lItem = new TreeItem(kItem, 0);
		            lItem.setText("TreeItem (3) -" + l);
		          }
		        }
		      }
		    }
		  */
	}
	
	@Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
