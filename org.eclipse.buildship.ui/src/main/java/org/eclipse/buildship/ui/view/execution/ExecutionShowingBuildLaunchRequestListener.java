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

package org.eclipse.buildship.ui.view.execution;

import org.eclipse.buildship.core.event.Event;
import org.eclipse.buildship.core.event.EventListener;
import org.eclipse.buildship.core.launch.ExecuteBuildLaunchRequestEvent;
import org.eclipse.buildship.ui.part.execution.ExecutionsView;
import org.eclipse.buildship.ui.part.execution.ExecutionsViewState;
import org.eclipse.buildship.ui.part.execution.listener.ExecutionTestProgressListener;
import org.eclipse.buildship.ui.util.workbench.WorkbenchUtils;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * This listener is invoked every time a Gradle build is started.
 */
public final class ExecutionShowingBuildLaunchRequestListener implements EventListener {

    private final AtomicInteger counter = new AtomicInteger(1);

    @Override
    public void onEvent(Event event) {
        if (event instanceof ExecuteBuildLaunchRequestEvent) {
            handleBuildLaunchRequest((ExecuteBuildLaunchRequestEvent) event);
        }
    }

    private void handleBuildLaunchRequest(final ExecuteBuildLaunchRequestEvent event) {
        // only attach a progress listener to the build launch request if the
        // run configuration has the build progress visualization flag enabled
        if (!event.getRunConfigurationAttributes().isShowExecutionView()) {
            return;
        }

        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

            @Override
            public void run() {
                // each time a new build is launched, increment the counter to show its progress in a new Executions View
                String secondaryId = String.valueOf(ExecutionShowingBuildLaunchRequestListener.this.counter.getAndIncrement());
                org.eclipse.buildship.ui.view.execution.ExecutionsView view = WorkbenchUtils.showView(ExecutionsView.ID, secondaryId, IWorkbenchPage.VIEW_CREATE);

                ExecutionPage executionPage = new ExecutionPage(new ExecutionsViewState());
                executionPage.createPage(view.getControl());

                // register a progress listener to receive build progress events
                event.getBuildLaunchRequest().typedProgressListeners(new ExecutionTestProgressListener(executionPage.getBuildStartedItem()));
            }
        });
    }

}
