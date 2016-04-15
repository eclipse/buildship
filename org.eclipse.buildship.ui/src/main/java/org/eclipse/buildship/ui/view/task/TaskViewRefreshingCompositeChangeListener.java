/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.buildship.ui.view.task;

import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;

import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.core.event.Event;
import org.eclipse.buildship.core.event.EventListener;
import org.eclipse.buildship.core.workspace.CompositeBuildSynchronizedEvent;

/**
 * Refreshes the task view whenever the workspace is synchronized with Gradle.
 *
 * @author Stefan Oehme
 *
 */
public class TaskViewRefreshingCompositeChangeListener implements EventListener {

    private final TaskView view;

    public TaskViewRefreshingCompositeChangeListener(TaskView view) {
        this.view = view;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof CompositeBuildSynchronizedEvent) {
            reloadTaskView();
        }

    }

    private void reloadTaskView() {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                TaskViewRefreshingCompositeChangeListener.this.view.reload(FetchStrategy.LOAD_IF_NOT_CACHED);
            }

        });
    }

}
