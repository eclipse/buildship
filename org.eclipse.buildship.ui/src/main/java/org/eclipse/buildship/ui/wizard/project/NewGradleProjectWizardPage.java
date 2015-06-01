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
import org.eclipse.buildship.ui.UiPluginConstants;
import org.eclipse.buildship.ui.util.databinding.conversion.BooleanInvert;
import org.eclipse.buildship.ui.util.databinding.dialog.MessageRestoringValidationMessageProvider;
import org.eclipse.buildship.ui.util.databinding.observable.ProjectLocationComputedValue;
import org.eclipse.buildship.ui.util.file.DirectoryDialogSelectionListener;
import org.eclipse.buildship.ui.util.widget.UiBuilder;

/**
 * First Wizard page for the new Gradle project wizard.
 *
 */
public final class NewGradleProjectWizardPage extends AbstractWizardPage {

    public static final String PROJECT_LOCATION_DIALOG_SETTING_ARRAY = "projectLocations"; //$NON-NLS-1$

    private final Queue<String> locationQueue = EvictingQueue.create(10);

    private DataBindingContext dbc;

    private Text projectNameText;
    private Button defaultWorkspaceLocationButton;
    private Label locationLabel;
    private Combo projectDirCombo;
    private Button projectDirBrowseButton;
    private WorkingSetConfigurationBlock workingSetConfigurationBlock;
    private AggregateValidationStatus validationStatus;

    public NewGradleProjectWizardPage(ProjectImportConfiguration configuration) {
        super("NewGradleProject", ProjectWizardMessages.Title_NewGradleProjectWizardPage, ProjectWizardMessages.InfoMessage_NewGradleProjectWizardPageDefault, //$NON-NLS-1$
                configuration, ImmutableList.<Property<?>> of(configuration.getProjectDir()));
        setPageComplete(false);
    }

    @Override
    protected void createWidgets(Composite root) {
        GridLayoutFactory.swtDefaults().numColumns(3).applyTo(root);
        createContent(root);
        bindUI();
        createPageCompleteObservable();
    }

