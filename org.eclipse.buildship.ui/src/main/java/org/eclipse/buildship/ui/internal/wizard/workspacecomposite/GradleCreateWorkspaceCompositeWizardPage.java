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
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.util.binding.Property;
import org.eclipse.buildship.ui.internal.util.layout.LayoutUtils;
import org.eclipse.buildship.ui.internal.util.widget.GradleProjectGroup;

/**
 * Page on the {@link WorkspaceCompositeCreationWizard} declaring the workspace composite name and included projects.
 */
public final class GradleCreateWorkspaceCompositeWizardPage extends AbstractCompositeWizardPage {

    private final CompositeCreationConfiguration creationConfiguration;

    @SuppressWarnings("unused")
    private Text workspaceCompositeNameText;
    private Label compositeName;
    private GradleProjectGroup gradleProjectCheckboxtreeComposite;

    private static IWorkingSet gradleComposite;
    private boolean firstCheck;

    public GradleCreateWorkspaceCompositeWizardPage(CompositeConfiguration importConfiguration, CompositeCreationConfiguration creationConfiguration) {
        super("NewGradleWorkspaceComposite", WorkspaceCompositeWizardMessages.Title_NewGradleWorkspaceCompositeWizardPage, WorkspaceCompositeWizardMessages.InfoMessage_NewGradleWorkspaceCompositeWizardPageDefault, //$NON-NLS-1$
                importConfiguration, ImmutableList.of(creationConfiguration.getCompositeName(), creationConfiguration.getCompositeProjects()));

        this.creationConfiguration = creationConfiguration;
        this.firstCheck = true;
    }

    public GradleCreateWorkspaceCompositeWizardPage() {
        this(getCompositeImportConfiguration(), getCompositeCreationConfiguration());
    }


    private static CompositeCreationConfiguration getCompositeCreationConfiguration() {
        CompositeCreationWizardController creationController;
        ArrayList<IAdaptable> compositeElements = new ArrayList<>();
        if (gradleComposite != null) {
            creationController = new CompositeCreationWizardController(gradleComposite.getName(), compositeElements);
        } else {
            creationController = new CompositeCreationWizardController("", compositeElements);
        }
        return creationController.getConfiguration();
    }

    private static CompositeConfiguration getCompositeImportConfiguration() {
        return new CompositeConfiguration();
    }

    protected String getPageId() {
        return "org.eclipse.buildship.ui.GradleCompositePage"; //$NON-NLS-1$
    }

    @Override
    protected void createWidgets(Composite root) {
        root.setLayout(LayoutUtils.newGridLayout(3));
        createContent(root);
        bindToConfiguration();
    }

    private void createContent(Composite root) {

        // composite name container
        Composite workspaceCompositeNameComposite = new Composite(root, SWT.FILL);
        GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 5).numColumns(2).applyTo(workspaceCompositeNameComposite);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).span(3, SWT.DEFAULT).applyTo(workspaceCompositeNameComposite);

        // composite name label
        this.compositeName = new Label(workspaceCompositeNameComposite, SWT.NONE);
        this.compositeName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        this.compositeName.setText(WorkspaceCompositeWizardMessages.Label_CompositeName);

        // composite name text field
        this.workspaceCompositeNameText = new Text(workspaceCompositeNameComposite, SWT.BORDER);
        this.workspaceCompositeNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        this.gradleProjectCheckboxtreeComposite = new GradleProjectGroup(root, hasSelectedElement());
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).span(3, SWT.DEFAULT).applyTo(this.gradleProjectCheckboxtreeComposite);

        getConfiguration().getProjectList().setValue(new IAdaptable[0]);

        if (gradleComposite != null) {
            this.workspaceCompositeNameText.setText(gradleComposite.getName());
        }
    }

    private void bindToConfiguration() {
        this.workspaceCompositeNameText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                updateLocation();
            }
        });
        this.gradleProjectCheckboxtreeComposite.getCheckboxTree().addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                updateCompositeProjects();
            }
        });
    }

    private void updateLocation() {
         File parentLocation = CorePlugin.getInstance().getStateLocation().append("workspace-composites").toFile();
         File projectDir = parentLocation != null ? new File(parentLocation, this.workspaceCompositeNameText.getText()) : null;

         // always update project name last to ensure project name validation errors have precedence in the UI
         getConfiguration().getCompositePreferencesDir().setValue(projectDir);
         this.creationConfiguration.setCompositeName(this.workspaceCompositeNameText.getText());
    }

    protected void updateCompositeProjects() {
        List<IAdaptable> projectList = new ArrayList<>();

        for (TreeItem treeElement : this.gradleProjectCheckboxtreeComposite.getCheckboxTree().getItems()) {
            if (treeElement.getChecked() == true) {
                if (treeElement.getText().contains(" (External): ")) {
                    //String[] treeValues = treeElement.getText().replace(" (External): ", "$").split("\\$");
                    // treeValues[0] contains the project name
                    // treeValues[1] contains the file path
                    //File externalFolder = new File(treeValues[1]);
                    projectList.add(null);
                } else {
                    projectList.add(ResourcesPlugin.getWorkspace().getRoot().getProject(treeElement.getText()));
                }
            }
        }
        getConfiguration().getProjectList().setValue(projectList.toArray(new IAdaptable[projectList.size()]));
        this.creationConfiguration.setCompositeProjects(projectList);
    }

    @Override
    protected String getPageContextInformation() {
        return WorkspaceCompositeWizardMessages.InfoMessage_NewGradleWorkspaceCompositeWizardPageContext;
    }

    /*
     * Has to be implemented here because of gradleComposite checks to prevent "name exists" bug while editing
     */
    @Override
    protected void validateInput(Property<?> source, Optional<String> validationErrorMessage) {
        String errorMessage = null;
        String infoMessage = null;
        String newText= this.workspaceCompositeNameText.getText();

        if (newText.equals(newText.trim()) == false) {
            errorMessage = WorkspaceCompositeWizardMessages.WarningMessage_GradleWorkspaceComposite_NameWhitespaces;
        }
        if (newText.isEmpty()) {
            if (this.firstCheck) {
                setPageComplete(false);
                this.firstCheck= false;
                return;
            } else {
                errorMessage = WorkspaceCompositeWizardMessages.WarningMessage_GradleWorkspaceComposite_NameEmpty;
            }
        }

        this.firstCheck= false;

        if (errorMessage == null && (gradleComposite == null || newText.equals(gradleComposite.getName()) == false)) {
            IWorkingSet[] workingSets= PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets();
            for (int i= 0; i < workingSets.length; i++) {
                if (newText.equals(workingSets[i].getName())) {
                    errorMessage= WorkspaceCompositeWizardMessages.WarningMessage_GradleWorkspaceComposite_CompositeNameExists;
                }
            }
        }

        if (!hasSelectedElement()) {
            infoMessage = WorkspaceCompositeWizardMessages.WarningMessage_GradleWorkspaceComposite_CompositeEmpty;
            setMessage(infoMessage, INFORMATION);
        } else {
            setMessage(WorkspaceCompositeWizardMessages.InfoMessage_NewGradleWorkspaceCompositeWizardPageDefault);
        }


        setErrorMessage(errorMessage);
        setPageComplete(errorMessage == null);
    }

    private boolean hasSelectedElement() {
        return this.creationConfiguration.getCompositeProjects().getValue().size() > 0;
    }

}
