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

package org.eclipse.buildship.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import org.eclipse.buildship.core.configuration.GradleProjectNature;

/**
 * Cleans up project artifacts created by previous versions of Buildship.
 */
public final class LegacyProjectUpdater {

    private LegacyProjectUpdater() {
    }

    public static void cleanupProjects() {
        Job cleanupJob = new Job("Cleanup Buildship projects") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                IWorkspaceRoot schedulingRule = ResourcesPlugin.getWorkspace().getRoot();
                try {
                    Job.getJobManager().beginRule(schedulingRule, monitor);
                    ImmutableList<IProject> allProjects = CorePlugin.workspaceOperations().getAllProjects();
                    for (IProject project : allProjects) {
                        if (project.isAccessible() && GradleProjectNature.INSTANCE.isPresentOn(project)) {
                            cleanupGradlePrefsFile(project);
                        }
                    }
                } finally {
                    Job.getJobManager().endRule(schedulingRule);
                }
                return Status.OK_STATUS;
            }
        };

        cleanupJob.setSystem(true);
        cleanupJob.schedule();
    }

    private static void cleanupGradlePrefsFile(IProject project) {
        // move the content from the gradle.prefs file to the eclipse preferences
        // and delete the source
        InputStream inputStream = null;
        try {
            IFile gradlePrefsFile = project.getFile(".settings/gradle.prefs");
            if (gradlePrefsFile.exists()) {
                ProjectScope scope = new ProjectScope(project);
                IEclipsePreferences preferences = scope.getNode(CorePlugin.PLUGIN_ID);
                if (preferences.get("PROJECT_CONFIGURATION", null) == null) {
                    // save project preferences
                    inputStream = gradlePrefsFile.getContents();
                    String json = CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
                    preferences.put("PROJECT_CONFIGURATION", json);
                    preferences.flush();

                    // delete the 'gradle' node
                    IEclipsePreferences oldNode = scope.getNode("gradle");
                    if (oldNode != null) {
                        oldNode.removeNode();
                    }

                    // delete the old gradle.prefs file
                    gradlePrefsFile.delete(true, null);
                }
            }
        } catch (Exception e) {
            CorePlugin.logger().error("Cleanup failed on project " + project.getName(), e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }
}
