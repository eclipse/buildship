/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.internal.view.execution;

import java.text.DecimalFormat;

import org.gradle.tooling.events.OperationResult;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.osgi.util.NLS;

/**
 * Label provider for for the second column of {@link ExecutionPage} containing the time spent
 * executing a build operation.
 */
public final class ExecutionPageDurationLabelProvider extends LabelProvider implements IStyledLabelProvider {

    private final DecimalFormat durationFormat = new DecimalFormat("#0.000");

    @Override
    public StyledString getStyledText(Object element) {
        return new StyledString(element instanceof OperationItem ? calculateDuration((OperationItem) element) : "");
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
            return "";
        }
    }

    private String formatDuration(long startTime, long endTime) {
        // synchronize since Format classes are not thread-safe
        synchronized (this.durationFormat) {
            return this.durationFormat.format((endTime - startTime) / 1000.0);
        }
    }
}
