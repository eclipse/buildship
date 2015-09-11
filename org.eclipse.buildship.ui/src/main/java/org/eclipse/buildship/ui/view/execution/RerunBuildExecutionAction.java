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
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.i18n.UiMessages;
import org.eclipse.buildship.ui.PluginImages;

/**
 * Reruns the build represented by the target {@link ExecutionPage}.
 */
public final class RerunBuildExecutionAction extends Action {

    private final ExecutionPage page;

    public RerunBuildExecutionAction(ExecutionPage executionPage) {
        this.page = Preconditions.checkNotNull(executionPage);

        setToolTipText(UiMessages.Action_RerunBuild_Tooltip);
        setImageDescriptor(PluginImages.RERUN_BUILD.withState(ImageState.ENABLED).getImageDescriptor());
        setDisabledImageDescriptor(PluginImages.RERUN_BUILD.withState(ImageState.DISABLED).getImageDescriptor());

        registerJobChangeListener();
    }

    private void registerJobChangeListener() {
        Job job = this.page.getProcessDescription().getJob();
        job.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(IJobChangeEvent event) {
                RerunBuildExecutionAction.this.setEnabled(event.getJob().getState() == Job.NONE);
            }
        });
        setEnabled(job.getState() == Job.NONE);
    }

    @Override
    public void run() {
        // TODO (donat) call processdescription.rerun()
        ILaunchConfiguration launchConfiguration = CorePlugin.gradleLaunchConfigurationManager().getOrCreateRunConfiguration(this.page.getProcessDescription().getConfigurationAttributes());
        DebugUITools.launch(launchConfiguration, ILaunchManager.RUN_MODE);
    }

}
