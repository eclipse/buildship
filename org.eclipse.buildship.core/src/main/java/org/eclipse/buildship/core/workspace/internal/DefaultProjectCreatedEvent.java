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

package org.eclipse.buildship.core.workspace.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.eclipse.buildship.core.workspace.ProjectCreatedEvent;
import org.eclipse.core.resources.IProject;

import java.util.List;

/**
 * Default implementation of {@link ProjectCreatedEvent}.
 */
public final class DefaultProjectCreatedEvent implements ProjectCreatedEvent {

    private final IProject project;
    private final ImmutableList<String> workingSets;

    public DefaultProjectCreatedEvent(IProject project, List<String> workingSets) {
        this.project = Preconditions.checkNotNull(project);
        this.workingSets = ImmutableList.copyOf(workingSets);
    }

    @Override
    public IProject getProject() {
        return this.project;
    }

    @Override
    public ImmutableList<String> getWorkingSets() {
        return this.workingSets;
    }

}
