/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view.task;

import com.google.common.base.Preconditions;

import org.eclipse.buildship.core.internal.event.Event;
import org.eclipse.buildship.core.internal.event.EventListener;
import org.eclipse.buildship.core.internal.workspace.FetchStrategy;
import org.eclipse.buildship.core.internal.workspace.GradleNatureAddedEvent;
import org.eclipse.buildship.core.internal.workspace.ProjectClosedEvent;
import org.eclipse.buildship.core.internal.workspace.ProjectCreatedEvent;
import org.eclipse.buildship.core.internal.workspace.ProjectDeletedEvent;
import org.eclipse.buildship.core.internal.workspace.ProjectMovedEvent;
import org.eclipse.buildship.core.internal.workspace.ProjectOpenedEvent;

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
