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

package org.eclipse.buildship.ui.view.executionview.listener;

import com.google.common.eventbus.Subscribe;

import org.eclipse.swt.widgets.Display;

import org.eclipse.buildship.core.event.BuildLaunchRequestEvent;
import org.eclipse.buildship.ui.view.executionview.AbstractPagePart;
import org.eclipse.buildship.ui.view.executionview.ExecutionPage;

/**
 * This listener is invoked every time a Gradle build is started.
 */
public class BuildLaunchRequestListener {


    private AbstractPagePart part;

    public BuildLaunchRequestListener(AbstractPagePart part) {
        this.part = part;
    }

    @Subscribe
    public void handleBuildlaunchRequest(final BuildLaunchRequestEvent event) {

        Display display = part.getSite().getShell().getDisplay();
        // TODO use syncExec here, because otherwise the test progress listener might fire events
        // too early. Once the Eclipse 4 IEventBroker is used we rather use IEventBroker.send than
        // IEventBroker.post to avoid this
        display.syncExec(new Runnable() {

            @Override
            public void run() {
                ExecutionPage executionPage = new ExecutionPage();
                executionPage.setDisplayName(event.getProcessName());
                part.addPage(executionPage);
                part.setCurrentPage(executionPage);

                event.getElement().testProgressListeners(new ExecutionViewTestProgressListener(executionPage.getBuildStartedItem()));
            }
        });
    }
}
