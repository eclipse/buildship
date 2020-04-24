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

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.core.internal.configuration.GradleProjectNature;
import org.eclipse.buildship.ui.internal.util.file.ExternalProjectDialogSelectionListener;
import org.eclipse.buildship.ui.internal.wizard.project.ProjectCreationWizard;
import org.eclipse.buildship.ui.internal.wizard.workspacecomposite.IGradleCompositeIDs;
import org.eclipse.buildship.ui.internal.wizard.workspacecomposite.WorkspaceCompositeWizardMessages;

@SuppressWarnings("unused")
public class GradleProjectGroup extends Group {

    private Font font;
    private Button newGradleProject;
    private Button addExternalGradleProject;
    private ExternalProjectDialogSelectionListener externalProjectListener;
    private Composite buttonComposite;
    private TreeViewer gradleProjectTree;
    private boolean editMode;

    public GradleProjectGroup(Composite parent, boolean editMode) {
        super(parent, SWT.NONE);
        setText(WorkspaceCompositeWizardMessages.Group_Label_GradleProjects);
        this.editMode = editMode;
        createWidgets();
    }

    public void createWidgets() {
        setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayoutFactory.swtDefaults().numColumns(4).applyTo(this);

        this.gradleProjectTree = new TreeViewer(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CHECK);
        this.gradleProjectTree.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
        fillCheckboxTreeWithProjects();
        if (this.editMode) {
            configureTree();
        }this.gradleProjectTree.setUseHashlookup(true);

        this.buttonComposite = new Composite(this, SWT.NONE);
        this.buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(this.buttonComposite);

        this.newGradleProject = new Button(this.buttonComposite, SWT.PUSH);
        this.newGradleProject.setText(WorkspaceCompositeWizardMessages.Button_New_GradleProject);
        this.newGradleProject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        this.addExternalGradleProject = new Button(this.buttonComposite, SWT.PUSH);
        this.addExternalGradleProject.setText(WorkspaceCompositeWizardMessages.Button_Add_ExternalGradleProject);
        this.addExternalGradleProject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        addListener();
    }

    private void addListener() {
        this.newGradleProject.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WizardDialog wizard = new WizardDialog(getShell(), new ProjectCreationWizard());
                if (wizard.open() == WizardDialog.OK) {
                    fillCheckboxTreeWithProjects();
                    configureTree();
                };
            }
        });
        this.externalProjectListener = new ExternalProjectDialogSelectionListener(getShell(), this.gradleProjectTree, "");
        this.addExternalGradleProject.addSelectionListener(this.externalProjectListener);
    }

    private void configureTree() {

        ArrayList<String> selection = getInitialTreeSelection();

        try {
            this.gradleProjectTree.getTree().setRedraw(false);
            for (TreeItem item : this.gradleProjectTree.getTree().getItems()) {
                if (selection.contains(item.getText())) {
                    item.setChecked(true);
                }
            }
        } finally {
            this.gradleProjectTree.getTree().setRedraw(true);
        }

    }

    public boolean hasSelectedItems() {
        return this.gradleProjectTree.getTree().getSelectionCount() > 0;
    }

    private ArrayList<String> getInitialTreeSelection() {
        ArrayList<String> projectNames = new ArrayList<>();
        BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
            @Override
            public void run() {
                IStructuredSelection projectSelection = null;
                    IWorkbenchPage page= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    if (page == null) {
                        return;
                    }

                    IWorkbenchPart part= page.getActivePart();
                    if (part == null) {
                        return;
                    }

                    try {
                        ISelectionProvider provider= part.getSite().getSelectionProvider();
                        if (provider != null) {
                            ISelection selection = provider.getSelection();
                            projectSelection = selection instanceof IStructuredSelection ? (IStructuredSelection) selection : StructuredSelection.EMPTY;
                        }
                    } catch (Exception e) {
                        return;
                    }


                Object[] elements= projectSelection.toArray();


                for (int i=0; i < elements.length; i++) {
                    if (elements[i] instanceof IWorkingSet) {
                        IWorkingSet ge = ((IWorkingSet) elements[i]);
                        if (ge != null && ge.getId().equals(IGradleCompositeIDs.NATURE)) {
                            for (int j = 0; j < ge.getElements().length; j++) {
                                IAdaptable[] element = ge.getElements();
                                projectNames.add(((IAdaptable) element[j]).getAdapter(IProject.class).getName());
                            }
                            elements[i] = projectNames;
                        }
                    }
                }
            }
        });

        return projectNames;
}

    private void fillCheckboxTreeWithProjects() {
        this.gradleProjectTree.getTree().removeAll();
          try {
              IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
              IProject[] projects = workspaceRoot.getProjects();
              for(int i = 0; i < projects.length; i++) {
                 IProject project = projects[i];
                 if(project.hasNature(GradleProjectNature.ID)) {
                    TreeItem jItem = new TreeItem(this.gradleProjectTree.getTree(), 0);
                    jItem.setText(project.getName());
                 }
              }
           }
           catch(CoreException ce) {
              ce.printStackTrace();
     }
    }

    public Tree getCheckboxTree() {
        return this.gradleProjectTree.getTree();
    }

    public TreeViewer getCheckboxTreeViewer() {
        return this.gradleProjectTree;
    }

    public Map<String, String> getExternalProjectPathList() {
        return this.externalProjectListener.getExternalProjectPaths();
    }

    @Override
        protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
