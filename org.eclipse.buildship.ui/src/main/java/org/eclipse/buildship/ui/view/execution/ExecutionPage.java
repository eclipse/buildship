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

import com.gradleware.tooling.toolingclient.BuildLaunchRequest;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.IBeanValueProperty;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapCellLabelProvider;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.buildship.ui.view.BasePage;
import org.eclipse.buildship.ui.viewer.FilteredTree;
import org.eclipse.buildship.ui.viewer.labelprovider.ObservableMapCellWithIconLabelProvider;

/**
 * Displays the tree of a single build execution.
 */
public final class ExecutionPage extends BasePage {

    private final ExecutionsViewState state;
    private final BuildLaunchRequest buildLaunchRequest;

    private FilteredTree filteredTree;
    private ExecutionsView executionsView;

    public ExecutionPage(ExecutionsView executionsView, Composite parent, ExecutionsViewState state, BuildLaunchRequest buildLaunchRequest) {
        this.executionsView = executionsView;
        this.state = state;
        this.buildLaunchRequest = buildLaunchRequest;
    }

    private void attachLabelProvider(String textProperty, String imageProperty, IObservableSet knownElements, ViewerColumn viewerColumn) {
        IBeanValueProperty txtProperty = BeanProperties.value(textProperty);
        if (imageProperty != null) {
            IBeanValueProperty imgProperty = BeanProperties.value(imageProperty);
            viewerColumn.setLabelProvider(new ObservableMapCellWithIconLabelProvider(txtProperty.observeDetail(knownElements), imgProperty.observeDetail(knownElements)));
        } else {
            viewerColumn.setLabelProvider(new ObservableMapCellLabelProvider(txtProperty.observeDetail(knownElements)));
        }
    }

    public FilteredTree getFilteredTree() {
        return this.filteredTree;
    }

    @Override
    public Control createPageContents(Composite parent) {
        // configure tree
        this.filteredTree = new FilteredTree(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL, new OperationItemPatternFilter());
        this.filteredTree.setShowFilterControls(false);
        this.filteredTree.getViewer().getTree().setHeaderVisible(this.state.isShowTreeHeader());

        TreeViewerColumn nameColumn = new TreeViewerColumn(this.filteredTree.getViewer(), SWT.NONE);
        nameColumn.getColumn().setText(ExecutionsViewMessages.Tree_Column_Operation_Name_Text);
        nameColumn.getColumn().setWidth(550);

        TreeViewerColumn durationColumn = new TreeViewerColumn(this.filteredTree.getViewer(), SWT.NONE);
        durationColumn.getColumn().setText(ExecutionsViewMessages.Tree_Column_Operation_Duration_Text);
        durationColumn.getColumn().setWidth(200);

        // configure data binding
        IListProperty childrenProperty = new OperationItemChildrenListProperty();
        ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider(childrenProperty.listFactory(), null);
        this.filteredTree.getViewer().setContentProvider(contentProvider);

        IObservableSet knownElements = contentProvider.getKnownElements();
        attachLabelProvider(OperationItem.FIELD_NAME, OperationItem.FIELD_IMAGE, knownElements, nameColumn);
        attachLabelProvider(OperationItem.FIELD_DURATION, null, knownElements, durationColumn);

        // set tree root node
        OperationItem root = new OperationItem();
        this.filteredTree.getViewer().setInput(root);

        // listen to progress events
        this.buildLaunchRequest.typedProgressListeners(new ExecutionProgressListener(this, root));

        return this.filteredTree;
    }

    public ExecutionsView getExecutionsView() {
        return this.executionsView;
    }

}
