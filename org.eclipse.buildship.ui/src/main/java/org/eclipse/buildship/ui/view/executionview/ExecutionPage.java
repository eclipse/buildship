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

package org.eclipse.buildship.ui.view.executionview;

import java.util.List;

import com.google.common.collect.Lists;

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

import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.buildship.ui.view.FilteredTreePart;
import org.eclipse.buildship.ui.view.executionview.model.ExecutionItem;
import org.eclipse.buildship.ui.view.executionview.model.internal.ProgressChildrenListProperty;
import org.eclipse.buildship.ui.view.pages.IPage;
import org.eclipse.buildship.ui.viewer.FilteredTree;
import org.eclipse.buildship.ui.viewer.PatternFilter;
import org.eclipse.buildship.ui.viewer.labelprovider.ObservableMapCellWithIconLabelProvider;

/**
 * This part displays the Gradle executions, like a build. It contains a FilteredTree with an
 * operation and a duration column.
 *
 */
public class ExecutionPage implements IPage, FilteredTreePart {

    private FilteredTree filteredTree;

    private TreeViewerColumn labelColumn;

    private TreeViewerColumn durationColumn;

    private ExecutionItem root = new ExecutionItem(null);

    private ExecutionItem buildStarted;

    private String displayName;

    @Override
    public void createPage(Composite parent) {

        ExecutionPartPreferences partPrefs = new ExecutionPartPreferences();

        filteredTree = new FilteredTree(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, new PatternFilter());
        filteredTree.setShowFilterControls(partPrefs.getFilterVisibile());
        filteredTree.getViewer().getTree().setHeaderVisible(partPrefs.getHeaderVisibile());

        createViewerColumns();

        bindUI();
    }

    @Override
    public void setFocus() {
        getViewer().getControl().setFocus();
    }

    @Override
    public FilteredTree getFilteredTree() {
        return filteredTree;
    }

    @Override
    public TreeViewer getViewer() {
        return filteredTree.getViewer();
    }

    @Override
    public void dispose() {
    }

    protected void createViewerColumns() {
        labelColumn = new TreeViewerColumn(getViewer(), SWT.NONE);
        labelColumn.getColumn().setText("Operations");
        labelColumn.getColumn().setWidth(450);

        durationColumn = new TreeViewerColumn(getViewer(), SWT.NONE);
        durationColumn.getColumn().setText("Duration");
        durationColumn.getColumn().setWidth(200);
    }

    private void bindUI() {
        IListProperty childrenProperty = new ProgressChildrenListProperty();

        ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider(childrenProperty.listFactory(), null);
        getViewer().setContentProvider(contentProvider);

        IObservableSet knownElements = contentProvider.getKnownElements();
        attachLabelProvider(ExecutionItem.FIELD_LABEL, ExecutionItem.FIELD_IMAGE, knownElements, labelColumn);
        attachLabelProvider(ExecutionItem.FIELD_DURATION, null, knownElements, durationColumn);

        getViewer().setInput(root);

        List<ExecutionItem> rootChildren = Lists.newArrayList();
        buildStarted = new ExecutionItem(null, "Gradle Build");
        buildStarted.setImage(PluginImages.GRADLE_ICON.withState(ImageState.ENABLED).getImageDescriptor());
        rootChildren.add(buildStarted);
        root.setChildren(rootChildren);
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

    @Override
    public Control getPageControl() {
        return getFilteredTree();
    }

    public ExecutionItem getBuildStartedItem() {
        return buildStarted;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

}
