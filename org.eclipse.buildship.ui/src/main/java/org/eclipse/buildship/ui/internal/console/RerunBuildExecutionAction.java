/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
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
import org.eclipse.buildship.ui.internal.PluginImage.ImageState;
import org.eclipse.buildship.ui.internal.PluginImages;
import org.eclipse.buildship.ui.internal.i18n.UiMessages;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.jface.action.Action;

/**
 * Reruns the build associated to the target {@link GradleConsole}.
 */
public final class RerunBuildExecutionAction extends Action implements ILaunchConfigurationListener {

    private final GradleConsole gradleConsole;

    public RerunBuildExecutionAction(GradleConsole gradleConsole) {
        this.gradleConsole = Preconditions.checkNotNull(gradleConsole);

        setToolTipText(UiMessages.Action_RerunBuild_Tooltip);
        setImageDescriptor(PluginImages.RERUN_BUILD.withState(ImageState.ENABLED).getImageDescriptor());
        setDisabledImageDescriptor(PluginImages.RERUN_BUILD.withState(ImageState.DISABLED).getImageDescriptor());

        registerJobChangeListener();
        registerLaunchConfigurationListener();
    }

    private void registerJobChangeListener() {
        Optional<ProcessDescription> processDescription = this.gradleConsole.getProcessDescription();
        if (processDescription.isPresent()) {
            Job job = processDescription.get().getJob();
            job.addJobChangeListener(new JobChangeAdapter() {

                @Override
                public void done(IJobChangeEvent event) {
                    update();
                }
            });
        }
        update();
    }

    private void registerLaunchConfigurationListener() {
        DebugPlugin.getDefault().getLaunchManager().addLaunchConfigurationListener(this);
    }

    @Override
    public void run() {
        this.gradleConsole.getProcessDescription().get().rerun();
    }

    @Override
    public void launchConfigurationAdded(ILaunchConfiguration configuration) {
    }

    @Override
    public void launchConfigurationChanged(ILaunchConfiguration configuration) {
    }

    @Override
    public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
        update();
    }

    private void update() {
        Optional<ProcessDescription> processDescription = this.gradleConsole.getProcessDescription();
        setEnabled(processDescription.isPresent() && processDescription.get().getJob().getState() == Job.NONE && processDescription.get().isRerunnable());
    }

    public void dispose() {
        DebugPlugin.getDefault().getLaunchManager().removeLaunchConfigurationListener(this);
    }

}
