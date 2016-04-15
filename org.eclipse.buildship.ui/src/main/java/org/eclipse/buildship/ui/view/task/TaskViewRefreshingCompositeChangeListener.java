/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
