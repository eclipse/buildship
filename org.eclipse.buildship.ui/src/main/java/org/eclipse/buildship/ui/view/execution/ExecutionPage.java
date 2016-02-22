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

import org.eclipse.buildship.core.console.ProcessDescription;
import org.eclipse.buildship.ui.external.viewer.FilteredTree;
import org.eclipse.buildship.ui.external.viewer.PatternFilter;
import org.eclipse.buildship.ui.util.color.ColorUtils;
import org.eclipse.buildship.ui.util.nodeselection.ActionShowingContextMenuListener;
import org.eclipse.buildship.ui.util.nodeselection.NodeSelection;
import org.eclipse.buildship.ui.util.nodeselection.NodeSelectionProvider;
import org.eclipse.buildship.ui.util.nodeselection.SelectionHistoryManager;
import org.eclipse.buildship.ui.util.nodeselection.SelectionSpecificAction;
import org.eclipse.buildship.ui.view.BasePage;
import org.eclipse.buildship.ui.view.CollapseTreeNodesAction;
import org.eclipse.buildship.ui.view.ExpandTreeNodesAction;
import org.eclipse.buildship.ui.view.MultiPageView;
import org.eclipse.buildship.ui.view.ObservableMapCellWithIconLabelProvider;
import org.eclipse.buildship.ui.view.PageSite;
import org.eclipse.buildship.ui.view.ShowFilterAction;
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
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.TreeTraverser;

import com.gradleware.tooling.toolingclient.SimpleRequest;

/**
 * Displays the tree of a single build execution.
 */
@SuppressWarnings("unchecked")
public final class ExecutionPage extends BasePage<FilteredTree> implements NodeSelectionProvider {

    private final ProcessDescription processDescription;
    private final SimpleRequest<Void> request;
    private final ExecutionViewState state;

    private SelectionHistoryManager selectionHistoryManager;
    private TreeViewerColumn nameColumn;
    private TreeViewerColumn durationColumn;

    public ExecutionPage(ProcessDescription processDescription, SimpleRequest<Void> request, ExecutionViewState state) {
        this.processDescription = processDescription;
        this.request = request;
        this.state = state;
    }

    public ProcessDescription getProcessDescription() {
        return this.processDescription;
    }

    @Override
    public String getDisplayName() {
        return this.processDescription.getName();
    }

