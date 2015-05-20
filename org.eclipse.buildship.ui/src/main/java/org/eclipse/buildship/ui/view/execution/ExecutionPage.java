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
import org.eclipse.buildship.ui.part.execution.ExecutionsViewMessages;
import org.eclipse.buildship.ui.part.execution.model.OperationItem;
import org.eclipse.buildship.ui.part.execution.model.OperationItemPatternFilter;
import org.eclipse.buildship.ui.part.execution.model.internal.ExecutionChildrenListProperty;
import org.eclipse.buildship.ui.viewer.FilteredTree;
import org.eclipse.buildship.ui.viewer.labelprovider.ObservableMapCellWithIconLabelProvider;
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

/**
 * This part displays the Gradle executions, like a build. It contains a FilteredTree with an
 * operation and a duration column.
 */
public final class ExecutionPage {

    private final ExecutionsViewState state;
    private final FilteredTree filteredTree;

    public ExecutionPage(Composite parent, ExecutionsViewState state, BuildLaunchRequest buildLaunchRequest) {
        this.state = state;

        this.filteredTree = new FilteredTree(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL, new OperationItemPatternFilter());
        this.filteredTree.setShowFilterControls(false);
        this.filteredTree.getViewer().getTree().setHeaderVisible(this.state.isShowTreeHeader());

        TreeViewerColumn nameColumn = new TreeViewerColumn(this.filteredTree.getViewer(), SWT.NONE);
        nameColumn.getColumn().setText(ExecutionsViewMessages.Tree_Column_Operation_Text);
        nameColumn.getColumn().setWidth(550);

        TreeViewerColumn durationColumn = new TreeViewerColumn(this.filteredTree.getViewer(), SWT.NONE);
        durationColumn.getColumn().setText(ExecutionsViewMessages.Tree_Column_Duration_Text);
        durationColumn.getColumn().setWidth(200);

        IListProperty childrenProperty = new ExecutionChildrenListProperty();
        ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider(childrenProperty.listFactory(), null);
        this.filteredTree.getViewer().setContentProvider(contentProvider);

        IObservableSet knownElements = contentProvider.getKnownElements();
        attachLabelProvider(OperationItem.FIELD_NAME, OperationItem.FIELD_IMAGE, knownElements, nameColumn);
        attachLabelProvider(OperationItem.FIELD_DURATION, null, knownElements, durationColumn);

        OperationItem root = new OperationItem(null);
        this.filteredTree.getViewer().setInput(root);

        buildLaunchRequest.typedProgressListeners(new ExecutionProgressListener(root));
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

}
