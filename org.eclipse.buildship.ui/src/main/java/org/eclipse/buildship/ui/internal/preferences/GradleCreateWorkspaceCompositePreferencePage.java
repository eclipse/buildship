/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.buildship.ui.internal.preferences;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.ui.internal.util.layout.LayoutUtils;
import org.eclipse.buildship.ui.internal.util.widget.GradleProjectGroup;
import org.eclipse.buildship.ui.internal.wizard.workspacecomposite.CompositeConfiguration;
import org.eclipse.buildship.ui.internal.wizard.workspacecomposite.CompositeCreationConfiguration;
import org.eclipse.buildship.ui.internal.wizard.workspacecomposite.CompositeCreationWizardController;
import org.eclipse.buildship.ui.internal.wizard.workspacecomposite.CompositeImportWizardController;
import org.eclipse.buildship.ui.internal.wizard.workspacecomposite.GradleImportOptionsWizardPage;
import org.eclipse.buildship.ui.internal.wizard.workspacecomposite.IGradleCompositeIDs;
import org.eclipse.buildship.ui.internal.wizard.workspacecomposite.WorkspaceCompositeCreationWizard;
import org.eclipse.buildship.ui.internal.wizard.workspacecomposite.WorkspaceCompositeWizardMessages;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.IWizardPage;
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
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetPage;

import com.google.common.collect.ImmutableList;

/**
 * Page on the {@link WorkspaceCompositeCreationWizard} declaring the workspace
 * composite name and included projects.
 */