    @Override
    public FilteredTree createPageWithResult(Composite parent) {
        // configure tree
        FilteredTree filteredTree = new FilteredTree(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL, new PatternFilter(true));
        filteredTree.setShowFilterControls(false);
        filteredTree.getViewer().getTree().setHeaderVisible(this.state.isShowTreeHeader());

        this.nameColumn = new TreeViewerColumn(filteredTree.getViewer(), SWT.NONE);
        this.nameColumn.getColumn().setText(ExecutionViewMessages.Tree_Column_Operation_Name_Text);
        this.nameColumn.getColumn().setWidth(this.state.getHeaderNameColumnWidth());

        this.durationColumn = new TreeViewerColumn(filteredTree.getViewer(), SWT.RIGHT);
        this.durationColumn.getColumn().setText(ExecutionViewMessages.Tree_Column_Operation_Duration_Text);
        this.durationColumn.getColumn().setWidth(this.state.getHeaderDurationColumnWidth());

        // configure data binding
        IListProperty childrenProperty = new OperationItemChildrenListProperty();
        ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider(childrenProperty.listFactory(), null);
        filteredTree.getViewer().setContentProvider(contentProvider);

        IObservableSet knownElements = contentProvider.getKnownElements();
        attachLabelProvider(OperationItem.FIELD_NAME, OperationItem.FIELD_IMAGE, knownElements, this.nameColumn);
        attachLabelProvider(OperationItem.FIELD_DURATION, null, knownElements, this.durationColumn);

        // keep header size synchronized between pages
        this.nameColumn.getColumn().addControlListener(new ControlAdapter() {

            @Override
            public void controlResized(ControlEvent e) {
                int newWidth = ExecutionPage.this.nameColumn.getColumn().getWidth();
                ExecutionPage.this.state.setHeaderNameColumnWidth(newWidth);
                ExecutionsView view = (ExecutionsView) getSite().getViewSite().getPart();
                for (ExecutionPage page : FluentIterable.from(view.getPages()).filter(ExecutionPage.class)) {
                    if (page != ExecutionPage.this) {
                        page.nameColumn.getColumn().setWidth(newWidth);
                    }
                }
            }
        });

        this.durationColumn.getColumn().addControlListener(new ControlAdapter() {

            @Override
            public void controlResized(ControlEvent e) {
                int newWidth = ExecutionPage.this.durationColumn.getColumn().getWidth();
                ExecutionPage.this.state.setHeaderDurationColumnWidth(newWidth);
                ExecutionsView view = (ExecutionsView) getSite().getViewSite().getPart();
                for (ExecutionPage page : FluentIterable.from(view.getPages()).filter(ExecutionPage.class)) {
                    if (page != ExecutionPage.this) {
                        page.durationColumn.getColumn().setWidth(newWidth);
                    }
                }
            }
        });

        // manage the selection history
        this.selectionHistoryManager = new SelectionHistoryManager(filteredTree.getViewer());

        // set tree root node
        OperationItem root = new OperationItem();
        filteredTree.getViewer().setInput(root);

        // listen to progress events
        this.request.addTypedProgressListeners(new ExecutionProgressListener(this, root));

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
        toolbarManager.appendToGroup(MultiPageView.PAGE_GROUP, new ShowFilterAction(getPageControl()));
        toolbarManager.appendToGroup(MultiPageView.PAGE_GROUP, new Separator());
        toolbarManager.appendToGroup(MultiPageView.PAGE_GROUP, new SwitchToConsoleViewAction(this));
        toolbarManager.appendToGroup(MultiPageView.PAGE_GROUP, new Separator());
        toolbarManager.appendToGroup(MultiPageView.PAGE_GROUP, new RerunFailedTestsAction(this));
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
        menuManager.addMenuListener(createContextMenuListener(treeViewer));
        Menu contextMenu = menuManager.createContextMenu(treeViewer.getTree());
        treeViewer.getTree().setMenu(contextMenu);
        pageSite.getViewSite().registerContextMenu(menuManager, treeViewer);
    }

    private ActionShowingContextMenuListener createContextMenuListener(TreeViewer treeViewer) {
        RunTestAction runTestAction = new RunTestAction(this);
        ShowFailureAction showFailureAction = new ShowFailureAction(this);
        OpenTestSourceFileAction openTestSourceFileAction = new OpenTestSourceFileAction(this);
        ExpandTreeNodesAction expandNodesAction = new ExpandTreeNodesAction(treeViewer);
        CollapseTreeNodesAction collapseNodesAction = new CollapseTreeNodesAction(treeViewer);

        List<SelectionSpecificAction> contextMenuActions = ImmutableList.<SelectionSpecificAction>of(runTestAction, showFailureAction, openTestSourceFileAction, expandNodesAction, collapseNodesAction);
        List<SelectionSpecificAction> contextMenuActionsPrecededBySeparator = ImmutableList.<SelectionSpecificAction>of(openTestSourceFileAction, expandNodesAction);
        ImmutableList<SelectionSpecificAction> contextMenuActionsSucceededBySeparator = ImmutableList.of();

        return new ActionShowingContextMenuListener(this, contextMenuActions, contextMenuActionsPrecededBySeparator, contextMenuActionsSucceededBySeparator);
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

    public FluentIterable<OperationItem> filterTreeNodes(Predicate<OperationItem> predicate) {
        OperationItem root = (OperationItem) getPageControl().getViewer().getInput();
        return new TreeTraverser<OperationItem>() {

            @Override
            public Iterable<OperationItem> children(OperationItem operationItem) {
                return operationItem.getChildren();
            }
        }.breadthFirstTraversal(root).filter(predicate);
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
        return this.processDescription.getJob().getState() == Job.NONE;
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
