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
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.gradle.tooling.events.problems.ProblemGroup;
import org.gradle.tooling.events.problems.SingleProblemEvent;
import org.gradle.tooling.events.problems.Solution;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.buildship.core.internal.marker.GradleErrorMarker;

public class ProblemEventAdapter implements Consumer<IMarker> {

    private final SingleProblemEvent problem;

    public ProblemEventAdapter(SingleProblemEvent problem) {
        this.problem = problem;
    }

    @Override
    public void accept(IMarker marker) {
        try {
            String idInfo = this.problem.getDefinition().getId().getDisplayName() + " (id: " + fqid() + ")";
            marker.setAttribute(GradleErrorMarker.ATTRIBUTE_ID, idInfo);
            marker.setAttribute(GradleErrorMarker.ATTRIBUTE_LABEL, this.problem.getContextualLabel().getContextualLabel());
            marker.setAttribute(GradleErrorMarker.ATTRIBUTE_DETAILS, this.problem.getDetails().getDetails());
            List<Solution> solutions = this.problem.getSolutions();
            if (solutions != null) {
                String solutionsString = solutions.stream().map(Solution::getSolution).collect(Collectors.joining(System.getProperty("line.separator")));
                marker.setAttribute(GradleErrorMarker.ATTRIBUTE_SOLUTIONS, solutionsString);
            }
            String documentationLink = this.problem.getDefinition().getDocumentationLink().getUrl();
            if (documentationLink != null) {
                marker.setAttribute(GradleErrorMarker.ATTRIBUTE_DOCUMENTATION_LINK, documentationLink);
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    private String fqid() {
        return groupFqid(this.problem.getDefinition().getId().getGroup()) + ":" + this.problem.getDefinition().getId().getName();
    }

    private String groupFqid(ProblemGroup group) {
        if (group.getParent() == null) {
            return group.getName();
        } else {
            return groupFqid(group.getParent()) + ":" + group.getName();
        }
    }
}
