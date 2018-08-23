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
import org.eclipse.buildship.core.console.ProcessDescription;
import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.buildship.ui.i18n.UiMessages;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.jface.action.Action;

/**
 * Reruns the build represented by the target {@link ExecutionPage}.
 *
 * Note: we listen for removals of {@code ILaunchConfiguration} instances even though not every {@code ProcessDescription} implementation
 * is necessarily backed by a launch configuration. This means that in the worst case, {@code ProcessDescription#isRerunnable()} is invoked
 * unnecessarily (which does no harm).
 */
public final class RerunBuildExecutionAction extends Action implements ILaunchConfigurationListener {

    private final ExecutionPage page;

    public RerunBuildExecutionAction(ExecutionPage executionPage) {
        this.page = Preconditions.checkNotNull(executionPage);

        setToolTipText(UiMessages.Action_RerunBuild_Tooltip);
        setImageDescriptor(PluginImages.RERUN_BUILD.withState(ImageState.ENABLED).getImageDescriptor());
        setDisabledImageDescriptor(PluginImages.RERUN_BUILD.withState(ImageState.DISABLED).getImageDescriptor());

        registerJobChangeListener();
        registerLaunchConfigurationListener();
    }

    private void registerJobChangeListener() {
        Job job = this.page.getProcessDescription().getJob();
        job.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(IJobChangeEvent event) {
                update();
            }
        });
        update();
    }

    private void registerLaunchConfigurationListener() {
        DebugPlugin.getDefault().getLaunchManager().addLaunchConfigurationListener(this);
    }

    @Override
    public void run() {
        this.page.getProcessDescription().rerun();
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
        ProcessDescription processDescription = this.page.getProcessDescription();
        setEnabled(processDescription.getJob().getState() == Job.NONE && processDescription.isRerunnable());
    }

    public void dispose() {
        DebugPlugin.getDefault().getLaunchManager().removeLaunchConfigurationListener(this);
    }

}