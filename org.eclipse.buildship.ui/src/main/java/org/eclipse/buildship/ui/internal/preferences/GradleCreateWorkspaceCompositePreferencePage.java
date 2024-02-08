/*******************************************************************************
 * Copyright (c) 2020 Gradle Inc.
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

import org.eclipse.buildship.core.BuildConfiguration;
import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.GradleProjectNature;
import org.eclipse.buildship.core.internal.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.internal.workspace.InternalGradleBuild;
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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
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

    //TODO (kuzniarz) This will definitely need rework IAdaptable -> File
    private static CompositeCreationConfiguration getCompositeCreationConfiguration() {
        List<File> compositeElements = new ArrayList<>();
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

        if (gradleComposite != null) {
            this.workspaceCompositeNameText.setText(gradleComposite.getName());
            this.gradleProjectCheckboxtreeComposite.setCheckboxTreeSelection(gradleComposite.getElements());
        }
        
        addListeners();
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
        List<File> projectList = new ArrayList<>();

        for (TreeItem treeElement : this.gradleProjectCheckboxtreeComposite.getCheckboxTree().getItems()) {
            if (treeElement.getChecked() == true) {
                if (treeElement.getText().contains(" (External): ")) {
                    String[] treeValues = treeElement.getText().replace(" (External): ", "$").split("\\$");
                    // treeValues[0] contains the project name
                    // treeValues[1] contains the file path
                    File externalFolder = new File(treeValues[1]);
                    projectList.add(externalFolder);
                } else {
                    projectList.add(getGradleRootFor(ResourcesPlugin.getWorkspace().getRoot().getProject(treeElement.getText())));
                }
            }
        }
        getConfiguration().getIncludedBuildsList().setValue(projectList);
        this.creationConfiguration.setCompositeProjects(projectList);
    }
    
    protected File getGradleRootFor(IProject project) {
		InternalGradleBuild gradleBuild = (InternalGradleBuild) CorePlugin.internalGradleWorkspace().getBuild(project).get();
		return gradleBuild.getBuildConfig().getRootProjectDirectory();
    }

    private void updateLocation() {
        // always update project name last to ensure project name validation errors have
        // precedence in the UI
        getConfiguration().getCompositeName().setValue(this.workspaceCompositeNameText.getText());
        this.creationConfiguration.setCompositeName(this.workspaceCompositeNameText.getText());
    }

    @Override
    protected String getPageContextInformation() {
        return WorkspaceCompositeWizardMessages.InfoMessage_NewGradleWorkspaceCompositeWizardPageContext;
    }

    @Override
    public void finish() {
        try {
        	updateCompositeProjects();
            String workspaceCompositeName = this.workspaceCompositeNameText.getText();
            String oldWorkspaceCompositeName = "";
            IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
            File compositePreferenceFile = getWorkspaceCompositesPropertiesFile(workspaceCompositeName);
            IAdaptable[] compositeElements = getCompositeElements(getConfiguration().getIncludedBuildsList().getValue());
            if (gradleComposite == null) {
                gradleComposite = workingSetManager.createWorkingSet(workspaceCompositeName, compositeElements);
                gradleComposite.setId(IGradleCompositeIDs.NATURE);
            } else {
                IAdaptable[] oldElements = gradleComposite.getElements();
                oldWorkspaceCompositeName = gradleComposite.getName();
                if (!gradleComposite.getName().equals(this.workspaceCompositeNameText.getText())) {
                    gradleComposite.setName(this.workspaceCompositeNameText.getText());
                }

                if (!oldElements.equals(compositeElements)) {
                    gradleComposite.setElements(compositeElements);
                }
            }
            FileOutputStream out = new FileOutputStream(compositePreferenceFile.getAbsoluteFile());
            Properties prop = getConfiguration().toCompositeProperties().toProperties();
            prop.store(out, " ");
            out.close();
            if (!workspaceCompositeName.equals(oldWorkspaceCompositeName)) {
                File oldCompositeProperties = getWorkspaceCompositesPropertiesFile(oldWorkspaceCompositeName);
                oldCompositeProperties.delete();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private File getWorkspaceCompositesPropertiesFile(String compositeName) {
    	return CorePlugin.getInstance().getStateLocation()
                .append("workspace-composites").append(compositeName).toFile();
    }

    private IAdaptable[] getCompositeElements(List<File> includedBuildsList) {
    	List<IAdaptable> compositeElements = new ArrayList<>();
		for (File includedBuild : includedBuildsList) {
			//TODO (kuzniarz) Files need to be added to composite to be viewed in ProjectExplorer
			if (isExternalProject(includedBuild)) {
				//compositeElements.add(ResourcesPlugin.getWorkspace().getRoot().getFile(Path.fromOSString(includedBuild.getAbsolutePath())));
			} else {
				compositeElements.add(ResourcesPlugin.getWorkspace().getRoot().getProject(includedBuild.getName()));
			}
		}
		return compositeElements.toArray(new IAdaptable[includedBuildsList.size()]);
	}

	private boolean isExternalProject(File includedBuild) {
		return !ResourcesPlugin.getWorkspace().getRoot().getProject(includedBuild.getName()).exists();
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
