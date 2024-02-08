/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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

    private final DecimalFormat durationFormat;
    private final String zeroDuration;

    public ExecutionPageDurationLabelProvider() {
        this.durationFormat = new DecimalFormat("#0.000");
        this.zeroDuration =  NLS.bind(ExecutionViewMessages.Tree_Item_Operation_Finished_In_0_Sec_Text, this.durationFormat.format(0));
    }

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
            return this.zeroDuration;
        }
    }

    private String formatDuration(long startTime, long endTime) {
        // synchronize since Format classes are not thread-safe
        synchronized (this.durationFormat) {
            return this.durationFormat.format((endTime - startTime) / 1000.0);
        }
    }
}
