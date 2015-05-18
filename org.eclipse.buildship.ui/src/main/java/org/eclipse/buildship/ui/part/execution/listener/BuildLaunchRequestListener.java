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

package org.eclipse.buildship.ui.part.execution.listener;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.core.event.Event;
import org.eclipse.buildship.core.event.EventListener;
import org.eclipse.buildship.core.launch.ExecuteBuildLaunchRequestEvent;
import org.eclipse.buildship.ui.part.execution.ExecutionPage;
import org.eclipse.buildship.ui.part.execution.ExecutionsView;
import org.eclipse.buildship.ui.util.workbench.WorkbenchUtils;

/**
 * This listener is invoked every time a Gradle build is started.
 */
public final class BuildLaunchRequestListener implements EventListener {

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

        Display display = PlatformUI.getWorkbench().getDisplay();
        display.syncExec(new Runnable() {

            @Override
            public void run() {
                // show the executions view // todo (donat) why does this not work?
                IViewPart showView = WorkbenchUtils.showView(ExecutionsView.ID, null, IWorkbenchPage.VIEW_ACTIVATE);

                // prepare a new executions page
                if (showView instanceof ExecutionsView) {
                    ExecutionsView executionsView = (ExecutionsView) showView;
                    ExecutionPage executionPage = new ExecutionPage(executionsView.getState());
                    executionPage.setDisplayName(event.getProcessName());
                    executionsView.addPage(executionPage);
                    executionsView.setCurrentPage(executionPage);

                    // register a progress listener to receive build progress events
                    event.getBuildLaunchRequest().testProgressListeners(new ExecutionTestProgressListener(executionPage.getBuildStartedItem()));
                }
            }
        });
    }
}
