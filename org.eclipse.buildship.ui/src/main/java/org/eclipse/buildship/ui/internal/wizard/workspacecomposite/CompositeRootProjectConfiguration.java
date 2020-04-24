/*
 * Copyright (c) 2019 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.internal.wizard.workspacecomposite;

import java.io.File;

import org.eclipse.buildship.core.internal.util.binding.Property;
import org.eclipse.buildship.core.internal.util.binding.Validators;

import com.google.common.base.Preconditions;

public class CompositeRootProjectConfiguration {
	
	private final Property<Boolean> useCompositeRoot;
	private final Property<File> rootProject;
	
	public CompositeRootProjectConfiguration(){
		this(Property.create(Validators.<Boolean>noOp()), Property.create(Validators.<File>noOp()));
	}
	
	public CompositeRootProjectConfiguration(Property<Boolean> useCompositeRoot, Property<File> rootProject) {
		this.useCompositeRoot = Preconditions.checkNotNull(useCompositeRoot);
		this.rootProject = Preconditions.checkNotNull(rootProject);
	}

	public Property<Boolean> getUseCompositeRoot() {
		return useCompositeRoot;
	}
	
	public void setUseCompositeRoot(Boolean useCompositeRoot) {
		this.useCompositeRoot.setValue(useCompositeRoot);
	}

	public Property<File> getRootProject() {
		return rootProject;
	}
	
	public void setRootProject(File rootProject) {
		this.rootProject.setValue(rootProject);
	}
	

}
