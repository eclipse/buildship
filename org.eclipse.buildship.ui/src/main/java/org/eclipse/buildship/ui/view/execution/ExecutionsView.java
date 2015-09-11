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

import com.gradleware.tooling.toolingclient.Request;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

import org.eclipse.buildship.core.console.ProcessDescription;
import org.eclipse.buildship.ui.view.MessagePage;
import org.eclipse.buildship.ui.view.MultiPageView;
import org.eclipse.buildship.ui.view.Page;
import org.eclipse.buildship.ui.view.SwitchToNextPageAction;

/**
 * A view displaying the Gradle executions.
 */
public final class ExecutionsView extends MultiPageView {

    // view id declared in the plugin.xml
    public static final String ID = "org.eclipse.buildship.ui.views.executionview"; //$NON-NLS-1$

    private ExecutionViewState state;
    private IContributionItem switchPagesAction;

    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);

        // load the persisted state before we create any UI components that query for some state
        this.state = new ExecutionViewState();
        this.state.load();

        // create the global actions
        this.switchPagesAction = new ActionContributionItem(new SwitchToNextPageAction(this, ExecutionViewMessages.Action_SwitchExecutionPage_Tooltip));
        this.switchPagesAction.setVisible(false);

        // add actions to the global toolbar of the executions view
        IToolBarManager toolBarManager = site.getActionBars().getToolBarManager();
        toolBarManager.appendToGroup(PART_GROUP, this.switchPagesAction);

        // add actions to the global menu of the executions view
        IMenuManager menuManager = site.getActionBars().getMenuManager();
        menuManager.add(new ToggleShowTreeHeaderAction(this, this.state));
    }

    @Override
    protected void updateVisibilityOfGlobalActions() {
        super.updateVisibilityOfGlobalActions();
        this.switchPagesAction.setVisible(hasPages());
    }

    @Override
    protected Page createDefaultPage() {
        return new MessagePage(ExecutionViewMessages.Label_No_Execution);
    }

    public void addExecutionPage(ProcessDescription processDescription, Request<Void> request) {
        ExecutionPage executionPage = new ExecutionPage(processDescription, request, this.state);
        addPage(executionPage);
        switchToPage(executionPage);
    }

    @Override
    public void dispose() {
        this.state.dispose();
        super.dispose();
    }

}
