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

package org.eclipse.buildship.ui.taskview;

import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;

import org.eclipse.buildship.core.configuration.ProjectConfiguration;

/**
 * Encapsulates the content backing the {@link TaskView}. The content consists 1) of the set of root
 * {@link ProjectConfiguration} instances and 2) the {@link FetchStrategy} to apply when querying
 * for the Gradle project/task models in the content provider for the given root project
 * configurations.
 */
public final class TaskViewContent {

    private final ImmutableSet<ProjectConfiguration> rootProjectConfigurations;
    private final FetchStrategy modelFetchStrategy;

    public TaskViewContent(Set<ProjectConfiguration> rootProjectConfigurations, FetchStrategy modelFetchStrategy) {
        this.rootProjectConfigurations = ImmutableSet.copyOf(rootProjectConfigurations);
        this.modelFetchStrategy = Preconditions.checkNotNull(modelFetchStrategy);
    }

    public ImmutableSet<ProjectConfiguration> getRootProjectConfigurations() {
        return this.rootProjectConfigurations;
    }

    public FetchStrategy getModelFetchStrategy() {
        return this.modelFetchStrategy;
    }

}
