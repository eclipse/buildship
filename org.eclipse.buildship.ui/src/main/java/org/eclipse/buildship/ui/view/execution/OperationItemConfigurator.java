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

import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.ui.PluginImage;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.osgi.util.NLS;
import org.gradle.tooling.events.*;
import org.gradle.tooling.events.task.TaskOperationDescriptor;
import org.gradle.tooling.events.test.TestOperationDescriptor;

/**
 * Configures an {@code OperationItem} instance from an event belonging associated with that item.
 */
public final class OperationItemConfigurator {

    public void configure(OperationItem operationItem, ProgressEvent event) {
        if (event instanceof StartEvent) {
            displayOperationSpecificName(operationItem, event);
            operationItem.setDuration(ExecutionsViewMessages.Tree_Item_Operation_Started_Text);
        } else if (event instanceof FinishEvent) {
            OperationResult result = ((FinishEvent) event).getResult();
            operationItem.setDuration(NLS.bind(ExecutionsViewMessages.Tree_Item_Operation_Finished_In_0_Text, result.getEndTime() - result.getStartTime()));
            if (result instanceof FailureResult) {
                operationItem.setImage(PluginImages.OPERATION_FAILURE.withState(PluginImage.ImageState.ENABLED).getImageDescriptor());
            } else if (result instanceof SkippedResult) {
                operationItem.setImage(PluginImages.OPERATION_SKIPPED.withState(PluginImage.ImageState.ENABLED).getImageDescriptor());
            } else if (result instanceof SuccessResult) {
                operationItem.setImage(PluginImages.OPERATION_SUCCESS.withState(PluginImage.ImageState.ENABLED).getImageDescriptor());
            }
        } else {
            throw new GradlePluginsRuntimeException("Unsupported event type: " + event.getClass());
        }
    }

    private void displayOperationSpecificName(OperationItem operationItem, ProgressEvent event) {
        OperationDescriptor descriptor = event.getDescriptor();
        if (descriptor instanceof TestOperationDescriptor) {
            operationItem.setName(descriptor.getName());
        } else if (descriptor instanceof TaskOperationDescriptor) {
            operationItem.setName(((TaskOperationDescriptor) descriptor).getTaskPath());
        }
    }

}
