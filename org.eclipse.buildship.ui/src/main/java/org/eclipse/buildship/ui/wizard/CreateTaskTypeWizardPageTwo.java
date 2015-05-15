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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.buildship.core.model.taskmetadata.TaskProperty;
import org.eclipse.buildship.core.model.taskmetadata.TaskType;

/**
 * Shows the second page from the {@link ExampleTaskTypeWizard},
 * where the user sets the property values from the selected task type.
 */
public class CreateTaskTypeWizardPageTwo extends WizardPage {
    private Composite composite;
    private Map<String, String> propertiesMap = new HashMap<String, String>();
	private ScrolledComposite sComposite;

    protected CreateTaskTypeWizardPageTwo() {
        super("Task Type seconed Page");
        setTitle("Set properties");
    }

    @Override
    public void createControl(Composite parent) {
        sComposite = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
        composite = new Composite(sComposite, SWT.NONE);
        sComposite.setContent(composite);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);

        sComposite.setExpandHorizontal(true);
        sComposite.setExpandVertical(true);
        sComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        setControl(sComposite);
    }

    /**
     * Sets the task type to get the containing properties and update the composite.
     * @param taskType {@link TaskType}
     */
    public void setTaskType(TaskType taskType) {
        List<TaskProperty> taskProperties = taskType.getTaskProperties();
        updateComposite(taskProperties);

    }

    /**
     * Sets the given properties to the page.
     * @param taskProperties
     */
    private void updateComposite(List<TaskProperty> taskProperties) {
        for (final TaskProperty taskProperty : taskProperties) {
            Label label = new Label(composite, SWT.NONE);
            label.setText(taskProperty.getName());
            label.setToolTipText(taskProperty.getDescription());

            final Text propertyValue = new Text(composite, SWT.BORDER);
            propertyValue.setMessage("Set the value of this property");
            GridDataFactory.fillDefaults().grab(true, false).applyTo(propertyValue);
            propertyValue.addKeyListener(new KeyAdapter(){
                @Override
                public void keyReleased(KeyEvent e) {
                    addProperty(taskProperty.getName(), propertyValue.getText());
                }

             });

        }
        sComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        composite.layout();
	}

    private void addProperty(String propertyName, String propertyValue) {
        propertiesMap.put(propertyName, propertyValue);
    }

    /**
     * Returns the property name and the entered value.
     * @return {@link Map} contains the task property name and its value.
     */
    public Map<String, String> getFilledProperties(){
        filterEmptyValues();
        return propertiesMap;
    }
    /**
     * Filters all empty keys.
     */
    private void filterEmptyValues() {
        for (Iterator<Map.Entry<String, String>> iterator = propertiesMap.entrySet().iterator(); iterator.hasNext();) {
        Map.Entry<String, String> entry = iterator.next();
            if(entry.getValue().isEmpty()){
                iterator.remove();
            }
        }
    }

}