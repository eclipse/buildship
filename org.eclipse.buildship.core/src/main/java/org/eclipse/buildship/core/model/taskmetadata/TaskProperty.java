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

/**
 * Create a task property of a {@link TaskType}.
 */
public class TaskProperty {
    private String name, description;

    /**
     * Creates a new instance of a {@link TaskProperty}.
     * @param name {@link String} the name of this property.
     * @param description {@link String} the description of this property.
     */
    public TaskProperty(String name, String description){
        this.name = name;
        this.description = description;
    }

    /**
     * Returns the name of the task property.
     * @return {@link String}
     */
    public String getName(){
        return name;
    }

    /**
     * Returns the description from the task property.
     * @return {@link String}
     */
    public String getDescription(){
        return description;
    }
}
