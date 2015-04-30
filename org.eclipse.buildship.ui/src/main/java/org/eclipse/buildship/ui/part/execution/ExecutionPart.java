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
import org.eclipse.buildship.ui.part.FilteredTreeProvider;
import org.eclipse.buildship.ui.part.execution.listener.BuildLaunchRequestListener;
import org.eclipse.buildship.ui.part.execution.listener.ProgressItemCreatedListener;
import org.eclipse.buildship.ui.part.pages.DefaultExecutionPage;
import org.eclipse.buildship.ui.part.pages.IPage;

/**
 * This part displays the Gradle executions, like a build. It contains a FilteredTree with an
 * operation and a duration column.
 *
 */
public class ExecutionPart extends FilteredTreePagePart implements FilteredTreeProvider {

    public static final String ID = "org.eclipse.buildship.ui.views.executionview";

    private BuildLaunchRequestListener buildLaunchRequestListener;

    private ProgressItemCreatedListener progressItemCreatedListener;

    @Override
    protected IPage getDefaultPage() {

        registerBuildLaunchRequestListener();

        registerExpandTreeOnNewProgressListener();

        return new DefaultExecutionPage();
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
