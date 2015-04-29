/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Denis Zygann <d.zygann@web.de> - Bug 465728 
 */
package org.eclipse.buildship.ui.wizard;

import java.util.List;

import org.eclipse.buildship.core.model.taskmetadata.TaskType;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Shows the first page of the {@link ExampleTaskTypeWizard}, where the user
 * sets the task type and the name for the function.
 */
public class CreateTaskTypeWizardPageOne extends WizardPage {

    private IObservableList taskTypes;
    private String taskName = "";
    private TaskType selectedTaskType;

    /**
     * The constructor sets the {@link TaskType}s and the title from the page.
     * 
     * @param taskTypes
     *            {@link List} contains the available {@link TaskType}s
     */
    protected CreateTaskTypeWizardPageOne(List<TaskType> taskTypes) {
        super("Task type first page");

        this.taskTypes = new WritableList(taskTypes, TaskType.class);
        setTitle("Set name and task type");
    }

    @Override
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.None);
        setControl(composite);

        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);

        Label taskNameLabel = new Label(composite, SWT.None);
        taskNameLabel.setText("Taskname:");

        Text taskNameText = new Text(composite, SWT.BORDER);
        taskNameText.setMessage("Set the name of your task");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(taskNameText);

        taskNameText.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                taskName = taskNameText.getText();
                updatePageComplete();
            }
        });

        Label taskTypesLabel = new Label(composite, SWT.None);
        taskTypesLabel.setText("Task types:");

        ComboViewer comboViewer = new ComboViewer(composite, SWT.READ_ONLY);
        comboViewer.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                if (element instanceof TaskType) {
                    TaskType taskType = (TaskType) element;
                    return taskType.getClassName();
                }
                return super.getText(element);
            }
        });
        comboViewer.setContentProvider(ArrayContentProvider.getInstance());
        comboViewer.setInput(taskTypes);
        comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                ISelection selection = event.getSelection();
                if (selection instanceof IStructuredSelection) {
                    IStructuredSelection sel = (IStructuredSelection) selection;
                    setSelectedTaskType((TaskType) sel.getFirstElement());
                }
                updatePageComplete();
            }
        });
        updatePageComplete();
    }

    /**
     * Checks, if the finish and next buttons could be available and sets the
     * {@link TaskType} to the second page.
     */
    private void updatePageComplete() {
        setPageComplete(false);
        if (!getTaskName().isEmpty() && getSelectedTaskType() != null) {
            setPageComplete(true);
            IWizardPage nextPage = getNextPage();
            if (nextPage instanceof CreateTaskTypeWizardPageTwo) {
                CreateTaskTypeWizardPageTwo propertiesPage = (CreateTaskTypeWizardPageTwo) nextPage;
                propertiesPage.setTaskType(getSelectedTaskType());
            }
        }
    }

    /**
     * Returns the selected {@link TaskType}.
     * 
     * @return {@link TaskType}
     */
    public TaskType getSelectedTaskType() {
        return selectedTaskType;
    }

    /**
     * Sets the selected {@link TaskType}.
     * 
     * @param currentTaskType
     *            {@link TaskType}
     */
    private void setSelectedTaskType(TaskType currentTaskType) {
        this.selectedTaskType = currentTaskType;
    }

    /**
     * Returns the given name.
     * 
     * @return {@link String} the task name
     */
    public String getTaskName() {
        return taskName;
    }

}
