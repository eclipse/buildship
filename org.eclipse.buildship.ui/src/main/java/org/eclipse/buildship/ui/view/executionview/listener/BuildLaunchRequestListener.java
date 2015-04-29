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

package org.eclipse.buildship.ui.view.executionview.listener;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;

import org.eclipse.buildship.core.event.BuildLaunchRequestEvent;
import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.buildship.ui.view.executionview.model.ExecutionItem;

/**
 * This listener is invoked every time a Gradle build is started.
 */
public class BuildLaunchRequestListener {

    private ExecutionItem root;

    public BuildLaunchRequestListener(ExecutionItem root) {
        this.root = root;
    }

    @Subscribe
    public void handleBuildlaunchRequest(BuildLaunchRequestEvent event) {
        List<ExecutionItem> rootChildren = Lists.newArrayList();
        ExecutionItem buildStarted = new ExecutionItem(null, "Gradle Build");
        buildStarted.setImage(PluginImages.GRADLE_ICON.withState(ImageState.ENABLED).getImageDescriptor());
        rootChildren.add(buildStarted);
        root.setChildren(rootChildren);

        event.getElement().testProgressListeners(new ExecutionViewTestProgressListener(buildStarted));
    }
}