    private void createContent(Composite root) {
        UiBuilder.UiBuilderFactory uiBuilderFactory = getUiBuilderFactory();

        uiBuilderFactory.newLabel(root).alignLeft().text(ProjectWizardMessages.Label_ProjectName);

        // Text to specify a project name
        this.projectNameText = new Text(root, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).span(2, SWT.DEFAULT).applyTo(this.projectNameText);

        Group locationGroup = new Group(root, SWT.NONE);
        locationGroup.setText(ProjectWizardMessages.Label_ProjectLocation);
        GridLayoutFactory.swtDefaults().numColumns(3).applyTo(locationGroup);
        GridDataFactory.fillDefaults().grab(true, false).span(3, SWT.DEFAULT).applyTo(locationGroup);

        // check button for the usage of the default workspace location
        this.defaultWorkspaceLocationButton = new Button(locationGroup, SWT.CHECK);
        GridDataFactory.fillDefaults().span(3, SWT.DEFAULT).applyTo(this.defaultWorkspaceLocationButton);
        this.defaultWorkspaceLocationButton.setText(ProjectWizardMessages.Button_UseDefaultLocation);
        this.defaultWorkspaceLocationButton.setSelection(true);

        // project directory label
        this.locationLabel = uiBuilderFactory.newLabel(locationGroup).alignLeft().text(ProjectWizardMessages.Label_CustomLocation).control();

        // combo for typing an alternative project path, which also provides recently used paths
        this.projectDirCombo = new Combo(locationGroup, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(this.projectDirCombo);
        addProjectLocationSettingBehavior();

        // browse button for file chooser
        this.projectDirBrowseButton = uiBuilderFactory.newButton(locationGroup).alignLeft().text(ProjectWizardMessages.Button_Label_Browse).control();
        this.projectDirBrowseButton.addSelectionListener(new DirectoryDialogSelectionListener(root.getShell(), this.projectDirCombo,
                ProjectWizardMessages.Label_ProjectRootDirectory));

        // create workingset configuration group
        this.workingSetConfigurationBlock = new WorkingSetConfigurationBlock(new String[] { UiPluginConstants.RESOURCE, UiPluginConstants.JAVA }, UiPlugin.getInstance()
                .getDialogSettings());

        Group workingSetGroup = new Group(root, SWT.NONE);
        workingSetGroup.setText(ProjectWizardMessages.Label_WorkingSets);
        GridLayoutFactory.swtDefaults().applyTo(workingSetGroup);
        GridDataFactory.fillDefaults().grab(true, false).span(3, SWT.DEFAULT).applyTo(workingSetGroup);

        this.workingSetConfigurationBlock.createContent(workingSetGroup);
    }

    private void addProjectLocationSettingBehavior() {
        IDialogSettings dialogSettings = getDialogSettings();
        String[] locations = dialogSettings.getArray(PROJECT_LOCATION_DIALOG_SETTING_ARRAY);
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
                NewGradleProjectWizardPage.this.locationQueue.offer(combo.getText());
            }
        });
    }

    private void bindUI() {
        this.dbc = new DataBindingContext();

        // bind project name
        ISWTObservableValue projectNameTarget = WidgetProperties.text(SWT.Modify).observe(this.projectNameText);
        UpdateValueStrategy updateProjectNameStrategy = new UpdateValueStrategy();
        updateProjectNameStrategy.setAfterGetValidator(ProjectNameValidator.INSTANCE);
        WritableValue projectNameValue = new WritableValue("", String.class); //$NON-NLS-1$
        Binding projectNameBinding = this.dbc.bindValue(projectNameTarget, projectNameValue, updateProjectNameStrategy, null);
        addControlDecorationSupport(projectNameBinding);

        // bind default location usage
        ISWTObservableValue defaultLocationSelection = WidgetProperties.selection().observe(this.defaultWorkspaceLocationButton);

        // bind project directory
        ISWTObservableValue projectDirTextTarget = WidgetProperties.selection().observe(this.projectDirCombo);

        // bind complete file to ProjectImportConfiguration
        ProjectImportConfigurationProjectDirObservable projectDirConfiguration = new ProjectImportConfigurationProjectDirObservable(getConfiguration());
        ProjectLocationComputedValue projectLocationComputedValue = new ProjectLocationComputedValue(projectNameValue, defaultLocationSelection, projectDirTextTarget);
        UpdateValueStrategy updateProjectLocationStrategy = new UpdateValueStrategy();
        updateProjectLocationStrategy.setBeforeSetValidator(new ProjectCustomLocationValidator(defaultLocationSelection));
        Binding projectCustomLocationBinding = this.dbc.bindValue(projectLocationComputedValue, projectDirConfiguration, updateProjectLocationStrategy, null);
        addControlDecorationSupport(projectCustomLocationBinding);

        // bind enabled state of location configuration
        UpdateValueStrategy booleanInvertStrategy = new UpdateValueStrategy();
        booleanInvertStrategy.setConverter(new BooleanInvert());
        this.dbc.bindValue(WidgetProperties.enabled().observe(this.locationLabel), defaultLocationSelection, booleanInvertStrategy, booleanInvertStrategy);
        this.dbc.bindValue(WidgetProperties.enabled().observe(this.projectDirCombo), defaultLocationSelection, booleanInvertStrategy, booleanInvertStrategy);
        this.dbc.bindValue(WidgetProperties.enabled().observe(this.projectDirBrowseButton), defaultLocationSelection, booleanInvertStrategy, booleanInvertStrategy);
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
                    DialogPageSupport dialogPageSupport = DialogPageSupport.create(NewGradleProjectWizardPage.this, NewGradleProjectWizardPage.this.dbc);
                    dialogPageSupport.setValidationMessageProvider(new MessageRestoringValidationMessageProvider(ProjectWizardMessages.InfoMessage_NewGradleProjectWizardPageDefault));
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

    public ImmutableList<IWorkingSet> getSelectedWorkingSets() {
        return ImmutableList.copyOf(this.workingSetConfigurationBlock.getSelectedWorkingSets());
    }

    @Override
    protected String getPageContextInformation() {
        return ProjectWizardMessages.InfoMessage_NewGradleProjectWizardPageContext;
    }

    @Override
    public void dispose() {
        if (this.dbc != null) {
            this.dbc.dispose();
        }
        if (this.locationQueue != null && !this.locationQueue.isEmpty()) {
            getDialogSettings().put(PROJECT_LOCATION_DIALOG_SETTING_ARRAY, this.locationQueue.toArray(new String[this.locationQueue.size()]));
        }
        super.dispose();
    }

}
