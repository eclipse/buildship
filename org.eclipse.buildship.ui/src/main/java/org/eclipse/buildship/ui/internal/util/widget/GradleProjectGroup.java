/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.buildship.ui.internal.util.widget;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.buildship.ui.internal.wizard.workspacecomposite.WorkspaceCompositeWizardMessages;

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

        this.gradleProjectTree = new Tree(this, SWT.CHECK);
        this.gradleProjectTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));

        this.buttonComposite = new Composite(this, SWT.NONE);
        this.buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(this.buttonComposite);

        this.newGradleProject = new Button(this.buttonComposite, SWT.PUSH);
        this.newGradleProject.setText(WorkspaceCompositeWizardMessages.Button_New_GradleProject);
        this.newGradleProject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        this.addExternalGradleProject = new Button(this.buttonComposite, SWT.PUSH);
        this.addExternalGradleProject.setText(WorkspaceCompositeWizardMessages.Button_Add_ExternalGradleProject);
        this.addExternalGradleProject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        fillCheckboxTreeWithFakeData();
    }

    private void fillCheckboxTreeWithFakeData() {
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
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
