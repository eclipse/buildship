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

package org.eclipse.buildship.ui.executionview;

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
import org.eclipse.ui.part.ViewPart;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.ui.executionview.listener.BuildLaunchRequestListener;
import org.eclipse.buildship.ui.executionview.listener.ProgressItemCreatedListener;
import org.eclipse.buildship.ui.executionview.model.ExecutionItem;
import org.eclipse.buildship.ui.executionview.model.internal.ProgressChildrenListProperty;
import org.eclipse.buildship.ui.viewer.FilteredTree;
import org.eclipse.buildship.ui.viewer.PatternFilter;
import org.eclipse.buildship.ui.viewer.labelprovider.ObservableMapCellWithIconLabelProvider;

/**
 * This part displays the Gradle executions, like a build. It contains a FilteredTree with an
 * operation and a duration column.
 *
 */
public class ExecutionView extends ViewPart {

    public static final String ID = "org.eclipse.buildship.ui.views.executionview";

    private FilteredTree filteredTree;

    private TreeViewerColumn labelColumn;

    private TreeViewerColumn durationColumn;

    private ExecutionItem root = new ExecutionItem(null);

    @Override
    public void createPartControl(Composite parent) {

        filteredTree = new FilteredTree(parent, SWT.BORDER, new PatternFilter());
        filteredTree.setShowFilterControls(false);
        filteredTree.getViewer().getTree().setHeaderVisible(true);

        createViewerColumns();

        bindUI();

        registerBuildLaunchRequestListener();

        registerExpandTreeOnNewProgressListener();

        getSite().setSelectionProvider(filteredTree.getViewer());
    }

    @Override
    public void setFocus() {
        filteredTree.getViewer().getControl().setFocus();
    }

    protected void createViewerColumns() {
        labelColumn = new TreeViewerColumn(filteredTree.getViewer(), SWT.NONE);
        labelColumn.getColumn().setText("Operations");
        labelColumn.getColumn().setWidth(450);

        durationColumn = new TreeViewerColumn(filteredTree.getViewer(), SWT.NONE);
        durationColumn.getColumn().setText("Duration");
        durationColumn.getColumn().setWidth(200);
    }

    protected void registerBuildLaunchRequestListener() {
        CorePlugin.eventBus().register(new BuildLaunchRequestListener(root));
    }

    protected void registerExpandTreeOnNewProgressListener() {
        CorePlugin.eventBus().register(new ProgressItemCreatedListener(filteredTree.getViewer()));
    }

    private void bindUI() {
        IListProperty childrenProperty = new ProgressChildrenListProperty();

        ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider(childrenProperty.listFactory(), null);
        filteredTree.getViewer().setContentProvider(contentProvider);

        IObservableSet knownElements = contentProvider.getKnownElements();
        attachLabelProvider(ExecutionItem.FIELD_LABEL, ExecutionItem.FIELD_IMAGE, knownElements, labelColumn);
        attachLabelProvider(ExecutionItem.FIELD_DURATION, null, knownElements, durationColumn);

        filteredTree.getViewer().setInput(root);
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

}
