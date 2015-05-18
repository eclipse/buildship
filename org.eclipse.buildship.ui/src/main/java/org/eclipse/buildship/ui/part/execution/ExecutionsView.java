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

package org.eclipse.buildship.ui.part.execution;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.ui.part.FilteredTreePagePart;
import org.eclipse.buildship.ui.part.IPage;
import org.eclipse.buildship.ui.part.execution.listener.BuildLaunchRequestListener;
import org.eclipse.buildship.ui.part.execution.listener.ProgressItemCreatedListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

/**
 * This part displays the Gradle executions, like a build. It contains a FilteredTree with an
 * operation and a duration column.
 */
public class ExecutionsView extends FilteredTreePagePart {

    public static final String ID = "org.eclipse.buildship.ui.views.executionview";

    private ExecutionsViewState state;

    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);

        // load the persisted state before we create any UI components that query for some state
        this.state = new ExecutionsViewState();
        this.state.load();
    }

    public ExecutionsViewState getState() {
        return this.state;
    }

    @Override
    protected IPage getDefaultPage() {
        registerBuildLaunchRequestListener();
        registerExpandTreeOnNewProgressListener();
        return new DefaultExecutionPage();
    }

    protected void registerBuildLaunchRequestListener() {
        BuildLaunchRequestListener buildLaunchRequestListener = new BuildLaunchRequestListener(this);
        CorePlugin.listenerRegistry().addEventListener(buildLaunchRequestListener);
    }

    protected void registerExpandTreeOnNewProgressListener() {
        ProgressItemCreatedListener progressItemCreatedListener = new ProgressItemCreatedListener(this);
        CorePlugin.listenerRegistry().addEventListener(progressItemCreatedListener);
    }

    @Override
    public void dispose() {
        this.state.dispose();
        super.dispose();
    }

}
