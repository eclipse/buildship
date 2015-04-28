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
package org.eclipse.buildship.core.model.taskmetadata;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the task type fields.
 */
public class TaskType {
    private String className;
    private List<TaskProperty> taskProperties;

    /**
     * Sets the class name of the task type.
     * @param className {@link String}
     */
    public TaskType(String className){
        this.className = className;	
        taskProperties = new ArrayList<TaskProperty>();
    }

    /**
     * Returns the {@link TaskProperty}s from the {@link TaskType}.
     * @return {@link List} contains {@link TaskProperty}
     */
    public List<TaskProperty> getTaskProperties() {
        return taskProperties;
    }

    /**
     * Creates a new {@link TaskProperty} and add it to the list.
     * @param name {@link String} the task property name.
     * @param description {@link String} the description of the property.
     */
    public void addTaskProperty(String name, String description){
    	addTaskProperty(new TaskProperty(name, description));
    }

    /**
     * Adds a {@link TaskProperty} to the task type.
     * @param property {@link TaskProperty}
     */
    public void addTaskProperty(TaskProperty property){
        taskProperties.add(property);
    }

    /**
     * Returns the class name from the task type
     * @return {@link String}
     */
    public String getClassName(){
        return className;
    }

}