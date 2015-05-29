/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.wizard.project;

import java.util.Queue;

import com.gradleware.tooling.toolingutils.binding.Property;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;

import org.eclipse.core.databinding.AggregateValidationStatus;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.dialog.DialogPageSupport;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.WorkingSetConfigurationBlock;

import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration;
import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.buildship.ui.WorkingSetIds;
import org.eclipse.buildship.ui.databinding.ComputedProjectLocationDirValue;
import org.eclipse.buildship.ui.databinding.ProjectLocationDirObservable;
import org.eclipse.buildship.ui.databinding.ValidationInitialMessageProvider;
import org.eclipse.buildship.ui.databinding.converter.BooleanInvert;
import org.eclipse.buildship.ui.databinding.validator.ProjectNameValidator;
import org.eclipse.buildship.ui.util.file.DirectoryDialogSelectionListener;
import org.eclipse.buildship.ui.util.widget.UiBuilder;

/**
 * First Wizard page for the new Gradle project wizard.
 *
 */
public class GradleNewProjectWizardPage extends AbstractWizardPage {

    public static final String PROJECT_LOCATION_DIALOGSETTING_ARRAY = "projectLocations"; //$NON-NLS-1$

    Queue<String> locationQueue = EvictingQueue.create(10);

    private DataBindingContext dbc;

    private Text projectNameText;
    private Button defaultWorkspacelocationButton;
    private Label locationLabel;
    private Combo projectDirCombo;
    private Button projectDirBrowseButton;
    private WorkingSetConfigurationBlock workingSetConfigurationBlock;
    private AggregateValidationStatus validationStatus;

    public GradleNewProjectWizardPage(ProjectImportConfiguration configuration) {
        super("NewGradleProject", "Create a Gradle Project", "Enter a project name.", //$NON-NLS-1$
                configuration, ImmutableList.<Property<?>> of(configuration.getProjectDir()));
        setPageComplete(false);
    }

    @Override
    protected void createWidgets(Composite root) {
        GridLayoutFactory.swtDefaults().numColumns(3).applyTo(root);
        createContent(root);
        bindUI();
        // check the validationStatus from the bindings
        createPageCompleteObservable();
    }

