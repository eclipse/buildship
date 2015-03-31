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

package org.eclipse.buildship.ui.console;

import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.eclipse.buildship.ui.PluginImage.ImageState;

/**
 * Cancel the build execution attached to the given {@link GradleConsole} instance.
 */
public final class CancelBuildExecutionAction extends Action {

    private final GradleConsole gradleConsole;

    public CancelBuildExecutionAction(GradleConsole gradleConsole) {
        this.gradleConsole = Preconditions.checkNotNull(gradleConsole);

        setToolTipText(ConsoleMessages.Action_CancelBuildExecution_Tooltip);
        setImageDescriptor(PluginImages.CANCEL_TASK_EXECUTION.withState(ImageState.ENABLED).getImageDescriptor());
        setDisabledImageDescriptor(PluginImages.CANCEL_TASK_EXECUTION.withState(ImageState.DISABLED).getImageDescriptor());

        registerJobChangeListener();
    }

    private void registerJobChangeListener() {
        Optional<Job> job = this.gradleConsole.getProcessDescription().getJob();
        if (job.isPresent()) {
            job.get().addJobChangeListener(new JobChangeAdapter() {

                @Override
                public void done(IJobChangeEvent event) {
                    CancelBuildExecutionAction.this.setEnabled(false);
                }
            });
            setEnabled(job.get().getState() != Job.NONE);
        } else {
            // if no job is associated with the console, never enable this action
            setEnabled(false);
        }
    }

    @Override
    public void run() {
        this.gradleConsole.getProcessDescription().getJob().get().cancel();
    }

    public void dispose(){
    }

}
