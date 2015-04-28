package org.eclipse.buildship.ui.progressview.model.internal;

import org.gradle.tooling.events.FinishEvent;
import org.gradle.tooling.events.OperationResult;
import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.test.TestFailureResult;
import org.gradle.tooling.events.test.TestSuccessResult;

import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.buildship.ui.progressview.model.ProgressItem;
import org.eclipse.buildship.ui.progressview.model.ProgressItemConfigurator;


public class DefaultProgressItemConfigurator implements ProgressItemConfigurator {


    private ProgressEvent propressEvent;

    @Override
    public void configure(ProgressItem progressItem) {
        String displayName = getPropressEvent().getDescriptor().getDisplayName();
        progressItem.setLabel(displayName);

        if(getPropressEvent() instanceof FinishEvent) {
            OperationResult result = ((FinishEvent) getPropressEvent()).getResult();
            progressItem.setDuration(result.getEndTime() - result.getStartTime() + "ms");
            if(result instanceof TestFailureResult) {
                progressItem.setImage(PluginImages.CANCEL_TASK_EXECUTION.withState(ImageState.ENABLED).getImageDescriptor());
            }else if (result instanceof TestSuccessResult) {
                progressItem.setImage(PluginImages.RUN_TASKS.withState(ImageState.ENABLED).getImageDescriptor());
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