    private void createContent(Composite root) {

        UiBuilder.UiBuilderFactory uiBuilderFactory = getUiBuilderFactory();

        uiBuilderFactory.newLabel(root).alignLeft().text("Project Name");

        // Text to specify a project name
        this.projectNameText = new Text(root, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).span(2, SWT.DEFAULT).applyTo(this.projectNameText);

        Group locationGroup = new Group(root, SWT.NONE);
        locationGroup.setText("Project location");
        GridLayoutFactory.swtDefaults().numColumns(3).applyTo(locationGroup);
        GridDataFactory.fillDefaults().grab(true, false).span(3, SWT.DEFAULT).applyTo(locationGroup);

        // check button for the usage of the default workspace location
        this.defaultWorkspacelocationButton = new Button(locationGroup, SWT.CHECK);
        GridDataFactory.fillDefaults().span(3, SWT.DEFAULT).applyTo(this.defaultWorkspacelocationButton);
        this.defaultWorkspacelocationButton.setText("Use default location");
        this.defaultWorkspacelocationButton.setSelection(true);

        // project directory label
        this.locationLabel = uiBuilderFactory.newLabel(locationGroup).alignLeft().text("Location").control();

        // combo for typing an alternative project path, which also provides recently used paths
        this.projectDirCombo = new Combo(locationGroup, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(this.projectDirCombo);
        addProjectLocationSettingBehavior();

        // browse button for file chooser
        this.projectDirBrowseButton = uiBuilderFactory.newButton(locationGroup).alignLeft().text(ProjectImportMessages.Button_Label_Browse).control();
        this.projectDirBrowseButton.addSelectionListener(new DirectoryDialogSelectionListener(root.getShell(), this.projectDirCombo,
                ProjectImportMessages.Label_ProjectRootDirectory));

        // create workingset configuration group
        this.workingSetConfigurationBlock = new WorkingSetConfigurationBlock(new String[] { WorkingSetIds.RESOURCE, WorkingSetIds.JAVA }, UiPlugin.getInstance()
                .getDialogSettings());

        Group workingSetGroup = new Group(root, SWT.NONE);
        workingSetGroup.setText("Working sets");
        GridLayoutFactory.swtDefaults().applyTo(workingSetGroup);
        GridDataFactory.fillDefaults().grab(true, false).span(3, SWT.DEFAULT).applyTo(workingSetGroup);

        this.workingSetConfigurationBlock.createContent(workingSetGroup);
    }

    private void addProjectLocationSettingBehavior() {
        IDialogSettings dialogSettings = getDialogSettings();
        String[] locations = dialogSettings.getArray(PROJECT_LOCATION_DIALOGSETTING_ARRAY);
        if (locations != null && locations.length > 0) {
            this.projectDirCombo.setItems(locations);
            this.projectDirCombo.setText(locations[0]);
        } else {
            IPath location = ResourcesPlugin.getWorkspace().getRoot().getLocation();
            this.projectDirCombo.setText(location.toOSString());
        }
        this.projectDirCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                Combo combo = (Combo) e.getSource();
                GradleNewProjectWizardPage.this.locationQueue.offer(combo.getText());
            }
        });
    }

    private void bindUI() {
        this.dbc = new DataBindingContext();

        // bind project name
        ISWTObservableValue projectNameTarget = WidgetProperties.text(SWT.Modify).observe(this.projectNameText);
        UpdateValueStrategy updateProjectNameStrategy = new UpdateValueStrategy();
        updateProjectNameStrategy.setAfterGetValidator(new ProjectNameValidator());
        WritableValue projectNameValue = new WritableValue("", String.class);
        Binding projectNameBinding = this.dbc.bindValue(projectNameTarget, projectNameValue, updateProjectNameStrategy, null);
        addControlDecorationSupport(projectNameBinding);

        // bind default location usage
        ISWTObservableValue defaultlocationSelection = WidgetProperties.selection().observe(this.defaultWorkspacelocationButton);

        // bind project directory
        ISWTObservableValue projectDirTextTarget = WidgetProperties.selection().observe(this.projectDirCombo);

        // bind complete file to ProjectImportConfiguration
        ProjectLocationDirObservable projectDirConfiguration = new ProjectLocationDirObservable(getConfiguration());
        ComputedProjectLocationDirValue computedProjectLocationDirValue = new ComputedProjectLocationDirValue(projectNameValue, defaultlocationSelection, projectDirTextTarget);
        this.dbc.bindValue(computedProjectLocationDirValue, projectDirConfiguration);

        // bind enabled state of location configuration
        UpdateValueStrategy booleanInvertStrategy = new UpdateValueStrategy();
        booleanInvertStrategy.setConverter(new BooleanInvert());
        this.dbc.bindValue(WidgetProperties.enabled().observe(this.locationLabel), defaultlocationSelection, booleanInvertStrategy, booleanInvertStrategy);
        this.dbc.bindValue(WidgetProperties.enabled().observe(this.projectDirCombo), defaultlocationSelection, booleanInvertStrategy, booleanInvertStrategy);
        this.dbc.bindValue(WidgetProperties.enabled().observe(this.projectDirBrowseButton), defaultlocationSelection, booleanInvertStrategy, booleanInvertStrategy);
    }

    private void addControlDecorationSupport(final Binding binding) {
        binding.getTarget().addChangeListener(new IChangeListener() {

            private boolean isControlDecorationSupportAdded;

            @Override
            public void handleChange(ChangeEvent event) {
                if (!this.isControlDecorationSupportAdded) {
                    this.isControlDecorationSupportAdded = true;
                    // show validation status at the widget itself
                    ControlDecorationSupport.create(binding, SWT.LEFT | SWT.TOP);
                    // show validation status as Wizard message
                    DialogPageSupport dialogPageSupport = DialogPageSupport.create(GradleNewProjectWizardPage.this, GradleNewProjectWizardPage.this.dbc);
                    dialogPageSupport.setValidationMessageProvider(new ValidationInitialMessageProvider("Enter a project name."));
                }
            }
        });
    }

    private void createPageCompleteObservable() {
        this.validationStatus = new AggregateValidationStatus(this.dbc, AggregateValidationStatus.MAX_SEVERITY);
        this.validationStatus.addValueChangeListener(new IValueChangeListener() {

            @Override
            public void handleValueChange(ValueChangeEvent event) {
                IObservableValue observableValue = event.getObservableValue();
                Object value = observableValue.getValue();
                // only allow to complete this page if the status is ok
                setPageComplete(value instanceof IStatus && ((IStatus) value).isOK());
            }
        });
    }

    @Override
    public boolean isPageComplete() {
        Object value = this.validationStatus.getValue();
        return (value instanceof IStatus && ((IStatus) value).isOK());
    }

    public IWorkingSet[] getSelectedWorkingSets() {
        return this.workingSetConfigurationBlock.getSelectedWorkingSets();
    }

    @Override
    protected String getPageContextInformation() {
        return "Click the Finish button to finish the wizard and create a default Gradle project. Click the Next button to select optional options.";
    }

    @Override
    public void dispose() {
        if (this.dbc != null) {
            this.dbc.dispose();
        }
        if (this.locationQueue != null && !this.locationQueue.isEmpty()) {
            getDialogSettings().put(PROJECT_LOCATION_DIALOGSETTING_ARRAY, this.locationQueue.toArray(new String[this.locationQueue.size()]));
        }
        super.dispose();
    }

}
