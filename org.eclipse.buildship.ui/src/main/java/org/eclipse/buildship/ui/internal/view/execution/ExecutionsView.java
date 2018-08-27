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

package org.eclipse.buildship.ui.internal.view.execution;

import org.gradle.tooling.LongRunningOperation;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

import org.eclipse.buildship.core.internal.console.ProcessDescription;
import org.eclipse.buildship.ui.internal.view.MessagePage;
import org.eclipse.buildship.ui.internal.view.MultiPageView;
import org.eclipse.buildship.ui.internal.view.Page;
import org.eclipse.buildship.ui.internal.view.SwitchToNextPageAction;

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

    public void addExecutionPage(ProcessDescription processDescription, LongRunningOperation operation) {
        ExecutionPage executionPage = new ExecutionPage(processDescription, operation, this.state);
        addPage(executionPage);
        switchToPage(executionPage);
    }

    @Override
    public void dispose() {
        if (this.state != null) {
            this.state.dispose();
        }
        super.dispose();
    }

}
