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

import org.eclipse.buildship.ui.PluginImage;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.gradle.tooling.events.*;

import java.text.DecimalFormat;

/**
 * Configures an {@code OperationItem} instance.
 */
public final class OperationItemConfigurator {

    public void configure(OperationItem operationItem) {
        operationItem.setName(calculateName(operationItem));
        operationItem.setDuration(calculateDuration(operationItem));
        operationItem.setImage(calculateImage(operationItem));
    }

    private String calculateName(OperationItem operationItem) {
        return OperationDescriptorRenderer.renderCompact(operationItem);
    }

    private String calculateDuration(OperationItem operationItem) {
        if (operationItem.getFinishEvent() != null) {
            OperationResult result = operationItem.getFinishEvent().getResult();
            DecimalFormat durationFormat = new DecimalFormat("#0.000"); //$NON-NLS-1$
            String duration = durationFormat.format((result.getEndTime() - result.getStartTime()) / 1000.0);
            return NLS.bind(ExecutionViewMessages.Tree_Item_Operation_Finished_In_0_Sec_Text, duration);
        } else {
            return ExecutionViewMessages.Tree_Item_Operation_Started_Text;
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

}
