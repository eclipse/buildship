/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.launch;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.gradle.tooling.TestLauncher;

import com.google.common.collect.Sets;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.launch.BaseLaunchRequestJob;
import org.eclipse.buildship.core.internal.launch.JavaElementSelection;
import org.eclipse.buildship.core.internal.launch.RunGradleJvmTestLaunchRequestJob;
import org.eclipse.buildship.model.ProjectInGradleConfiguration;
import org.eclipse.buildship.model.SourceSet;
import org.eclipse.buildship.model.TestTask;

/**
 * Shortcut for Gradle test launches from the Java editor or from the current selection.
 */
public final class TestLaunchShortcut implements ILaunchShortcut {

    @Override
    public void launch(ISelection selection, String mode) {
        SelectionJavaElementResolver resolver = SelectionJavaElementResolver.from(selection);
        launch(resolver, mode);
    }

    @Override
    public void launch(IEditorPart editor, String mode) {
        launch(EditorBackedJavaElementSelection.from(editor), mode);
    }

    private void launch(JavaElementSelection selection, String mode) {
        String testTask = null;
        com.google.common.base.Optional<IProject> containerProject = selection.findFirstContainerProject();
        if (containerProject.isPresent()) {

            List<IMember> selectedMembers = new ArrayList<>();
            selectedMembers.addAll(selection.getSelectedTypes());
            selectedMembers.addAll(selection.getSelectedMethods());
            IMember iMember = selectedMembers.get(0);
            ICompilationUnit compilationUnit = iMember.getCompilationUnit();
            final File selectedFile = containerProject.get().getLocation().append(compilationUnit.getPath().makeRelativeTo(containerProject.get().getFullPath())).toFile();

            // Find all source sets that contain this file
            ProjectInGradleConfiguration projectInGradleConfiguration = CorePlugin.modelPersistence().loadModel(containerProject.get()).getProjectInGradleConfiguration();

            Set<? extends SourceSet> sourceSets = projectInGradleConfiguration.getSourceSets();
            Set<SourceSet> selectedSourceSets = new LinkedHashSet<>();
            for (SourceSet sourceSet : sourceSets) {
               Set<File> srcDirs = sourceSet.getSrcDirs();
               for (File srcDir : srcDirs) {
                   if (selectedFile.toString().startsWith(srcDir.toString())) {
                       selectedSourceSets.add(sourceSet);
                   }
               }
            }

            // Find all test tasks that contain the source sets
            Set<? extends TestTask> testTasks = projectInGradleConfiguration.getTestTasks();
            Set<TestTask> selectedTestTasks = new LinkedHashSet<>();
            for (TestTask tt : testTasks) {
                for (SourceSet ss : selectedSourceSets) {
                    if (!Sets.intersection(tt.getTestClassesDirs(), ss.getRuntimeClasspath()).isEmpty()) {
                        selectedTestTasks.add(tt);
                    }
                }

            }
            List<String> testTaskPaths = selectedTestTasks.stream().map(TestTask::getPath).collect(Collectors.toList());

            testTask = selectTestTask(testTaskPaths);
        }

        Optional<BaseLaunchRequestJob<TestLauncher>> job = RunGradleJvmTestLaunchRequestJob.createJob(selection, mode, testTask);
        job.ifPresent(Job::schedule);
    }

    private String selectTestTask(final List<String> options) {
        if (options.isEmpty()) {
            return null;
        }
        // TODO allow single item selection only
        ListSelectionDialog dialog = ListSelectionDialog.of(options).title("Select test task").asSheet(true).canCancel(false).preselect(new Object[] { options.get(0) })
                .create(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());

        dialog.open();

        Object[] result = dialog.getResult();
        if (result.length > 0) {
            return result[0].toString();
        } else {
            return null;
        }
    }
}
