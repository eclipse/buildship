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

package org.eclipse.buildship.core.projectimport.internal;

import com.google.common.base.Preconditions;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.projectimport.ProjectCreatedEvent;

/**
 * This event is fired, when a new {@link IProject} is created by the ProjectImportJob.
 *
 * @see org.eclipse.buildship.core.projectimport.ProjectImportJob
 */
public class DefaultProjectCreatedEvent implements ProjectCreatedEvent {

    private IProject project;

    public DefaultProjectCreatedEvent(IProject project) {
        this.project = Preconditions.checkNotNull(project);
    }

    @Override
    public IProject getProject() {
        return this.project;
    }

}