public final class GradleCreateWorkspaceCompositePreferencePage extends AbstractPropertiesPage
        implements IWorkingSetPage {

    private final CompositeCreationConfiguration creationConfiguration;

    @SuppressWarnings("unused")
    private Text workspaceCompositeNameText;
    private Label compositeName;
    private GradleProjectGroup gradleProjectCheckboxtreeComposite;

    private static IWorkingSet gradleComposite;
    private boolean firstCheck;
    private static CompositeImportWizardController importController;

    public GradleCreateWorkspaceCompositePreferencePage(CompositeConfiguration importConfiguration,
            CompositeCreationConfiguration creationConfiguration) {
        super("NewGradleWorkspaceComposite", //$NON-NLS-1$
                WorkspaceCompositeWizardMessages.Title_NewGradleWorkspaceCompositeWizardPage,
                WorkspaceCompositeWizardMessages.InfoMessage_NewGradleWorkspaceCompositeWizardPageDefault, importConfiguration, ImmutableList.of(creationConfiguration.getCompositeName(),
                        creationConfiguration.getCompositeProjects()));
        gradleComposite = null;
        this.creationConfiguration = creationConfiguration;
        this.firstCheck = true;
    }

    public GradleCreateWorkspaceCompositePreferencePage() {
        this(getCompositeImportConfiguration(), getCompositeCreationConfiguration());
    }

    private IWizardPage buildImportOptionsWizardPage() {
        IWizardPage page = new GradleImportOptionsWizardPage(getConfiguration());
        page.setWizard(getWizard());
        return page;
    }

    protected String getPageId() {
        return "org.eclipse.buildship.ui.GradleCompositePage"; //$NON-NLS-1$
    }

    private static CompositeCreationConfiguration getCompositeCreationConfiguration() {
        ArrayList<IAdaptable> compositeElements = new ArrayList<>();
        String compositeName = gradleComposite != null ? gradleComposite.getName() : "";
        CompositeCreationWizardController creationController = new CompositeCreationWizardController(compositeName, compositeElements);
        return creationController.getConfiguration();
    }

    private static CompositeConfiguration getCompositeImportConfiguration() {
        importController = new CompositeImportWizardController(null);
        return importController.getConfiguration();
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
    }

    @Override
    protected void createWidgets(Composite root) {
        root.setLayout(LayoutUtils.newGridLayout(3));
        createContent(root);
    }

    private void createContent(Composite root) {

        // composite name container
        Composite workspaceCompositeNameComposite = new Composite(root, SWT.FILL);
        GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 5).numColumns(2)
                .applyTo(workspaceCompositeNameComposite);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).span(3, SWT.DEFAULT)
                .applyTo(workspaceCompositeNameComposite);

        // composite name label
        this.compositeName = new Label(workspaceCompositeNameComposite, SWT.NONE);
        this.compositeName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        this.compositeName.setText(WorkspaceCompositeWizardMessages.Label_CompositeName);

        // composite name text field
        this.workspaceCompositeNameText = new Text(workspaceCompositeNameComposite, SWT.BORDER);
        this.workspaceCompositeNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        this.gradleProjectCheckboxtreeComposite = new GradleProjectGroup(root, (gradleComposite != null));
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).span(3, SWT.DEFAULT)
                .applyTo(this.gradleProjectCheckboxtreeComposite);

        addListeners();

        if (gradleComposite != null) {
            this.workspaceCompositeNameText.setText(gradleComposite.getName());
        }
    }

    private void addListeners() {
        this.workspaceCompositeNameText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                updateLocation();
                validateInput();
            }
        });
        this.gradleProjectCheckboxtreeComposite.getCheckboxTree().addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                updateCompositeProjects();
                validateInput();
            }
        });
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

    private void updateLocation() {
        File parentLocation = CorePlugin.getInstance().getStateLocation().append("workspace-composites").toFile();
        File projectDir = parentLocation != null ? new File(parentLocation, this.workspaceCompositeNameText.getText())
                : null;

        // always update project name last to ensure project name validation errors have
        // precedence in the UI
        getConfiguration().getCompositePreferencesDir().setValue(projectDir);
        this.creationConfiguration.setCompositeName(this.workspaceCompositeNameText.getText());
    }

    @Override
    protected String getPageContextInformation() {
        return WorkspaceCompositeWizardMessages.InfoMessage_NewGradleWorkspaceCompositeWizardPageContext;
    }

    @Override
    public void finish() {
        updateCompositeProjects();
        String workspaceCompositeName = this.workspaceCompositeNameText.getText();
        IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();

        try {
            File compositePreferenceFile = CorePlugin.getInstance().getStateLocation()
                    .append("workspace-composites").append(workspaceCompositeName).toFile();

            if (gradleComposite == null) {
                gradleComposite = workingSetManager.createWorkingSet(workspaceCompositeName,
                        getConfiguration().getProjectList().getValue());
                gradleComposite.setId(IGradleCompositeIDs.NATURE);
            } else {
                IAdaptable[] oldElements = gradleComposite.getElements();
                if (!gradleComposite.getName().equals(this.workspaceCompositeNameText.getText())) {
                    gradleComposite.setName(this.workspaceCompositeNameText.getText());
                }

                if (!oldElements.equals(getConfiguration().getProjectList().getValue())) {
                    gradleComposite.setElements(getConfiguration().getProjectList().getValue());
                }
            }
            FileOutputStream out = new FileOutputStream(compositePreferenceFile.getAbsoluteFile());
            Properties prop = getConfiguration().toCompositeProperties().toProperties();
            prop.store(out, " ");
            out.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public IWorkingSet getSelection() {
        return gradleComposite;
    }

    @Override
    public void setSelection(IWorkingSet workingSet) {
        Assert.isNotNull(workingSet, "Composite must not be null"); //$NON-NLS-1$
        gradleComposite = workingSet;
        if (getContainer() == null && getShell() != null && this.workspaceCompositeNameText != null) {
            this.workspaceCompositeNameText.setText(gradleComposite.getName());
        }
    }

    @Override
    public IWizardPage getNextPage() {
        return buildImportOptionsWizardPage();
    }

    protected void validateInput() {
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
        }

        setMessage(infoMessage, INFORMATION);
        setErrorMessage(errorMessage);
        setPageComplete(errorMessage == null);
    }

    private boolean hasSelectedElement() {
        return this.creationConfiguration.getCompositeProjects().getValue().size() > 0;
    }

}
