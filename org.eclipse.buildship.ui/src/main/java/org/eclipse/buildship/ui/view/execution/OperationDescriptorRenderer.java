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
        if (descriptor instanceof TaskOperationDescriptor) {
            String taskPath = ((TaskOperationDescriptor) descriptor).getTaskPath();
            return taskIsUpToDate(finishEvent) ? String.format("Task %s UP-TO-DATE", taskPath) : String.format("Task %s", taskPath);
        } else if (descriptor instanceof TestOperationDescriptor) {
            return String.format("Test '%s'", descriptor.getName());
        } else {
            return descriptor.getDisplayName();
        }
    }

    public static String renderCompact(OperationItem operationItem) {
        OperationDescriptor descriptor = operationItem.getStartEvent().getDescriptor();
        if (descriptor instanceof TaskOperationDescriptor) {
            String taskPath = ((TaskOperationDescriptor) descriptor).getTaskPath();
            return taskIsUpToDate(operationItem.getFinishEvent()) ? String.format("%s UP-TO-DATE", taskPath) : taskPath;
        } else if (descriptor instanceof TestOperationDescriptor) {
            return descriptor.getName();
        } else {
            return descriptor.getDisplayName();
        }
    }


    private static boolean taskIsUpToDate(FinishEvent finishEvent) {
        if (finishEvent instanceof TaskFinishEvent) {
            TaskFinishEvent taskFinishEvent = (TaskFinishEvent) finishEvent;
            if (taskFinishEvent.getResult() instanceof TaskSuccessResult) {
                TaskSuccessResult taskSuccessResult = (TaskSuccessResult) taskFinishEvent.getResult();
                return taskSuccessResult.isUpToDate();
            }
        }
        return false;
    }

}
