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

import java.util.List;
import java.util.Optional;

import org.gradle.tooling.Failure;
import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.ProgressListener;
import org.gradle.tooling.events.problems.FileLocation;
import org.gradle.tooling.events.problems.LineInFileLocation;
import org.gradle.tooling.events.problems.Location;
import org.gradle.tooling.events.problems.OffsetInFileLocation;
import org.gradle.tooling.events.problems.ProblemAggregationEvent;
import org.gradle.tooling.events.problems.ProblemEvent;
import org.gradle.tooling.events.problems.SingleProblemEvent;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.marker.GradleErrorMarker;
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
            try {
                if (problemEvent instanceof SingleProblemEvent) {
                    reportProblem((SingleProblemEvent) problemEvent);
                } else if (problemEvent instanceof ProblemAggregationEvent) {
                }
            } catch (Exception e) {
                CorePlugin.logger().warn("Cannot report problem " + problemEvent, e);
            }
        }
    }

    private void reportProblem(SingleProblemEvent event) {
        List<Location> locations = event.getLocations();

        // 1/4 offset in file location
        Optional<OffsetInFileLocation> offsetInFileLocation = locations.stream().filter(OffsetInFileLocation.class::isInstance).map(OffsetInFileLocation.class::cast).findFirst();
        if (offsetInFileLocation.isPresent()) {
            IResource resource = toResource(offsetInFileLocation.get());

            GradleErrorMarker.createProblemMarker(
                    toMarkerSeverity(event.getDefinition().getSeverity()),
                    resource,
                    this.gradleBuild,
                    markerMessage(event),
                    stacktraceStringFor(event.getFailure().getFailure()),
                    marker -> {
                        OffsetInFileLocation location = offsetInFileLocation.get();
                        int startOffset = location.getOffset();
                        int endOffset =  location.getLength();
                        try {
                            marker.setAttribute(IMarker.CHAR_START, startOffset);
                            marker.setAttribute(IMarker.CHAR_END, startOffset + endOffset);
                        } catch (CoreException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    new ProblemEventAdapter(event)
                );
            return;
        }

        // 2/4 line in file location
        Optional<LineInFileLocation> lineInFileLocation = locations.stream().filter(LineInFileLocation.class::isInstance).map(LineInFileLocation.class::cast).findFirst();
        if (lineInFileLocation.isPresent()) {
            IResource resource = toResource(lineInFileLocation.get());
            GradleErrorMarker.createProblemMarker(
                    toMarkerSeverity(event.getDefinition().getSeverity()),
                    resource,
                    this.gradleBuild,
                    markerMessage(event),
                    stacktraceStringFor(event.getFailure().getFailure()),
                    marker -> {
                        Integer lineNumber = lineNumberOf(lineInFileLocation.get());
                        if (lineNumber >= 0) {
                            try {
                                marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
                            } catch (CoreException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    },
                    new ProblemEventAdapter(event)
                );
            return;
        }

        // 3/4 file location
        Optional<FileLocation> fileLocation = locations.stream().filter(FileLocation.class::isInstance).map(FileLocation.class::cast).findFirst();
        if (fileLocation.isPresent()) {
            IResource resource = toResource(fileLocation.get());
            GradleErrorMarker.createProblemMarker(
                    toMarkerSeverity(event.getDefinition().getSeverity()),
                    resource,
                    this.gradleBuild,
                    markerMessage(event),
                    stacktraceStringFor(event.getFailure().getFailure()),
                    m -> {},
                    new ProblemEventAdapter(event)
                );
            return;
        }

        // 4/4 no location
        GradleErrorMarker.createProblemMarker(
            toMarkerSeverity(event.getDefinition().getSeverity()),
            ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(this.gradleBuild.getBuildConfig().getRootProjectDirectory().getAbsolutePath())),
            this.gradleBuild,
            markerMessage(event),
            stacktraceStringFor(event.getFailure().getFailure()),
            m -> {},
            new ProblemEventAdapter(event)
        );
    }

    private static String markerMessage(SingleProblemEvent problem ) {
        String result = problem.getDetails().getDetails();
        if (result == null) {
            result = problem.getContextualLabel().getContextualLabel();
        }
        if (result == null) {
            result = problem.getDefinition().getId().getDisplayName();
        }

        return result == null ? "" : result;
    }

    private static String stacktraceStringFor(Failure failure) {
        if (failure == null) {
            return null;
        }
        return failure.getDescription();
    }

    private static IResource toResource(FileLocation location) {
        IPath absolutePath = Path.fromOSString(location.getPath());
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        IFile workspacePath = workspaceRoot.getFileForLocation(absolutePath);
        if (workspacePath != null && workspacePath.exists()) {
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
