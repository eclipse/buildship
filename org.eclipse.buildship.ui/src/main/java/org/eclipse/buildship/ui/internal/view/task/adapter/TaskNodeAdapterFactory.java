/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view.task.adapter;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.views.properties.IPropertySource;

import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.ui.internal.view.task.ProjectTaskNode;
import org.eclipse.buildship.ui.internal.view.task.TaskNode;
import org.eclipse.buildship.ui.internal.view.task.TaskSelectorNode;

/**
 * Adapts {@link TaskNode} instances to {@link IPropertySource} instances.
 * <p/>
 * By adapting to {@link IPropertySource} instances, the Eclipse Properties View can automatically
 * display the properties of a task node upon selection.
 * <p/>
 * The adapter is registered with the Eclipse platform via the {@code org.eclipse.core.runtime.adapters} extension point.
 * <p/>
 * Description on the adapters is available at
 * <a href= "http://www.programcreek.com/2012/01/decipher-eclipse-architecture-iadaptable-part-1-brief-introduction">
 *     http://www.programcreek.com/2012/01/decipher-eclipse-architecture-iadaptable-part-1-brief-introduction</a>.
 */
@SuppressWarnings({ "unchecked", "rawtypes" }) // Eclipse Mars M6 introduced type parameters on the IAdapterFactory interface
public final class TaskNodeAdapterFactory implements IAdapterFactory {

    @Override
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adapterType == IPropertySource.class && adaptableObject instanceof TaskNode) {
            return createTaskNodeAdapter((TaskNode) adaptableObject);
        } else {
            return null;
        }
    }

    private IPropertySource createTaskNodeAdapter(TaskNode adaptableObject) {
        if (adaptableObject instanceof ProjectTaskNode) {
            return new ProjectTaskNodeAdapter((ProjectTaskNode) adaptableObject);
        } else if (adaptableObject instanceof TaskSelectorNode) {
            return new TaskSelectorNodeAdapter((TaskSelectorNode) adaptableObject);
        } else {
            throw new GradlePluginsRuntimeException("Unknown task node type: " + adaptableObject.getClass());
        }
    }

    @Override
    public Class[] getAdapterList() {
        return new Class[]{IPropertySource.class};
    }

}
