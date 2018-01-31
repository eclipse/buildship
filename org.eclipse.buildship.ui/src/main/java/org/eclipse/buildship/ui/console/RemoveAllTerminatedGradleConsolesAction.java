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

import java.util.Arrays;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleManager;

import org.eclipse.buildship.core.console.ProcessDescription;
import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;

/**
 * Removes all finished {@link org.eclipse.debug.core.ILaunch} instances associated with a {@link GradleConsole} instance.
 * The action is only enabled if at least one console can be removed.
 */
public final class RemoveAllTerminatedGradleConsolesAction extends Action {

    private final GradleConsole gradleConsole;

    public RemoveAllTerminatedGradleConsolesAction(GradleConsole gradleConsole) {
        this.gradleConsole = Preconditions.checkNotNull(gradleConsole);

        setToolTipText(ConsoleMessages.Action_RemoveAllTerminatedConsoles_Tooltip);
        setImageDescriptor(PluginImages.REMOVE_ALL_CONSOLES.withState(ImageState.ENABLED).getImageDescriptor());
        setDisabledImageDescriptor(PluginImages.REMOVE_ALL_CONSOLES.withState(ImageState.DISABLED).getImageDescriptor());

        registerJobChangeListener();
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
            update();
        } else {
            // if no job is associated with the console, never enable this action
            setEnabled(false);
        }
    }

    private void update() {
        setEnabled(this.gradleConsole.isCloseable() && this.gradleConsole.isTerminated());
    }

    @Override
    public void run() {
        ImmutableList<GradleConsole> terminatedConsoles = getTerminatedConsoles();
        ConsolePlugin.getDefault().getConsoleManager().removeConsoles(terminatedConsoles.toArray(new GradleConsole[terminatedConsoles.size()]));
    }

    private ImmutableList<GradleConsole> getTerminatedConsoles() {
        IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
        return FluentIterable.from(Arrays.asList(consoleManager.getConsoles())).filter(GradleConsole.class).filter(new Predicate<GradleConsole>() {

            @Override
            public boolean apply(GradleConsole console) {
                return console.isCloseable() && console.isTerminated();
            }
        }).toList();
    }

    public void dispose() {
    }

}
