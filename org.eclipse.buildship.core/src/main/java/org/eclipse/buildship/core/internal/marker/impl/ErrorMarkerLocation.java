/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.marker.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.GradleProjectNature;
import org.eclipse.buildship.core.internal.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.internal.preferences.PersistentModel;
import org.eclipse.buildship.core.internal.workspace.GradleBuild;

/**
 * Calculates the location of a new error marker.
 *
 * @author Donat Csikos
 */
public final class ErrorMarkerLocation {

    private static final Pattern ERROR_LOCATION_PATTERN = Pattern.compile("Build file '(.+)' line: (\\d+).*");

    private final IResource resource;
    private final int lineNumber;

    public ErrorMarkerLocation(IResource resource) {
        this(resource, 0);
    }

    public ErrorMarkerLocation(IResource resource, int lineNumber) {
        this.resource = resource;
        this.lineNumber = lineNumber;
    }

    public IResource getResource() {
        return this.resource;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public static ErrorMarkerLocation findErrorLocation(GradleBuild gradleBuild, Throwable t) {
        Matcher errorLocationInStackTrace = tryFindStackTraceErrorLocation(t);

        if (errorLocationInStackTrace != null) {
            IResource resource = tryFindWorkspaceFile(gradleBuild, new Path(errorLocationInStackTrace.group(1)));
            if (resource != null) {
                int lineNumber = Integer.parseInt(errorLocationInStackTrace.group(2));
                return new ErrorMarkerLocation(resource, lineNumber);
            }
        }

        return new ErrorMarkerLocation(calculateFallbackLocation(gradleBuild));
    }

    private static Matcher tryFindStackTraceErrorLocation(Throwable t) {
        if (t == null || t.getMessage() == null) {
            return null;
        }

        Matcher matcher = ERROR_LOCATION_PATTERN.matcher(t.getMessage());
        if (!matcher.find()) {
            return tryFindStackTraceErrorLocation(t.getCause());
        }

        return matcher;
    }

    private static IResource tryFindWorkspaceFile(GradleBuild gradleBuild, Path filePath) {
        for (IProject project : getWorkspaceProjectsFor(gradleBuild)) {
            IPath projectLocation = project.getLocation();
            if (projectLocation.isPrefixOf(filePath)) {
                IFile file = project.getFile(filePath.makeRelativeTo(projectLocation));
                if (file.exists()) {
                    return file;
                }
            }
        }

        return null;
    }

    private static IResource calculateFallbackLocation(GradleBuild gradleBuild) {
        Optional<IProject> projectOrNull = CorePlugin.workspaceOperations().findProjectByLocation(gradleBuild.getBuildConfig().getRootProjectDirectory());
        if (projectOrNull.isPresent()) {
            IProject project = projectOrNull.get();
            if (GradleProjectNature.isPresentOn(project)) {
                PersistentModel persistentModel = CorePlugin.modelPersistence().loadModel(project);
                if (persistentModel.isPresent()) {
                    IFile buildScript = project.getFile(persistentModel.getbuildScriptPath());
                    if (buildScript.exists()) {
                        return buildScript;
                    }
                }
            }
            return project;
        } else {
            return ResourcesPlugin.getWorkspace().getRoot();
        }
    }

    private static Iterable<IProject> getWorkspaceProjectsFor(final GradleBuild gradleBuild) {
        return FluentIterable.from(CorePlugin.workspaceOperations().getAllProjects()).filter(new Predicate<IProject>() {

            @Override
            public boolean apply(IProject project) {
                if (!GradleProjectNature.isPresentOn(project)) {
                    return false;
                }
                ProjectConfiguration projectConfiguration = CorePlugin.configurationManager().tryLoadProjectConfiguration(project);
                if (projectConfiguration == null) {
                    return false;
                }
                return projectConfiguration.getBuildConfiguration().equals(gradleBuild.getBuildConfig());
            }
        }).toList();
    }
}
