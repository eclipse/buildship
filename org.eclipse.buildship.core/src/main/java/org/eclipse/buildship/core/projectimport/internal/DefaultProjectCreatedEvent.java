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
import org.eclipse.buildship.core.projectimport.ProjectCreatedEvent;
import org.eclipse.core.resources.IProject;

/**
 * Default implementation of {@link org.eclipse.buildship.core.projectimport.ProjectCreatedEvent}.
 */
public final class DefaultProjectCreatedEvent implements ProjectCreatedEvent {

    private final IProject project;

    public DefaultProjectCreatedEvent(IProject project) {
        this.project = Preconditions.checkNotNull(project);
    }

    @Override
    public IProject getProject() {
        return this.project;
    }

}
