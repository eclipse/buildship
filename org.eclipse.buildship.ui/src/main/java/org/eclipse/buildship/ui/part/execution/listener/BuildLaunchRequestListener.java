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

import com.google.common.base.Preconditions;
import org.eclipse.buildship.core.event.Event;
import org.eclipse.buildship.core.event.EventListener;
import org.eclipse.buildship.core.launch.ExecuteBuildLaunchRequestEvent;
import org.eclipse.buildship.ui.part.execution.ExecutionPage;
import org.eclipse.buildship.ui.part.execution.ExecutionsView;
import org.eclipse.buildship.ui.util.workbench.WorkbenchUtils;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;

/**
 * This listener is invoked every time a Gradle build is started.
 */
public final class BuildLaunchRequestListener implements EventListener {

    private final ExecutionsView executionsView;

    public BuildLaunchRequestListener(ExecutionsView executionsView) {
        this.executionsView = Preconditions.checkNotNull(executionsView);
    }

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

        Display display = this.executionsView.getSite().getShell().getDisplay();
        display.syncExec(new Runnable() {

            @Override
            public void run() {
                // show the executions view // todo (donat) why does this not work?
                WorkbenchUtils.showView(ExecutionsView.ID, null, IWorkbenchPage.VIEW_ACTIVATE);

                // prepare a new executions page
                ExecutionsView executionsView = BuildLaunchRequestListener.this.executionsView;
                ExecutionPage executionPage = new ExecutionPage(executionsView.getState());
                executionPage.setDisplayName(event.getProcessName());
                executionsView.addPage(executionPage);
                executionsView.setCurrentPage(executionPage);

                // register a progress listener to receive build progress events
                event.getBuildLaunchRequest().testProgressListeners(new ExecutionTestProgressListener(executionPage.getBuildStartedItem()));
            }
        });
    }
}
