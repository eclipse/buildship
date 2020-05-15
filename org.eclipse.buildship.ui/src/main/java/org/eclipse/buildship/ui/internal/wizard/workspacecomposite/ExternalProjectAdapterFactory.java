/*******************************************************************************
 * Copyright (c) 2020 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.wizard.workspacecomposite;

import org.eclipse.core.runtime.IAdapterFactory;

public class ExternalProjectAdapterFactory implements IAdapterFactory {

    @Override
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adaptableObject instanceof ExternalGradleProject){
            return new ExternalGradleProjectAdapter((ExternalGradleProject) adaptableObject);
        }
        return null;
    }

    @Override
    public Class<?>[] getAdapterList() {
        // TODO Auto-generated method stub
        return null;
    }

}
