/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Denis Zygann <d.zygann@web.de> - Bug 465728
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 465728
 */

package org.eclipse.buildship.ui.wizard.task;

import java.util.List;

import org.eclipse.core.databinding.AggregateValidationStatus;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.dialog.DialogPageSupport;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.buildship.core.model.taskmetadata.TaskType;
import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.buildship.ui.databinding.validators.TaskNameValidator;

/**
 * Shows the first page of the {@link NewGradleTaskWizard}, where the user sets the task type and the name
 * for the function.
 */
public class CreateTaskTypeWizardMainPage extends WizardPage {

    private TaskCreationModel taskCreationModel;

    private IObservableList taskTypes;

    private Text taskNameText;

    private ComboViewer comboViewer;

    private DataBindingContext dbc;

    /**
     * The constructor sets the {@link TaskType}s and the title from the page.
     *
     * @param taskTypes {@link List} contains the available {@link TaskType}s
     * @param taskCreationModel
     */
    protected CreateTaskTypeWizardMainPage(List<TaskType> taskTypes, TaskCreationModel taskCreationModel) {
        super("Task name and type");
        this.taskCreationModel = taskCreationModel;

        this.taskTypes = new WritableList(taskTypes, TaskType.class);
        // also allow to create task without task type
        this.taskTypes.add(TaskType.DEFAULT_TASK_TYPE);

        setTitle("Set name and task type");
        setImageDescriptor(PluginImages.IMPORT_WIZARD.withState(ImageState.ENABLED).getImageDescriptor());
        setPageComplete(false);
    }

    @Override
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.None);

        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);

        Label taskNameLabel = new Label(composite, SWT.None);
        taskNameLabel.setText("Task Name");

        taskNameText = new Text(composite, SWT.BORDER);
        taskNameText.setMessage("Set the name of your task");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(taskNameText);

        Label taskTypesLabel = new Label(composite, SWT.None);
        taskTypesLabel.setText("Task Type");

        comboViewer = new ComboViewer(composite, SWT.READ_ONLY);

        bindUI();

        createPageCompleteObservable();

        setControl(composite);
    }

    private void bindUI() {
        ViewerSupport.bind(comboViewer, taskTypes, PojoProperties.value(TaskType.class, "name")); //$NON-NLS-1$

        dbc = new DataBindingContext();

        ISWTObservableValue taskNameTarget = WidgetProperties.text(SWT.Modify).observe(taskNameText);
        IObservableValue taskNameModel = BeanProperties.value(TaskCreationModel.class, TaskCreationModel.FIELD_TASKNAME, String.class).observe(taskCreationModel);

        UpdateValueStrategy nonEmptyCheckUpdateStrategy = new UpdateValueStrategy();
        nonEmptyCheckUpdateStrategy.setAfterGetValidator(new TaskNameValidator());

        Binding taskNameBinding = dbc.bindValue(taskNameTarget, taskNameModel, nonEmptyCheckUpdateStrategy, nonEmptyCheckUpdateStrategy);
        // Add ControlDecorationSupport, when the first change occurs so that warnings are not shown
        // immediately
        addControlDecorationSupport(taskNameBinding);

        IViewerObservableValue taskTypeTarget = ViewerProperties.singleSelection().observe(comboViewer);
        IObservableValue taskTypeModel = BeanProperties.value(TaskCreationModel.class, TaskCreationModel.FIELD_TASKTYPE, TaskType.class).observe(taskCreationModel);

        dbc.bindValue(taskTypeTarget, taskTypeModel);
    }

    private void addControlDecorationSupport(final Binding taskNameBinding) {
        taskNameBinding.getTarget().addChangeListener(new IChangeListener() {

            private boolean isControlDecorationSupportAdded;

            @Override
            public void handleChange(ChangeEvent event) {
                if (!isControlDecorationSupportAdded) {
                    isControlDecorationSupportAdded = true;
                    // show validation status at the widget itself
                    ControlDecorationSupport.create(taskNameBinding, SWT.LEFT | SWT.TOP);
                    // show validation status as Wizard message
                    DialogPageSupport.create(CreateTaskTypeWizardMainPage.this, dbc);
                }
            }
        });
    }

    private void createPageCompleteObservable() {
        AggregateValidationStatus aggregateValidationStatus = new AggregateValidationStatus(dbc, AggregateValidationStatus.MAX_SEVERITY);
        aggregateValidationStatus.addValueChangeListener(new IValueChangeListener() {

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
    public boolean canFlipToNextPage() {
        return super.canFlipToNextPage() && !comboViewer.getSelection().isEmpty()
                && !(TaskType.DEFAULT_TASK_TYPE.equals(((IStructuredSelection) comboViewer.getSelection()).getFirstElement()));
    }

    @Override
    public void dispose() {
        if (dbc != null) {
            dbc.dispose();
        }
        super.dispose();
    }

}
