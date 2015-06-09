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

import java.text.DecimalFormat;

import org.gradle.tooling.events.FailureResult;
import org.gradle.tooling.events.OperationResult;
import org.gradle.tooling.events.SkippedResult;
import org.gradle.tooling.events.SuccessResult;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;

import org.eclipse.buildship.ui.PluginImage;
import org.eclipse.buildship.ui.PluginImages;

/**
 * Configures an {@code OperationItem} instance.
 */
public final class OperationItemConfigurator {

    private final DecimalFormat durationFormat = new DecimalFormat("#0.000"); //$NON-NLS-1$

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public void update(OperationItem operationItem) {
        synchronized (operationItem) {
            operationItem.setName(calculateName(operationItem));
            operationItem.setDuration(calculateDuration(operationItem));
            operationItem.setImage(calculateImage(operationItem));
        }
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public void updateDuration(OperationItem operationItem) {
        synchronized (operationItem) {
            operationItem.setDuration(calculateDuration(operationItem));
        }
    }

    private String calculateName(OperationItem operationItem) {
        return OperationDescriptorRenderer.renderCompact(operationItem);
    }

    private String calculateDuration(OperationItem operationItem) {
        if (operationItem.getFinishEvent() != null) {
            OperationResult result = operationItem.getFinishEvent().getResult();
            String duration = formatDuration(result.getStartTime(), result.getEndTime());
            return NLS.bind(ExecutionViewMessages.Tree_Item_Operation_Finished_In_0_Sec_Text, duration);
        } else if (operationItem.getStartEvent() != null) {
            String duration = formatDuration(operationItem.getStartEvent().getEventTime(), System.currentTimeMillis());
            return NLS.bind(ExecutionViewMessages.Tree_Item_Operation_Running_For_0_Sec_Text, duration);
        } else {
            // only happens for the artificial root node
            return ""; //$NON-NLS-1$
        }
    }

    private ImageDescriptor calculateImage(OperationItem operationItem) {
        if (operationItem.getFinishEvent() != null) {
            OperationResult result = operationItem.getFinishEvent().getResult();
            if (result instanceof FailureResult) {
                return PluginImages.OPERATION_FAILURE.withState(PluginImage.ImageState.ENABLED).getImageDescriptor();
            } else if (result instanceof SkippedResult) {
                return PluginImages.OPERATION_SKIPPED.withState(PluginImage.ImageState.ENABLED).getImageDescriptor();
            } else if (result instanceof SuccessResult) {
                return PluginImages.OPERATION_SUCCESS.withState(PluginImage.ImageState.ENABLED).getImageDescriptor();
            } else {
                return null;
            }
        } else {
            return PluginImages.OPERATION_IN_PROGRESS.withState(PluginImage.ImageState.ENABLED).getImageDescriptor();
        }
    }

    private String formatDuration(long startTime, long endTime) {
        // synchronize since Format classes are not thread-safe
        synchronized (this.durationFormat) {
            return this.durationFormat.format((endTime - startTime) / 1000.0);
        }
    }

}
