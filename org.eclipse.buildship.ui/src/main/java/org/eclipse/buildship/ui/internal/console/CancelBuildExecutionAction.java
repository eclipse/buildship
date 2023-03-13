/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.console;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import org.eclipse.buildship.core.internal.console.ProcessDescription;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;

import org.eclipse.buildship.ui.internal.PluginImage.ImageState;
import org.eclipse.buildship.ui.internal.PluginImages;
import org.eclipse.buildship.ui.internal.i18n.UiMessages;

/**
 * Cancel the build execution attached to the given {@link GradleConsole} instance.
 */
public final class CancelBuildExecutionAction extends Action {

    private final GradleConsole gradleConsole;

    public CancelBuildExecutionAction(GradleConsole gradleConsole) {
        this.gradleConsole = Preconditions.checkNotNull(gradleConsole);

        setToolTipText(UiMessages.Action_CancelExecution_Tooltip);
        setImageDescriptor(PluginImages.CANCEL_BUILD_EXECUTION.withState(ImageState.ENABLED).getImageDescriptor());
        setDisabledImageDescriptor(PluginImages.CANCEL_BUILD_EXECUTION.withState(ImageState.DISABLED).getImageDescriptor());

        registerJobChangeListener();
    }

    private void registerJobChangeListener() {
        Optional<ProcessDescription> processDescription = this.gradleConsole.getProcessDescription();
        if (processDescription.isPresent()) {
            Job job = processDescription.get().getJob();
            job.addJobChangeListener(new JobChangeAdapter() {

                @Override
                public void done(IJobChangeEvent event) {
                    CancelBuildExecutionAction.this.setEnabled(event.getJob().getState() != Job.NONE);
                }
            });
            setEnabled(job.getState() != Job.NONE);
        } else {
            // if no job is associated with the console, never enable this action
            setEnabled(false);
        }
    }

    @Override
    public void run() {
        this.gradleConsole.getProcessDescription().get().getJob().cancel();
    }

    public void dispose(){
    }

}
