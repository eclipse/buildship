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

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.core.event.Event;
import org.eclipse.buildship.core.event.EventListener;
import org.eclipse.buildship.core.launch.ExecuteBuildLaunchRequestEvent;
import org.eclipse.buildship.ui.util.workbench.WorkbenchUtils;

/**
 * {@link EventListener} implementation showing/activating the Executions View when a new Gradle build is executed and the
 * {@link org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes#isShowExecutionView()} setting is enabled.
 * <p/>
 * The listener implementation is necessary since opening a view is a UI-related task and the execution is performed in the core component.
 */
public final class ExecutionShowingBuildLaunchRequestListener implements EventListener {

    @Override
    public void onEvent(Event event) {
        if (event instanceof ExecuteBuildLaunchRequestEvent) {
            handleBuildLaunchRequest((ExecuteBuildLaunchRequestEvent) event);
        }
    }

    private void handleBuildLaunchRequest(final ExecuteBuildLaunchRequestEvent event) {
        // call synchronously to make sure we do not miss any progress events
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

            @Override
            public void run() {
                // each time a new build is launched, increment the secondary id to show its execution in a new Executions View
                ExecutionsView view = WorkbenchUtils.showView(ExecutionsView.ID, String.valueOf(0),
                        event.getRunConfigurationAttributes().isShowExecutionView() ? IWorkbenchPage.VIEW_ACTIVATE : IWorkbenchPage.VIEW_VISIBLE);

                // show the launched build in the newly added Executions View
                view.addExecutionPage(event.getBuildLaunchRequest(), event.getProcessName());
            }
        });
    }

}
