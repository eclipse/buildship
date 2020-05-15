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

import java.util.List;

import org.eclipse.buildship.core.internal.util.binding.Property;
import org.eclipse.core.runtime.IAdaptable;

import com.google.common.base.Preconditions;

public class CompositeCreationConfiguration {

    private final Property<String> compositeName;
    private final Property<List<IAdaptable>> compositeProjects;

    public CompositeCreationConfiguration(Property<String> compositeName, Property<List<IAdaptable>> compositeProjects) {
        this.compositeName = Preconditions.checkNotNull(compositeName);
        this.compositeProjects = Preconditions.checkNotNull(compositeProjects);
    }

    public Property<String> getCompositeName() {
        return this.compositeName;
    }

    public void setCompositeName(String compositeName) {
        this.compositeName.setValue(compositeName);
    }

    public Property<List<IAdaptable>> getCompositeProjects() {
        return this.compositeProjects;
    }

    public void setCompositeProjects(List<IAdaptable> compositeProjects) {
        this.compositeProjects.setValue(compositeProjects);
    }

}
