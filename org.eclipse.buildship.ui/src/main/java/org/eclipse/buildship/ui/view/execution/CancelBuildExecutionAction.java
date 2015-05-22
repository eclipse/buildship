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

import com.google.common.base.Preconditions;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;

import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.buildship.ui.i18n.UiMessages;

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
        Job job = this.page.getBuildJob();
        job.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(IJobChangeEvent event) {
                CancelBuildExecutionAction.this.setEnabled(false);
            }
        });
        setEnabled(job.getState() != Job.NONE);
    }

    @Override
    public void run() {
        this.page.getBuildJob().cancel();
    }

}
