/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 465728
 */

package org.eclipse.buildship.ui.wizard.task.renderer;

import org.eclipse.core.runtime.IAdapterFactory;

import org.eclipse.buildship.core.model.taskmetadata.TaskPropertyTypes;

/**
 * {@link IAdapterFactory} for certain
 * {@link org.eclipse.buildship.core.model.taskmetadata.TaskPropertyType}.
 *
 */
public class PropertyRendererAdapterFactory implements IAdapterFactory {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (TaskPropertyTypes.Boolean.equals(adaptableObject)) {
            return new TriStatePropertyRenderer();
        }
        return null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Class[] getAdapterList() {
        return new Class[] { PropertyRenderer.class };
    }

}
