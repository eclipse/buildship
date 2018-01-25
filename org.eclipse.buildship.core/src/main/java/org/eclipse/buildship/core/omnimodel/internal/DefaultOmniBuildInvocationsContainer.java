/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel.internal;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.gradle.tooling.model.gradle.BuildInvocations;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSortedMap;

import org.eclipse.buildship.core.omnimodel.OmniBuildInvocations;
import org.eclipse.buildship.core.omnimodel.OmniBuildInvocationsContainer;
import org.eclipse.buildship.core.omnimodel.OmniGradleProject;
import org.eclipse.buildship.core.util.gradle.Path;

/**
 * Default implementation of the {@link OmniBuildInvocationsContainer} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniBuildInvocationsContainer implements OmniBuildInvocationsContainer {

    private final ImmutableSortedMap<Path, OmniBuildInvocations> buildInvocationsPerProject;

    private DefaultOmniBuildInvocationsContainer(SortedMap<Path, OmniBuildInvocations> buildInvocationsPerProject) {
        this.buildInvocationsPerProject = ImmutableSortedMap.copyOfSorted(buildInvocationsPerProject);
    }

    @Override
    public Optional<OmniBuildInvocations> get(Path projectPath) {
        return Optional.fromNullable(this.buildInvocationsPerProject.get(projectPath));
    }

    @Override
    public ImmutableSortedMap<Path, OmniBuildInvocations> asMap() {
        return this.buildInvocationsPerProject;
    }

    public static OmniBuildInvocationsContainer from(Map<String, BuildInvocations> buildInvocationsPerProject) {
        ImmutableSortedMap.Builder<Path, OmniBuildInvocations> buildInvocationsMap = ImmutableSortedMap.orderedBy(Path.Comparator.INSTANCE);
        for (String projectPath : buildInvocationsPerProject.keySet()) {
            buildInvocationsMap.put(Path.from(projectPath), DefaultOmniBuildInvocations.from(buildInvocationsPerProject.get(projectPath), Path.from(projectPath)));
        }
        return new DefaultOmniBuildInvocationsContainer(buildInvocationsMap.build());
    }

    public static DefaultOmniBuildInvocationsContainer from(SortedMap<Path, OmniBuildInvocations> buildInvocationsPerProject) {
        return new DefaultOmniBuildInvocationsContainer(buildInvocationsPerProject);
    }

    public static OmniBuildInvocationsContainer from(OmniGradleProject gradleProject) {
        ImmutableSortedMap.Builder<Path, OmniBuildInvocations> result = ImmutableSortedMap.orderedBy(Path.Comparator.INSTANCE);
        collectBuildInvocations(gradleProject, result);
        return new DefaultOmniBuildInvocationsContainer(result.build());
    }

    private static void collectBuildInvocations(OmniGradleProject project, ImmutableSortedMap.Builder<Path, OmniBuildInvocations> result) {
        result.put(project.getPath(), DefaultOmniBuildInvocations.from(project.getProjectTasks(), project.getTaskSelectors()));

        List<OmniGradleProject> children = project.getChildren();
        for (OmniGradleProject child : children) {
            collectBuildInvocations(child, result);
        }
    }

}
