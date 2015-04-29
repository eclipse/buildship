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

package org.eclipse.buildship.ui.view.executionview.model.internal;

import org.gradle.tooling.events.FinishEvent;
import org.gradle.tooling.events.OperationResult;
import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.test.TestFailureResult;
import org.gradle.tooling.events.test.TestSuccessResult;

import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.buildship.ui.view.executionview.model.ExecutionItem;
import org.eclipse.buildship.ui.view.executionview.model.ExecutionItemConfigurator;

/**
 * Default implementation of the {@link ExecutionItemConfigurator}.
 */
public class DefaultExecutionItemConfigurator implements ExecutionItemConfigurator {


    private ProgressEvent propressEvent;

    @Override
    public void configure(ExecutionItem progressItem) {
        String displayName = getPropressEvent().getDescriptor().getDisplayName();
        progressItem.setLabel(displayName);

        if(getPropressEvent() instanceof FinishEvent) {
            OperationResult result = ((FinishEvent) getPropressEvent()).getResult();
            progressItem.setDuration(result.getEndTime() - result.getStartTime() + "ms");
            if(result instanceof TestFailureResult) {
                progressItem.setImage(PluginImages.TEST_FAILURE.withState(ImageState.ENABLED).getImageDescriptor());
            }else if (result instanceof TestSuccessResult) {
                progressItem.setImage(PluginImages.TEST_SUCCESS.withState(ImageState.ENABLED).getImageDescriptor());
            }
        }else {
            progressItem.setDuration("Started...");
        }
    }

    public ProgressEvent getPropressEvent() {
        return propressEvent;
    }

    public void setPropressEvent(ProgressEvent propressEvent) {
        this.propressEvent = propressEvent;
    }

}
