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
import org.eclipse.buildship.ui.part.execution.ExecutionsViewMessages;
import org.eclipse.buildship.ui.part.execution.model.OperationItem;
import org.eclipse.buildship.ui.part.execution.model.OperationItemConfigurator;

/**
 * Default implementation of the {@link OperationItemConfigurator}.
 */
public class DefaultOperationItemConfigurator implements OperationItemConfigurator {


    private ProgressEvent propressEvent;

    @Override
    public void configure(OperationItem progressItem) {
        String displayName = getPropressEvent().getDescriptor().getDisplayName();
        progressItem.setLabel(displayName);

        if(getPropressEvent() instanceof FinishEvent) {
            OperationResult result = ((FinishEvent) getPropressEvent()).getResult();
            progressItem.setDuration(NLS.bind(ExecutionsViewMessages.Tree_Item_Test_Finished_Text, result.getEndTime() - result.getStartTime()));
            if(result instanceof TestFailureResult) {
                progressItem.setImage(PluginImages.OPERATION_FAILURE.withState(ImageState.ENABLED).getImageDescriptor());
            }else if (result instanceof TestSuccessResult) {
                progressItem.setImage(PluginImages.OPERATION_SUCCESS.withState(ImageState.ENABLED).getImageDescriptor());
            }
        }else {
            progressItem.setDuration(ExecutionsViewMessages.Tree_Item_Test_Started_Text);
        }
    }

    public ProgressEvent getPropressEvent() {
        return this.propressEvent;
    }

    public void setPropressEvent(ProgressEvent propressEvent) {
        this.propressEvent = propressEvent;
    }

}
