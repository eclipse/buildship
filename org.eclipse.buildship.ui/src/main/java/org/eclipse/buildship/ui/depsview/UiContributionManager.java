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

package org.eclipse.buildship.ui.depsview;

import org.eclipse.buildship.ui.UiPluginConstants;
import org.eclipse.buildship.ui.generic.ActionEnablingSelectionChangedListener;
import org.eclipse.buildship.ui.generic.ActionShowingContextMenuListener;
import org.eclipse.buildship.ui.generic.ContextActivatingViewPartListener;
import org.eclipse.buildship.ui.generic.SelectionSpecificAction;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Adds UI contributions to the {@link DependenciesView}.
 */
public final class UiContributionManager {

    private static final String TASK_MISC_GROUP = "taskMiscGroup";

    private final DependenciesView dependenciesView;
    private final ImmutableList<SelectionSpecificAction> managedActions;
    private final ActionEnablingSelectionChangedListener managedActionsSelectionChangedListener;
    private final TreeViewerSelectionChangeListener treeViewerSelectionChangeListener;
    private final ContextActivatingViewPartListener contextActivatingViewPartListener;
    private final WorkbenchSelectionListener workbenchSelectionListener;

    public UiContributionManager(DependenciesView taskView) {
        this.dependenciesView = Preconditions.checkNotNull(taskView);

        // add selection-sensitive actions
        this.managedActions = ImmutableList.<SelectionSpecificAction>of();

        this.managedActionsSelectionChangedListener = new ActionEnablingSelectionChangedListener(dependenciesView.getTreeViewer(), this.managedActions);
        this.treeViewerSelectionChangeListener = new TreeViewerSelectionChangeListener(dependenciesView);
        this.contextActivatingViewPartListener = new ContextActivatingViewPartListener(UiPluginConstants.UI_TASKVIEW_CONTEXT_ID, taskView);
        this.workbenchSelectionListener = new WorkbenchSelectionListener(taskView);
    }

    /**
     * Wires all UI contributions into the task view.
     */
    public void wire() {
        fillToolbar();
        fillMenu();
        fillContextMenu();
        addListeners();
    }

    private void fillToolbar() {
        IToolBarManager manager = this.dependenciesView.getViewSite().getActionBars().getToolBarManager();
        manager.add(new GroupMarker(TASK_MISC_GROUP));
        manager.appendToGroup(TASK_MISC_GROUP, new ToggleLinkToSelectionAction(this.dependenciesView));
    }

    private void fillMenu() {
        //IMenuManager manager = this.dependenciesView.getViewSite().getActionBars().getMenuManager();
    }

    private void fillContextMenu() {
        MenuManager contextMenuManager = new MenuManager();
        contextMenuManager.setRemoveAllWhenShown(true);
        contextMenuManager.addMenuListener(new ActionShowingContextMenuListener(this.dependenciesView.getTreeViewer(), this.managedActions));
        Control treeViewerControl = this.dependenciesView.getTreeViewer().getControl();
        Menu contextMenu = contextMenuManager.createContextMenu(treeViewerControl);
        this.dependenciesView.getTreeViewer().getControl().setMenu(contextMenu);
    }

    private void addListeners() {
        this.dependenciesView.getTreeViewer().addSelectionChangedListener(this.managedActionsSelectionChangedListener);
        this.dependenciesView.getTreeViewer().addSelectionChangedListener(this.treeViewerSelectionChangeListener);
        this.dependenciesView.getSite().getPage().addPartListener(this.contextActivatingViewPartListener);
        this.dependenciesView.getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this.workbenchSelectionListener);
    }

    public void dispose() {
        this.dependenciesView.getTreeViewer().removeSelectionChangedListener(this.managedActionsSelectionChangedListener);
        this.dependenciesView.getTreeViewer().removeSelectionChangedListener(this.treeViewerSelectionChangeListener);
        this.dependenciesView.getSite().getPage().removePartListener(this.contextActivatingViewPartListener);
        this.dependenciesView.getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this.workbenchSelectionListener);
    }

}
