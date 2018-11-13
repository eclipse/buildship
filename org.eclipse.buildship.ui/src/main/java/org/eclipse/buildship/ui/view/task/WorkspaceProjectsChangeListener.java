/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.view.task;

import com.google.common.base.Preconditions;

import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;

import org.eclipse.buildship.core.event.Event;
import org.eclipse.buildship.core.event.EventListener;
import org.eclipse.buildship.core.workspace.GradleNatureAddedEvent;
import org.eclipse.buildship.core.workspace.ProjectClosedEvent;
import org.eclipse.buildship.core.workspace.ProjectCreatedEvent;
import org.eclipse.buildship.core.workspace.ProjectDeletedEvent;
import org.eclipse.buildship.core.workspace.ProjectMovedEvent;
import org.eclipse.buildship.core.workspace.ProjectOpenedEvent;

/**
 * Tracks the creation/deletion/closed/opened of projects in the workspace and updates the {@link TaskView}
 * accordingly.
 * <p>
 * Every time a project is added or removed from the workspace or change the state of the project to open or
 * closed, the listener updates the content of the task view.
 */
public final class WorkspaceProjectsChangeListener implements EventListener {

    private final TaskView taskView;

    public WorkspaceProjectsChangeListener(TaskView taskView) {
        this.taskView = Preconditions.checkNotNull(taskView);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof GradleNatureAddedEvent
                || event instanceof ProjectCreatedEvent
                || event instanceof ProjectDeletedEvent
                || event instanceof ProjectClosedEvent
                || event instanceof ProjectOpenedEvent
                || event instanceof ProjectMovedEvent) {
            this.taskView.reload(FetchStrategy.LOAD_IF_NOT_CACHED);
        }
    }
}
