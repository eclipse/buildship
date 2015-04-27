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

package org.eclipse.buildship.ui.taskview;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import org.eclipse.buildship.ui.UiPluginConstants;
import org.eclipse.buildship.ui.generic.ActionEnablingSelectionChangedListener;
import org.eclipse.buildship.ui.generic.ActionShowingContextMenuListener;
import org.eclipse.buildship.ui.generic.ContextActivatingViewPartListener;
import org.eclipse.buildship.ui.generic.SelectionSpecificAction;

/**
 * Adds UI contributions to the {@link TaskView}.
 */
public final class UiContributionManager {

    private static final String TOOLBAR_MISC_GROUP = "toolbarMiscGroup";
    private static final String MENU_SORTING_GROUP = "toolbarSortingGroup";
    private static final String MENU_FILTERING_GROUP = "menuFilteringGroup";
    private static final String MENU_MISC_GROUP = "menuMiscGroup";

    private final TaskView taskView;
    private final ImmutableList<SelectionSpecificAction> managedActions;
    private final ActionEnablingSelectionChangedListener managedActionsSelectionChangedListener;
    private final TreeViewerSelectionChangeListener treeViewerSelectionChangeListener;
    private final TreeViewerDoubleClickListener treeViewerDoubleClickListener;
    private final ContextActivatingViewPartListener contextActivatingViewPartListener;
    private final WorkbenchSelectionListener workbenchSelectionListener;
    private final WorkspaceProjectsChangeListener workspaceProjectsChangeListener;

    public UiContributionManager(TaskView taskView) {
        this.taskView = Preconditions.checkNotNull(taskView);

        // add selection-sensitive actions
        RunTasksAction runTasksAction = new RunTasksAction(UiPluginConstants.RUN_TASKS_COMMAND_ID);
        RunDefaultTasksAction runDefaultTasksAction = new RunDefaultTasksAction(UiPluginConstants.RUN_DEFAULT_TASKS_COMMAND_ID);
        CreateRunConfigurationAction createRunConfigurationAction = new CreateRunConfigurationAction(UiPluginConstants.OPEN_RUN_CONFIGURATION_COMMAND_ID);
        OpenRunConfigurationAction openRunConfigurationAction = new OpenRunConfigurationAction(UiPluginConstants.OPEN_RUN_CONFIGURATION_COMMAND_ID);
        OpenBuildScriptAction openBuildScriptAction = new OpenBuildScriptAction(UiPluginConstants.OPEN_BUILD_SCRIPT_COMMAND_ID);
        this.managedActions = ImmutableList
                .<SelectionSpecificAction> of(runTasksAction, runDefaultTasksAction, createRunConfigurationAction, openRunConfigurationAction, openBuildScriptAction);

        this.managedActionsSelectionChangedListener = new ActionEnablingSelectionChangedListener(taskView.getTreeViewer(), this.managedActions);
        this.treeViewerSelectionChangeListener = new TreeViewerSelectionChangeListener(taskView);
        this.treeViewerDoubleClickListener = new TreeViewerDoubleClickListener(UiPluginConstants.RUN_TASKS_COMMAND_ID, taskView.getTreeViewer());
        this.contextActivatingViewPartListener = new ContextActivatingViewPartListener(UiPluginConstants.UI_TASKVIEW_CONTEXT_ID, taskView);
        this.workbenchSelectionListener = new WorkbenchSelectionListener(taskView);
        this.workspaceProjectsChangeListener = new WorkspaceProjectsChangeListener(taskView);
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
        IToolBarManager manager = this.taskView.getViewSite().getActionBars().getToolBarManager();
        manager.add(new GroupMarker(TOOLBAR_MISC_GROUP));
        manager.appendToGroup(TOOLBAR_MISC_GROUP, new RefreshViewAction(UiPluginConstants.REFRESH_TASKVIEW_COMMAND_ID));
        manager.appendToGroup(TOOLBAR_MISC_GROUP, new ToggleLinkToSelectionAction(this.taskView));
    }

    private void fillMenu() {
        IMenuManager manager = this.taskView.getViewSite().getActionBars().getMenuManager();
        manager.add(new Separator(MENU_FILTERING_GROUP));
        manager.appendToGroup(MENU_FILTERING_GROUP, new FilterTaskSelectorsAction(this.taskView));
        manager.appendToGroup(MENU_FILTERING_GROUP, new FilterProjectTasksAction(this.taskView));
        manager.appendToGroup(MENU_FILTERING_GROUP, new FilterPrivateTasksAction(this.taskView));
        manager.add(new Separator(MENU_SORTING_GROUP));
        manager.appendToGroup(MENU_SORTING_GROUP, new SortTasksByTypeAction(this.taskView));
        manager.appendToGroup(MENU_SORTING_GROUP, new SortTasksByVisibilityAction(this.taskView));
        manager.add(new Separator(MENU_MISC_GROUP));
        manager.appendToGroup(MENU_MISC_GROUP, new ToggleShowTreeHeaderAction(this.taskView));
    }

    private void fillContextMenu() {
        MenuManager contextMenuManager = new MenuManager();
        contextMenuManager.setRemoveAllWhenShown(true);
        contextMenuManager.addMenuListener(new ActionShowingContextMenuListener(this.taskView.getTreeViewer(), this.managedActions));
        Control treeViewerControl = this.taskView.getTreeViewer().getControl();
        Menu contextMenu = contextMenuManager.createContextMenu(treeViewerControl);
        this.taskView.getTreeViewer().getControl().setMenu(contextMenu);
    }

    private void addListeners() {
        this.taskView.getTreeViewer().addSelectionChangedListener(this.managedActionsSelectionChangedListener);
        this.taskView.getTreeViewer().addSelectionChangedListener(this.treeViewerSelectionChangeListener);
        this.taskView.getTreeViewer().addDoubleClickListener(this.treeViewerDoubleClickListener);
        this.taskView.getSite().getPage().addPartListener(this.contextActivatingViewPartListener);
        this.taskView.getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this.workbenchSelectionListener);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this.workspaceProjectsChangeListener);
    }

    public void dispose() {
        this.taskView.getTreeViewer().removeSelectionChangedListener(this.managedActionsSelectionChangedListener);
        this.taskView.getTreeViewer().removeSelectionChangedListener(this.treeViewerSelectionChangeListener);
        this.taskView.getTreeViewer().removeDoubleClickListener(this.treeViewerDoubleClickListener);
        this.taskView.getSite().getPage().removePartListener(this.contextActivatingViewPartListener);
        this.taskView.getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this.workbenchSelectionListener);
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this.workspaceProjectsChangeListener);
    }

}
