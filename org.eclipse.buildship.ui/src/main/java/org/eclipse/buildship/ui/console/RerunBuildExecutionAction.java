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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;

import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.buildship.ui.i18n.UiMessages;

/**
 * Reruns the build associated to the target {@link GradleConsole}.
 */
public final class RerunBuildExecutionAction extends Action {

    private final GradleConsole gradleConsole;

    public RerunBuildExecutionAction(GradleConsole gradleConsole) {
        this.gradleConsole = Preconditions.checkNotNull(gradleConsole);

        setToolTipText(UiMessages.Action_RerunBuild_Tooltip);
        setImageDescriptor(PluginImages.RERUN_BUILD.withState(ImageState.ENABLED).getImageDescriptor());
        setDisabledImageDescriptor(PluginImages.RERUN_BUILD.withState(ImageState.DISABLED).getImageDescriptor());

        registerJobChangeListener();
    }

    private void registerJobChangeListener() {
        Optional<Job> job = this.gradleConsole.getProcessDescription().getJob();
        if (job.isPresent()) {
            job.get().addJobChangeListener(new JobChangeAdapter() {

                @Override
                public void done(IJobChangeEvent event) {
                    RerunBuildExecutionAction.this.setEnabled(event.getJob().getState() == Job.NONE);
                }
            });
        }
        if (!job.isPresent()) {
            setEnabled(false);
        } else {
            setEnabled(job.get().getState() == Job.NONE);
        }
    }

    @Override
    public void run() {
        ILaunchConfiguration launchConfiguration = this.gradleConsole.getProcessDescription().getLaunch().get().getLaunchConfiguration();
        DebugUITools.launch(launchConfiguration, ILaunchManager.RUN_MODE);
    }

}
