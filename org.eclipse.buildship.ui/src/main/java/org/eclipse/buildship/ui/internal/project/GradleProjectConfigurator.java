/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.project;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.eclipse.buildship.core.BuildConfiguration;
import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.GradleCore;
import org.eclipse.buildship.core.internal.configuration.GradleProjectNature;
import org.eclipse.buildship.core.internal.workspace.NewProjectHandler;
import org.eclipse.buildship.core.internal.workspace.SynchronizationJob;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.wizards.datatransfer.ProjectConfigurator;


public class GradleProjectConfigurator implements ProjectConfigurator {

  private static final String[] ROOT_FILES = new String[] {"settings.gradle", "settings.gradle.kts", "gradle.properties"};

  @Override
  public Set<File> findConfigurableLocations(File root, IProgressMonitor monitor) {
    if(isGradleRoot(root)) {
      ImmutableSet.of(root);
    }
    return ImmutableSet.of();
  }

  @Override
  public void removeDirtyDirectories(Map<File, List<ProjectConfigurator>> proposals) {
    Set<File> gradleRoots = new HashSet<>();
    Set<File> nonGradleRoots = new HashSet<>();
    for(Entry<File, List<ProjectConfigurator>> entry : proposals.entrySet()) {
      File root = entry.getKey();
      if(isGradleRoot(root)) {
        entry.getValue().removeIf(pc -> !(pc instanceof GradleProjectConfigurator));
        gradleRoots.add(root);
      } else {
        nonGradleRoots.add(root);
      }
    }
    if(!gradleRoots.isEmpty()) {
      for(File file : nonGradleRoots) {
        proposals.remove(file);
      }
    }
  }

  private boolean isGradleRoot(File root) {
    if(root != null) {
        for (String file : ROOT_FILES) {
            if(new File(root, file).isFile()) {
               return true;
            }
        }
    }
    return false;
  }

  @Override
  public boolean shouldBeAnEclipseProject(IContainer container, IProgressMonitor monitor) {
    IPath location = container.getLocation();
    return location != null && isGradleRoot(location.toFile());
  }

  @Override
  public Set<IFolder> getFoldersToIgnore(IProject project, IProgressMonitor monitor) {
    return ImmutableSet.of();
  }

  @Override
  public boolean canConfigure(IProject project, Set<IPath> ignoredPaths, IProgressMonitor monitor) {
    return shouldBeAnEclipseProject(project, monitor);
  }

  @Override
  public void configure(IProject project, Set<IPath> ignoredPaths, IProgressMonitor monitor) {
      if (!GradleProjectNature.isPresentOn(project)) {
          BuildConfiguration buildConfig = BuildConfiguration.forRootProjectDirectory(project.getLocation().toFile()).build();
          GradleBuild build = GradleCore.getWorkspace().createBuild(buildConfig);
          new SynchronizationJob(NewProjectHandler.IMPORT_AND_MERGE, ImmutableList.of(build)).schedule();
      }
  }

}
