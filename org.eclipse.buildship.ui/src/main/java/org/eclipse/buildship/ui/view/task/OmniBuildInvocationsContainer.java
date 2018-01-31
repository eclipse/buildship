/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.view.task;

import java.util.SortedMap;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSortedMap;

import org.eclipse.buildship.core.util.gradle.Path;


/**
 * TODO This class should be merged with the TaskViewContentProvider.
 *
 * @author Donat Csikos
 */
public final class OmniBuildInvocationsContainer {

    private final ImmutableSortedMap<Path, OmniBuildInvocations> buildInvocationsPerProject;

    private OmniBuildInvocationsContainer(SortedMap<Path, OmniBuildInvocations> buildInvocationsPerProject) {
        this.buildInvocationsPerProject = ImmutableSortedMap.copyOfSorted(buildInvocationsPerProject);
    }

    public Optional<OmniBuildInvocations> get(Path projectPath) {
        return Optional.fromNullable(this.buildInvocationsPerProject.get(projectPath));
    }

    public ImmutableSortedMap<Path, OmniBuildInvocations> asMap() {
        return this.buildInvocationsPerProject;
    }

    public static OmniBuildInvocationsContainer from(SortedMap<Path, OmniBuildInvocations> buildInvocationsPerProject) {
        return new OmniBuildInvocationsContainer(buildInvocationsPerProject);
    }
}
