/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.util.progress;

import java.util.Optional;
import java.util.stream.Collectors;

import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.ProgressListener;
import org.gradle.tooling.events.problems.BaseProblemDescriptor;
import org.gradle.tooling.events.problems.FileLocation;
import org.gradle.tooling.events.problems.LineInFileLocation;
import org.gradle.tooling.events.problems.ProblemAggregation;
import org.gradle.tooling.events.problems.ProblemAggregationDescriptor;
import org.gradle.tooling.events.problems.ProblemDescriptor;
import org.gradle.tooling.events.problems.ProblemEvent;
import org.gradle.tooling.events.problems.Solution;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.marker.GradleErrorMarker;
import org.eclipse.buildship.core.internal.util.gradle.Pair;
import org.eclipse.buildship.core.internal.workspace.InternalGradleBuild;

public class ProblemsReportingProgressListener implements ProgressListener {

    private InternalGradleBuild gradleBuild;

    public ProblemsReportingProgressListener(InternalGradleBuild gradleBuild) {
        this.gradleBuild = gradleBuild;
    }

    @Override
    public void statusChanged(ProgressEvent event) {
        if (event instanceof ProblemEvent) {
            ProblemEvent problemEvent = (ProblemEvent) event;
            BaseProblemDescriptor eventDescriptor = problemEvent.getDescriptor();
            try {
                if (eventDescriptor instanceof ProblemDescriptor) {
                    reportProblem((ProblemDescriptor) eventDescriptor);
                } else if (eventDescriptor instanceof ProblemAggregationDescriptor) {
                    for (ProblemAggregation aggregation : ((ProblemAggregationDescriptor) eventDescriptor).getAggregations()) {
                        for (ProblemDescriptor descriptor : aggregation.getProblemDescriptors()) {
                            reportProblem(descriptor);
                        }
                    }
                }
            } catch (Exception e) {
                CorePlugin.logger().warn("Cannot report problem " + problemEvent, e);
            }
        }
    }

    private void reportProblem(ProblemDescriptor descriptor) {
        Optional<Pair<IResource,Integer>> location = resourceAndFileNumberOfFirstFileLocation(descriptor);
        if (location.isPresent()) {
            GradleErrorMarker.createMarker(
                toMarkerSeverity(descriptor.getSeverity()),
                location.get().getFirst(), this.gradleBuild,
                descriptor.getLabel().getLabel(),
                null, // TODO (donat) Gradle 8.7 descriptor.getException().getException(),
                location.get().getSecond(),
                toPath(descriptor.getCategory()),
                descriptor.getSolutions().stream().map(Solution::getSolution).collect(Collectors.toList()),
                descriptor.getDocumentationLink().getUrl()
            );
        } else {
            GradleErrorMarker.createMarker(
                toMarkerSeverity(descriptor.getSeverity()),
                ResourcesPlugin.getWorkspace().getRoot(),
                this.gradleBuild,
                descriptor.getLabel().getLabel(),
                null, // TODO (donat) Gradle 8.7 descriptor.getException().getException(),
                -1,
                toPath(descriptor.getCategory()),
                descriptor.getSolutions().stream().map(Solution::getSolution).collect(Collectors.toList()),
                descriptor.getDocumentationLink().getUrl()
            );
        }
    }

    private static String toPath(org.gradle.tooling.events.problems.ProblemCategory problemCategory) {
        StringBuilder sb = new StringBuilder();
        sb.append(problemCategory.getNamespace());
        sb.append(':');
        sb.append(problemCategory.getCategory());
        for (String sc : problemCategory.getSubcategories()) {
            sb.append(':');
            sb.append(sc);
        }
        return sb.toString();
    }

    public Optional<FileLocation> firstFileLocation(ProblemDescriptor descriptor) {
        return descriptor.getLocations().stream().filter(FileLocation.class::isInstance).map(FileLocation.class::cast).findFirst();
    }

    public Optional<Pair<IResource, Integer>> resourceAndFileNumberOfFirstFileLocation(ProblemDescriptor descriptor) {
        return firstFileLocation(descriptor).map(location -> new Pair<>(toResource(location), lineNumberOf(location)));
    }

    private static IResource toResource(FileLocation location) {
        IPath absolutePath = Path.fromOSString(location.getPath());
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        IFile workspacePath = workspaceRoot.getFileForLocation(absolutePath);
        if (workspacePath.exists()) {
            return workspacePath;
        }
        return ResourcesPlugin.getWorkspace().getRoot();
    }

    private static Integer lineNumberOf(FileLocation location) {
        if (location instanceof LineInFileLocation) {
            if (((LineInFileLocation) location).getLine() > 0) {
                return ((LineInFileLocation) location).getLine();
            }
        }
        return -1;
    }

    public int toMarkerSeverity(org.gradle.tooling.events.problems.Severity severity) {
        if (severity == org.gradle.tooling.events.problems.Severity.ERROR) {
            return IMarker.SEVERITY_ERROR;
        } else if (severity == org.gradle.tooling.events.problems.Severity.ADVICE) {
            return IMarker.SEVERITY_INFO;
        } else {
            return IMarker.SEVERITY_WARNING;
        }
    }
}
