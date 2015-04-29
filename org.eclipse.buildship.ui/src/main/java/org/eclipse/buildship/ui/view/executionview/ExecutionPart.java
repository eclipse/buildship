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

import org.eclipse.jface.viewers.Viewer;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.ui.view.FilteredTreePart;
import org.eclipse.buildship.ui.view.ViewerPart;
import org.eclipse.buildship.ui.view.executionview.listener.BuildLaunchRequestListener;
import org.eclipse.buildship.ui.view.executionview.listener.ProgressItemCreatedListener;
import org.eclipse.buildship.ui.view.pages.DefaultPage;
import org.eclipse.buildship.ui.view.pages.IPage;
import org.eclipse.buildship.ui.viewer.FilteredTree;

/**
 * This part displays the Gradle executions, like a build. It contains a FilteredTree with an
 * operation and a duration column.
 *
 */
public class ExecutionPart extends AbstractPagePart implements FilteredTreePart {

    public static final String ID = "org.eclipse.buildship.ui.views.executionview";

    private BuildLaunchRequestListener buildLaunchRequestListener;

    private ProgressItemCreatedListener progressItemCreatedListener;

    @Override
    protected IPage getDefaultPage() {

        registerBuildLaunchRequestListener();

        registerExpandTreeOnNewProgressListener();

        return new DefaultPage();
    }

    @Override
    public FilteredTree getFilteredTree() {
        IPage page = getCurrentPage();
        if (page instanceof FilteredTreePart) {
            return ((FilteredTreePart) page).getFilteredTree();
        }
        return null;
    }

    @Override
    public Viewer getViewer() {
        IPage page = getCurrentPage();
        if (page instanceof ViewerPart) {
            return ((ViewerPart) page).getViewer();
        }

        return null;
    }

    @Override
    public void dispose() {
        super.dispose();
        CorePlugin.eventBroker().unsubscribe(buildLaunchRequestListener);
        CorePlugin.eventBroker().unsubscribe(progressItemCreatedListener);
    }

    protected void registerBuildLaunchRequestListener() {
        this.buildLaunchRequestListener = new BuildLaunchRequestListener(this);
        CorePlugin.eventBroker().subscribe("", buildLaunchRequestListener);
    }

    protected void registerExpandTreeOnNewProgressListener() {
        this.progressItemCreatedListener = new ProgressItemCreatedListener(this);
        CorePlugin.eventBroker().subscribe("", progressItemCreatedListener);
    }
}
