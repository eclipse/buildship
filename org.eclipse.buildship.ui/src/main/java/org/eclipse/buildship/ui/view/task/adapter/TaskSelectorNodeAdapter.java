/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.view.task.adapter;

import com.google.common.base.Preconditions;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import org.eclipse.buildship.core.omnimodel.OmniTaskSelector;
import org.eclipse.buildship.ui.view.task.TaskSelectorNode;

/**
 * Adapts a {@link TaskSelectorNode} instance to a {@link IPropertySource} instance.
 */
final class TaskSelectorNodeAdapter implements IPropertySource {

    private static final String PROPERTY_NAME = "task.name";
    private static final String PROPERTY_DESCRIPTION = "task.description";
    private static final String PROPERTY_PROJECT_PATH = "task.projectPath";
    private static final String PROPERTY_PUBLIC = "task.public";
    private static final String PROPERTY_TYPE = "task.type";

    private final OmniTaskSelector taskSelector;

    TaskSelectorNodeAdapter(TaskSelectorNode taskNode) {
        this.taskSelector = Preconditions.checkNotNull(taskNode).getTaskSelector();
    }

    @Override
    public Object getEditableValue() {
        return this;
    }

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        // @formatter:off
        return new IPropertyDescriptor[]{
                new PropertyDescriptor(PROPERTY_NAME, "Name"),
                new PropertyDescriptor(PROPERTY_DESCRIPTION, "Description"),
                new PropertyDescriptor(PROPERTY_PROJECT_PATH, "Project Path"),
                new PropertyDescriptor(PROPERTY_PUBLIC, "Public"),
                new PropertyDescriptor(PROPERTY_TYPE, "Type"),
        };
        // @formatter:on
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (id.equals(PROPERTY_NAME)) {
            return this.taskSelector.getName();
        } else if (id.equals(PROPERTY_DESCRIPTION)) {
            return this.taskSelector.getDescription();
        } else if (id.equals(PROPERTY_PROJECT_PATH)) {
            return this.taskSelector.getProjectPath().getPath();
        } else if (id.equals(PROPERTY_PUBLIC)) {
            return this.taskSelector.isPublic();
        } else if (id.equals(PROPERTY_TYPE)) {
            return "Gradle Task Selector";
        } else {
            throw new IllegalStateException("Unsupported task selector property: " + id);
        }
    }

    @Override
    public boolean isPropertySet(Object id) {
        return false;
    }

    @Override
    public void resetPropertyValue(Object id) {
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
    }

}
