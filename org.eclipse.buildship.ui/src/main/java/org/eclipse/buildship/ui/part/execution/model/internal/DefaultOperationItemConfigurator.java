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

package org.eclipse.buildship.ui.part.execution.model.internal;

import org.eclipse.osgi.util.NLS;

import org.gradle.tooling.events.FinishEvent;
import org.gradle.tooling.events.OperationResult;
import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.test.TestFailureResult;
import org.gradle.tooling.events.test.TestSuccessResult;

import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.buildship.ui.view.execution.ExecutionsViewMessages;
import org.eclipse.buildship.ui.part.execution.model.OperationItem;
import org.eclipse.buildship.ui.part.execution.model.OperationItemConfigurator;

/**
 * Default implementation of the {@link OperationItemConfigurator}.
 */
public class DefaultOperationItemConfigurator implements OperationItemConfigurator {

    private ProgressEvent progressEvent;

    @Override
    public void configure(OperationItem operationItem) {
        String displayName = operationItem.getOperationDescriptor().getDisplayName();
        operationItem.setLabel(displayName);

        if(getProgressEvent() instanceof FinishEvent) {
            OperationResult result = ((FinishEvent) getProgressEvent()).getResult();
            operationItem.setDuration(NLS.bind(ExecutionsViewMessages.Tree_Item_Operation_Finished_In_0_Text, result.getEndTime() - result.getStartTime()));
            if(result instanceof TestFailureResult) {
                operationItem.setImage(PluginImages.OPERATION_FAILURE.withState(ImageState.ENABLED).getImageDescriptor());
            }else if (result instanceof TestSuccessResult) {
                operationItem.setImage(PluginImages.OPERATION_SUCCESS.withState(ImageState.ENABLED).getImageDescriptor());
            }
        }else {
            operationItem.setDuration(ExecutionsViewMessages.Tree_Item_Operation_Started_Text);
        }
    }

    public ProgressEvent getProgressEvent() {
        return this.progressEvent;
    }

    public void setProgressEvent(ProgressEvent progressEvent) {
        this.progressEvent = progressEvent;
    }

}
