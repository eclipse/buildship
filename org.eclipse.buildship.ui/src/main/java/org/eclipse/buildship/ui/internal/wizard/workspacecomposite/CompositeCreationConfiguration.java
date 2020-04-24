/*
 * Copyright (c) 2019 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

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
		return compositeName;
	}
    
    public void setCompositeName(String compositeName) {
    	this.compositeName.setValue(compositeName);
    }
    
    public Property<List<IAdaptable>> getCompositeProjects() {
    	return compositeProjects;
    }
    
    public void setCompositeProjects(List<IAdaptable> compositeProjects) {
    	this.compositeProjects.setValue(compositeProjects);
    }
        
}
