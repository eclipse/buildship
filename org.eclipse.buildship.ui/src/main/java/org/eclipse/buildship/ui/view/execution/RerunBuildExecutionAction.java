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

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;

/**
 * Action for an {@link ExecutionPage} to re-run finished builds.
 */
public final class RerunBuildExecutionAction extends Action {

    private ExecutionPage page;

    public RerunBuildExecutionAction(ExecutionPage executionPage) {
        this.page = executionPage;
        setToolTipText(ExecutionsViewMessages.Action_RerunExecution_Tooltip);
        setImageDescriptor(PluginImages.RUN_BUILD.withState(ImageState.ENABLED).getImageDescriptor());
        setDisabledImageDescriptor(PluginImages.RUN_BUILD.withState(ImageState.DISABLED).getImageDescriptor());

        registerJobChangeListener();
    }

    private void registerJobChangeListener() {
        Job job = this.page.getBuildJob();
        job.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(IJobChangeEvent event) {
                RerunBuildExecutionAction.this.setEnabled(true);
            }
        });
        setEnabled(job.getState() == Job.NONE);
    }

    @Override
    public void run() {
        ILaunchConfiguration launchConfiguration = CorePlugin.gradleLaunchConfigurationManager().getOrCreateRunConfiguration(this.page.getAttributes());
        DebugUITools.launch(launchConfiguration, ILaunchManager.RUN_MODE);
    }
}
