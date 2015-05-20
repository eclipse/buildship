package org.eclipse.buildship.ui.view.execution;

import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.ui.PluginImage;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.buildship.ui.part.execution.model.OperationItem;
import org.eclipse.osgi.util.NLS;
import org.gradle.tooling.events.*;

/**
 * Configures an {@code OperationItem} instance from an event belonging associated with that item.
 */
public final class DefaultOperationItemConfigurator {

    public void configure(OperationItem operationItem, ProgressEvent event) {
        String displayName = operationItem.getOperationDescriptor().getDisplayName();
        operationItem.setLabel(displayName);

        if (event instanceof StartEvent) {
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

}
