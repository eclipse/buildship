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

package org.eclipse.buildship.ui.part.execution;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.ui.handler.CollapseHandler;
import org.eclipse.buildship.ui.handler.ShowFilterControlsAction;
import org.eclipse.buildship.ui.part.FilteredTreePagePart;
import org.eclipse.buildship.ui.part.FilteredTreeProvider;
import org.eclipse.buildship.ui.part.IPage;
import org.eclipse.buildship.ui.part.execution.listener.ProgressItemCreatedListener;
import org.eclipse.buildship.ui.viewer.FilteredTree;

/**
 * This part displays the Gradle executions, like a build. It contains a FilteredTree with an
 * operation and a duration column.
 */
public class ExecutionsView extends FilteredTreePagePart {

    public static final String ID = "org.eclipse.buildship.ui.views.executionview"; //$NON-NLS-1$

    private ExecutionsViewState state;
    private ProgressItemCreatedListener progressItemCreatedListener;

    private ActionContributionItem showFilterControlsContributionItem;
    private boolean isFilterControlsAdded;

    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);

        // load the persisted state before we create any UI components that query for some state
        this.state = new ExecutionsViewState();
        this.state.load();

        // register a listener that expands the tree as new items are added
        this.progressItemCreatedListener = new ProgressItemCreatedListener(this);
        CorePlugin.listenerRegistry().addEventListener(this.progressItemCreatedListener);

        this.showFilterControlsContributionItem = new ActionContributionItem(new ShowFilterControlsAction(this));
    }

    @Override
    public void setCurrentPage(IPage page) {
        handleShowFilterControlAction(page);

        super.setCurrentPage(page);
    }

    private void handleShowFilterControlAction(IPage page) {
        if (page instanceof FilteredTreeProvider) {
            if (!isFilterControlsAdded) {
                getViewSite().getActionBars().getToolBarManager().insertAfter(CollapseHandler.ID, showFilterControlsContributionItem);
                isFilterControlsAdded = true;
            }
            FilteredTree filteredTree = ((FilteredTreeProvider) page).getFilteredTree();
            showFilterControlsContributionItem.getAction().setChecked(filteredTree.isShowFilterControls());
        } else {
            getViewSite().getActionBars().getToolBarManager().remove(showFilterControlsContributionItem);
            isFilterControlsAdded = false;
        }
        getViewSite().getActionBars().updateActionBars();
    }

    public ExecutionsViewState getState() {
        return this.state;
    }

    @Override
    protected IPage getDefaultPage() {
        return new DefaultExecutionPage();
    }

    @Override
    public void dispose() {
        CorePlugin.listenerRegistry().removeEventListener(this.progressItemCreatedListener);
        this.state.dispose();
        super.dispose();
    }

}
