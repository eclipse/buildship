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

import org.eclipse.buildship.core.model.taskmetadata.TaskProperty;
import org.eclipse.buildship.core.model.taskmetadata.TaskType;
import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.buildship.ui.wizard.task.renderer.PropertyRenderer;
import org.eclipse.buildship.ui.wizard.task.renderer.StringPropertyRenderer;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.Platform;
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

/**
 * Shows the second page from the {@link NewGradleTaskWizard}, where the user sets the property values
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
        setImageDescriptor(PluginImages.WIZARD.withState(ImageState.ENABLED).getImageDescriptor());
    }

    @Override
    public void createControl(Composite parent) {
        this.sComposite = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
        this.composite = new Composite(this.sComposite, SWT.NONE);
        this.sComposite.setContent(this.composite);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(this.composite);

        this.sComposite.setExpandHorizontal(true);
        this.sComposite.setExpandVertical(true);

        setControl(this.sComposite);
    }

    public void showTaskTypeProperties() {
        // dispose old properties
        disposeCompositeChildren(this.composite);
        TaskType taskType = this.taskCreationModel.getTaskType();
        if (taskType != null) {
            this.dbc = new DataBindingContext();
            StyledText taskTypeLabel = new StyledText(this.composite, SWT.READ_ONLY);
            taskTypeLabel.setBackgroundMode(SWT.INHERIT_FORCE);
            taskTypeLabel.setBackground(this.composite.getBackground());
            taskTypeLabel.setText("These are the properties of the " + taskType.getName() + " Task Type");
            taskTypeLabel.setStyleRange(new StyleRange(32, taskType.getName().length(), null, null, SWT.BOLD));
            taskTypeLabel.setToolTipText(taskType.getDescription());
            GridDataFactory.swtDefaults().span(2, 1).applyTo(taskTypeLabel);
            boolean firstTextHasFocus = false;
            for (TaskProperty taskProperty : taskType.getTaskProperties()) {
                Label label = new Label(this.composite, SWT.NONE);
                label.setText(taskProperty.getName());
                label.setToolTipText(taskProperty.getDescription());

                PropertyRenderer propertyRenderer = Platform.getAdapterManager().getAdapter(taskProperty.getTaskPropertyType(), PropertyRenderer.class);
                if (null == propertyRenderer) {
                    // use StringPropertyRenderer as default
                    propertyRenderer = new StringPropertyRenderer();
                }
                propertyRenderer.createControl(this.composite);

                // ensure that the first Text control gains the focus
                if (!firstTextHasFocus) {
                    propertyRenderer.getControl().setFocus();
                    firstTextHasFocus = true;
                }

                IObservableValue taskPropertyModel = Observables.observeMapEntry(this.taskCreationModel.getTaskPropertyValues(), taskProperty, TaskProperty.class);

                this.dbc.bindValue(propertyRenderer.getObservable(), taskPropertyModel, propertyRenderer.getTargetUpdateValueStrategy(),
                        propertyRenderer.getModelUpdateValueStrategy());
            }
            this.sComposite.setMinSize(this.composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            this.composite.layout();
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
        if (this.dbc != null) {
            this.dbc.dispose();
        }
        super.dispose();
    }
}
