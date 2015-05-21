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

package org.eclipse.buildship.ui.view.execution;

import com.gradleware.tooling.toolingclient.BuildLaunchRequest;

import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

import org.eclipse.buildship.ui.view.MessagePage;
import org.eclipse.buildship.ui.view.MultiPageView;
import org.eclipse.buildship.ui.view.Page;

/**
 * A view displaying the Gradle executions.
 */
public final class ExecutionsView extends MultiPageView {

    // view id declared in the plugin.xml
    public static final String ID = "org.eclipse.buildship.ui.views.executionview"; //$NON-NLS-1$

    private ExecutionsViewState state;

    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);

        // load the persisted state before we create any UI components that query for some state
        this.state = new ExecutionsViewState();
        this.state.load();
    }

    @Override
    protected Page getDefaultPage() {
        return new MessagePage(ExecutionsViewMessages.Label_No_Execution);
    }

    public void addExecutionPage(BuildLaunchRequest buildLaunchRequest, String processName) {
        ExecutionPage executionPage = new ExecutionPage(this.state, buildLaunchRequest, processName);
        addPage(executionPage);
        setCurrentPage(executionPage);
    }

    @Override
    public void dispose() {
        this.state.dispose();
        super.dispose();
    }

}
