/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.launch;

import java.util.Optional;

import org.gradle.tooling.TestLauncher;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;

import org.eclipse.buildship.core.internal.launch.BaseLaunchRequestJob;
import org.eclipse.buildship.core.internal.launch.JavaElementSelection;
import org.eclipse.buildship.core.internal.launch.RunGradleJvmTestLaunchRequestJob;

/**
 * Shortcut for Gradle test launches from the Java editor or from the current selection.
 */
public final class TestLaunchShortcut implements ILaunchShortcut {

    @Override
    public void launch(ISelection selection, String mode) {
        launch(SelectionJavaElementResolver.from(selection), mode);
    }

    @Override
    public void launch(IEditorPart editor, String mode) {
        launch( EditorBackedJavaElementSelection.from(editor), mode);
    }

    private void launch(JavaElementSelection selection, String mode) {
        Optional<BaseLaunchRequestJob<TestLauncher>> job = RunGradleJvmTestLaunchRequestJob.createJob(selection, mode);
        job.ifPresent(Job::schedule);
    }
}
