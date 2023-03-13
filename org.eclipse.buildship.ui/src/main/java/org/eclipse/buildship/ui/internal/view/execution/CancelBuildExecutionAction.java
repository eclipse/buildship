/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view.execution;

import com.google.common.base.Preconditions;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;

import org.eclipse.buildship.ui.internal.PluginImage.ImageState;
import org.eclipse.buildship.ui.internal.PluginImages;
import org.eclipse.buildship.ui.internal.i18n.UiMessages;

/**
 * Cancel the build execution displayed by the target {@link ExecutionPage}.
 */
public final class CancelBuildExecutionAction extends Action {

    private final ExecutionPage page;

    public CancelBuildExecutionAction(ExecutionPage page) {
        this.page = Preconditions.checkNotNull(page);

        setToolTipText(UiMessages.Action_CancelExecution_Tooltip);
        setImageDescriptor(PluginImages.CANCEL_BUILD_EXECUTION.withState(ImageState.ENABLED).getImageDescriptor());
        setDisabledImageDescriptor(PluginImages.CANCEL_BUILD_EXECUTION.withState(ImageState.DISABLED).getImageDescriptor());

        registerJobChangeListener();
    }

    private void registerJobChangeListener() {
        Job job = this.page.getProcessDescription().getJob();
        job.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(IJobChangeEvent event) {
                CancelBuildExecutionAction.this.setEnabled(event.getJob().getState() != Job.NONE);
            }
        });
        setEnabled(job.getState() != Job.NONE);
    }

    @Override
    public void run() {
        this.page.getProcessDescription().getJob().cancel();
    }

}
