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

import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.buildship.ui.part.execution.ExecutionsViewMessages;
import org.eclipse.buildship.ui.part.execution.ExecutionsViewState;
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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * This part displays the Gradle executions, like a build. It contains a FilteredTree with an
 * operation and a duration column.
 */
public final class ExecutionPage {

    private final ExecutionsViewState state;
    private FilteredTree filteredTree;
    private TreeViewerColumn labelColumn;
    private TreeViewerColumn durationColumn;
    private OperationItem root = new OperationItem(null);
    private OperationItem buildStarted;

    public ExecutionPage(ExecutionsViewState state) {
        this.state = state;
    }

    public void createPage(Composite parent) {
        this.filteredTree = new FilteredTree(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL, new OperationItemPatternFilter());
        this.filteredTree.setShowFilterControls(false);
        this.filteredTree.getViewer().getTree().setHeaderVisible(this.state.isShowTreeHeader());

        createViewerColumns();
        bindUI();
    }

    public void setFocus() {
        if (getViewer() != null && getViewer().getControl() != null && !getViewer().getControl().isDisposed()) {
            getViewer().getControl().setFocus();
        }
    }

    public FilteredTree getFilteredTree() {
        return this.filteredTree;
    }

    public TreeViewer getViewer() {
        if (getFilteredTree() != null && !getFilteredTree().isDisposed()) {
            return getFilteredTree().getViewer();
        }
        return null;
    }

    public void dispose() {
        if (getPageControl() != null && !getPageControl().isDisposed()) {
            getPageControl().dispose();
            this.filteredTree = null;
        }
    }

    protected void createViewerColumns() {
        this.labelColumn = new TreeViewerColumn(getViewer(), SWT.NONE);
        this.labelColumn.getColumn().setText(ExecutionsViewMessages.Tree_Column_Operation_Text);
        this.labelColumn.getColumn().setWidth(550);

        this.durationColumn = new TreeViewerColumn(getViewer(), SWT.NONE);
        this.durationColumn.getColumn().setText(ExecutionsViewMessages.Tree_Column_Duration_Text);
        this.durationColumn.getColumn().setWidth(200);
    }

    private void bindUI() {
        IListProperty childrenProperty = new ExecutionChildrenListProperty();

        ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider(childrenProperty.listFactory(), null);
        getViewer().setContentProvider(contentProvider);

        IObservableSet knownElements = contentProvider.getKnownElements();
        attachLabelProvider(OperationItem.FIELD_LABEL, OperationItem.FIELD_IMAGE, knownElements, this.labelColumn);
        attachLabelProvider(OperationItem.FIELD_DURATION, null, knownElements, this.durationColumn);

        getViewer().setInput(this.root);

        this.buildStarted = new OperationItem(null, ExecutionsViewMessages.Tree_Item_Root_Text);
        this.buildStarted.setImage(PluginImages.OPERATION_ROOT.withState(ImageState.ENABLED).getImageDescriptor());
        this.root.addChild(this.buildStarted);
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

    public Control getPageControl() {
        return getFilteredTree();
    }

    public OperationItem getBuildStartedItem() {
        return this.buildStarted;
    }

}
