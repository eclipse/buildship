/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.view.execution;

import org.gradle.tooling.events.FinishEvent;
import org.gradle.tooling.events.OperationDescriptor;
import org.gradle.tooling.events.task.TaskFinishEvent;
import org.gradle.tooling.events.task.TaskOperationDescriptor;
import org.gradle.tooling.events.task.TaskSuccessResult;
import org.gradle.tooling.events.test.TestOperationDescriptor;

/**
 * Renders an operation in the context of the Executions View.
 */
public final class OperationDescriptorRenderer {

    private OperationDescriptorRenderer() {
    }

    public static String renderVerbose(FinishEvent finishEvent) {
        OperationDescriptor descriptor = finishEvent.getDescriptor();
        return render(descriptor, finishEvent, true);
    }

    public static String renderCompact(OperationItem operationItem) {
        OperationDescriptor descriptor = operationItem.getStartEvent().getDescriptor();
        FinishEvent finishEvent = operationItem.getFinishEvent();
        return render(descriptor, finishEvent, false);
    }

    private static String render(OperationDescriptor descriptor, FinishEvent finishEvent, boolean verbose) {
        if (descriptor instanceof TaskOperationDescriptor) {
            return renderTask(finishEvent, ((TaskOperationDescriptor) descriptor), verbose);
        } else if (descriptor instanceof TestOperationDescriptor) {
            return renderTest(descriptor, verbose);
        } else {
            return renderOther(descriptor);
        }
    }

    private static String renderTask(FinishEvent finishEvent, TaskOperationDescriptor descriptor, boolean verbose) {
        StringBuilder task = new StringBuilder();

        if (verbose) {
            task.append("Task ");
        }

        task.append(descriptor.getTaskPath());

        if (finishEvent instanceof TaskFinishEvent) {
            if (finishEvent.getResult() instanceof TaskSuccessResult) {
                TaskSuccessResult taskResult = (TaskSuccessResult) finishEvent.getResult();
                if (taskResult.isFromCache()) {
                    task.append(" FROM-CACHE");
                } else if (taskResult.isUpToDate()) {
                    task.append(" UP-TO-DATE");
                }
            }
        }
        return task.toString();
    }

    private static String renderTest(OperationDescriptor descriptor, boolean verbose) {
        if (verbose) {
            return String.format("Test '%s'", descriptor.getName());
        } else {
            return descriptor.getName();
        }
    }

    private static String renderOther(OperationDescriptor descriptor) {
        return descriptor.getDisplayName();
    }

}
