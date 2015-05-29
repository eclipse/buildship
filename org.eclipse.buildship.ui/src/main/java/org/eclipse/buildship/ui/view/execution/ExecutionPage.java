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

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.gradleware.tooling.toolingclient.BuildLaunchRequest;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.IBeanValueProperty;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapCellLabelProvider;
import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;

import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.ui.generic.ActionShowingContextMenuListener;
import org.eclipse.buildship.ui.generic.NodeSelection;
import org.eclipse.buildship.ui.generic.NodeSelectionProvider;
import org.eclipse.buildship.ui.generic.SelectionHistoryManager;
import org.eclipse.buildship.ui.generic.SelectionSpecificAction;
import org.eclipse.buildship.ui.util.color.ColorUtils;
import org.eclipse.buildship.ui.view.BasePage;
import org.eclipse.buildship.ui.view.CollapseTreeNodesAction;
import org.eclipse.buildship.ui.view.ExpandTreeNodesAction;
import org.eclipse.buildship.ui.view.MultiPageView;
import org.eclipse.buildship.ui.view.PageSite;
import org.eclipse.buildship.ui.viewer.FilteredTree;
import org.eclipse.buildship.ui.viewer.ObservableMapCellWithIconLabelProvider;

/**
 * Displays the tree of a single build execution.
 */
@SuppressWarnings("unchecked")
public final class ExecutionPage extends BasePage<FilteredTree> implements NodeSelectionProvider {

    private final Job buildJob;
    private final String displayName;
    private final BuildLaunchRequest buildLaunchRequest;
    private final GradleRunConfigurationAttributes configurationAttributes;
    private final ExecutionsViewState state;

    private SelectionHistoryManager selectionHistoryManager;

    public ExecutionPage(Job buildJob, String displayName, BuildLaunchRequest buildLaunchRequest, GradleRunConfigurationAttributes configurationAttributes, ExecutionsViewState state) {
        this.buildJob = buildJob;
        this.displayName = displayName;
        this.buildLaunchRequest = buildLaunchRequest;
        this.configurationAttributes = configurationAttributes;
        this.state = state;
    }

    public Job getBuildJob() {
        return this.buildJob;
    }

    public GradleRunConfigurationAttributes getConfigurationAttributes() {
        return this.configurationAttributes;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public FilteredTree createPageWithResult(Composite parent) {
        // configure tree
        FilteredTree filteredTree = new FilteredTree(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL, new OperationItemPatternFilter());
        filteredTree.setShowFilterControls(false);
        filteredTree.getViewer().getTree().setHeaderVisible(this.state.isShowTreeHeader());

        TreeViewerColumn nameColumn = new TreeViewerColumn(filteredTree.getViewer(), SWT.NONE);
        nameColumn.getColumn().setText(ExecutionsViewMessages.Tree_Column_Operation_Name_Text);
        nameColumn.getColumn().setWidth(550);

        TreeViewerColumn durationColumn = new TreeViewerColumn(filteredTree.getViewer(), SWT.RIGHT);
        durationColumn.getColumn().setText(ExecutionsViewMessages.Tree_Column_Operation_Duration_Text);
        durationColumn.getColumn().setWidth(200);

        // configure data binding
        IListProperty childrenProperty = new OperationItemChildrenListProperty();
        ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider(childrenProperty.listFactory(), null);
        filteredTree.getViewer().setContentProvider(contentProvider);

        IObservableSet knownElements = contentProvider.getKnownElements();
        attachLabelProvider(OperationItem.FIELD_NAME, OperationItem.FIELD_IMAGE, knownElements, nameColumn);
        attachLabelProvider(OperationItem.FIELD_DURATION, null, knownElements, durationColumn);

        // manage the selection history
        this.selectionHistoryManager = new SelectionHistoryManager(filteredTree.getViewer());

        // set tree root node
        OperationItem root = new OperationItem();
        filteredTree.getViewer().setInput(root);

        // listen to progress events
        this.buildLaunchRequest.typedProgressListeners(new ExecutionProgressListener(this, root));

        // return the tree as the outermost page control
        return filteredTree;
    }

    private void attachLabelProvider(String textProperty, String imageProperty, IObservableSet knownElements, ViewerColumn viewerColumn) {
        IBeanValueProperty txtProperty = BeanProperties.value(textProperty);
        if (imageProperty != null) {
            IBeanValueProperty imgProperty = BeanProperties.value(imageProperty);
            ObservableMapCellWithIconLabelProvider labelProvider = new ObservableMapCellWithIconLabelProvider(getCustomTextColoringMapping(),
                    txtProperty.observeDetail(knownElements), imgProperty.observeDetail(knownElements));
            viewerColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(labelProvider));
        } else {
            ObservableMapCellLabelProvider labelProvider = new ObservableMapCellLabelProvider(txtProperty.observeDetail(knownElements));
            viewerColumn.setLabelProvider(labelProvider);
        }
    }

