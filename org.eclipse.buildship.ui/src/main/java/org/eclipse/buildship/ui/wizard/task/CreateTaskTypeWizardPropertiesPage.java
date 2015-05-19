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

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.buildship.core.model.taskmetadata.TaskProperty;
import org.eclipse.buildship.core.model.taskmetadata.TaskType;
import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;

/**
 * Shows the second page from the {@link NewTaskWizard}, where the user sets the property values
 * from the selected task type.
 */
public class CreateTaskTypeWizardPropertiesPage extends WizardPage {

    private TaskCreationModel taskCreationModel;

    private Composite composite;
    private ScrolledComposite sComposite;

    private DataBindingContext dbc;

    protected CreateTaskTypeWizardPropertiesPage(TaskCreationModel taskCreationModel) {
        super("Task Type Properties Page");
        this.taskCreationModel = taskCreationModel;
        setTitle("Set Properties");
        setMessage("You can directly configure the properties of the previously chosen task type.");
        setImageDescriptor(PluginImages.IMPORT_WIZARD.withState(ImageState.ENABLED).getImageDescriptor());
    }

    @Override
    public void createControl(Composite parent) {
        sComposite = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
        composite = new Composite(sComposite, SWT.NONE);
        sComposite.setContent(composite);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);

        sComposite.setExpandHorizontal(true);
        sComposite.setExpandVertical(true);

        setControl(sComposite);
    }

    public void showTaskTypeProperties() {
        // dispose old properties
        disposeCompositeChildren(composite);
        TaskType taskType = taskCreationModel.getTaskType();
        if (taskType != null) {
            dbc = new DataBindingContext();
            StyledText taskTypeLabel = new StyledText(composite, SWT.READ_ONLY);
            taskTypeLabel.setBackgroundMode(SWT.INHERIT_FORCE);
            taskTypeLabel.setBackground(composite.getBackground());
            taskTypeLabel.setText("These are the properties of the " + taskType.getName() + " Task Type:");
            taskTypeLabel.setStyleRange(new StyleRange(32, taskType.getName().length(), null, null, SWT.BOLD));
            taskTypeLabel.setToolTipText(taskType.getDescription());
            GridDataFactory.swtDefaults().span(2, 1).applyTo(taskTypeLabel);
            for (TaskProperty taskProperty : taskType.getTaskProperties()) {
                Label label = new Label(composite, SWT.NONE);
                label.setText(taskProperty.getName());
                label.setToolTipText(taskProperty.getDescription());

                final Text propertyValue = new Text(composite, SWT.BORDER);
                propertyValue.setMessage("Set the value of this property");
                GridDataFactory.fillDefaults().grab(true, false).applyTo(propertyValue);

                IObservableValue taskPropertyModel = Observables.observeMapEntry(taskCreationModel.getTaskPropertyValues(), taskProperty, TaskProperty.class);
                ISWTObservableValue propertyTextTarget = WidgetProperties.text(SWT.Modify).observe(propertyValue);

                dbc.bindValue(propertyTextTarget, taskPropertyModel);
            }
            sComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            composite.layout();
        }
    }

    private void disposeCompositeChildren(Composite parent) {
        Control[] children = parent.getChildren();
        for (Control control : children) {
            control.dispose();
        }
    }

    @Override
    public void dispose() {
        if (dbc != null) {
            dbc.dispose();
        }
        super.dispose();
    }
}
