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

import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This service offers Gradle specific operations for projects.
 *
 */
public interface ProjectService {

    void convertToGradleProject(IProgressMonitor progressMonitor, GradleRunConfigurationAttributes configurationAttributes, IProject project) throws Exception;
}