    private Map<String, ColorDescriptor> getCustomTextColoringMapping() {
        return ImmutableMap.of("UP-TO-DATE", ColorUtils.getDecorationsColorDescriptorFromCurrentTheme());
    }

    @Override
    public void init(PageSite pageSite) {
        super.init(pageSite);

        populateToolBar();
        registerContextMenu(pageSite);
        registerListeners();
    }

    private void populateToolBar() {
        IActionBars actionBars = getSite().getActionBars();
        IToolBarManager toolbarManager = actionBars.getToolBarManager();
        toolbarManager.appendToGroup(MultiPageView.PAGE_GROUP, new ExpandTreeNodesAction(getPageControl().getViewer()));
        toolbarManager.appendToGroup(MultiPageView.PAGE_GROUP, new CollapseTreeNodesAction(getPageControl().getViewer()));
        toolbarManager.appendToGroup(MultiPageView.PAGE_GROUP, new ShowFilterAction(this));
        toolbarManager.appendToGroup(MultiPageView.PAGE_GROUP, new Separator());
        toolbarManager.appendToGroup(MultiPageView.PAGE_GROUP, new SwitchToConsoleViewAction(this));
        toolbarManager.appendToGroup(MultiPageView.PAGE_GROUP, new Separator());
        toolbarManager.appendToGroup(MultiPageView.PAGE_GROUP, new CancelBuildExecutionAction(this));
        toolbarManager.appendToGroup(MultiPageView.PAGE_GROUP, new RerunBuildExecutionAction(this));
        toolbarManager.appendToGroup(MultiPageView.PAGE_GROUP, new RemoveTerminatedExecutionPageAction(this));
        toolbarManager.appendToGroup(MultiPageView.PAGE_GROUP, new RemoveAllTerminatedExecutionPagesAction(this));
        toolbarManager.update(true);
    }

    private void registerContextMenu(PageSite pageSite) {
        TreeViewer treeViewer = getPageControl().getViewer();
        MenuManager menuManager = new MenuManager();
        menuManager.setRemoveAllWhenShown(true);
        menuManager.addMenuListener(new ActionShowingContextMenuListener(this, createContextMenuActions(treeViewer)));
        Menu contextMenu = menuManager.createContextMenu(treeViewer.getTree());
        treeViewer.getTree().setMenu(contextMenu);
        pageSite.getViewSite().registerContextMenu(menuManager, treeViewer);
    }

    private List<SelectionSpecificAction> createContextMenuActions(TreeViewer treeViewer) {
        ExpandTreeNodesAction expandNodesAction = new ExpandTreeNodesAction(treeViewer);
        CollapseTreeNodesAction collapseNodesAction = new CollapseTreeNodesAction(treeViewer);
        OpenTestSourceFileAction openTestSourceFileAction = new OpenTestSourceFileAction(this);
        ShowFailureAction showFailureAction = new ShowFailureAction(this);
        return ImmutableList.<SelectionSpecificAction>of(expandNodesAction, collapseNodesAction, openTestSourceFileAction, showFailureAction);
    }

    private void registerListeners() {
        // navigate to source file on double click or when pressing enter
        getPageControl().getViewer().addDoubleClickListener(new IDoubleClickListener() {

            @Override
            public void doubleClick(DoubleClickEvent event) {
                OpenTestSourceFileAction openTestSourceFileAction = new OpenTestSourceFileAction(ExecutionPage.this);
                openTestSourceFileAction.run();
            }
        });
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Object getAdapter(Class adapter) {
        if (FilteredTree.class.equals(adapter)) {
            return getPageControl();
        } else if (adapter.isAssignableFrom(TreeViewer.class)) {
            // isAssignableFrom also applies for the ISelectionProvider interface
            return getPageControl().getViewer();
        }
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

    @Override
    public boolean isCloseable() {
        return this.buildJob.getState() == Job.NONE;
    }

    @Override
    public NodeSelection getSelection() {
        return this.selectionHistoryManager.getSelectionHistory();
    }

    @Override
    public void dispose() {
        this.selectionHistoryManager.dispose();
        super.dispose();
    }

}
