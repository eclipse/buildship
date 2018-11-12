/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.workspace;

import java.util.Collection;
import java.util.Set;

import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.collect.ImmutableSet;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.internal.util.gradle.HierarchicalElementUtils;

public class ModelProviderUtil {

    /**
     * @return a flattened list of Eclipse projects for all projects included in the Gradle build
     */
    public static Set<EclipseProject> fetchAllEclipseProjects(GradleBuild build, CancellationTokenSource tokenSource, FetchStrategy fetchStrategy, IProgressMonitor monitor) {
        ModelProvider modelProvider = build.getModelProvider();

        Collection<EclipseProject> models = modelProvider.fetchModels(EclipseProject.class, fetchStrategy, tokenSource, monitor);
        ImmutableSet.Builder<EclipseProject> result = ImmutableSet.builder();
        for (EclipseProject model : models) {
            result.addAll(HierarchicalElementUtils.getAll(model));
        }
        return result.build();
    }
}
