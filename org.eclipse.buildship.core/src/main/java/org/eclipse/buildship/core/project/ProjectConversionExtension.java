/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.project;

import org.eclipse.buildship.core.gradle.model.GradleModel;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This interface is used for the
 * org.eclipse.buildship.core.gradleconversionextension extension point so that
 * other projects can contribute to the {@link GradleModel} in order to convert
 * the given {@link IProject} to a Gradle project.
 */
public interface ProjectConversionExtension {

	void addProjectSpecificInformation(IProgressMonitor monitor, IProject project, GradleModel model) throws Exception;
}
