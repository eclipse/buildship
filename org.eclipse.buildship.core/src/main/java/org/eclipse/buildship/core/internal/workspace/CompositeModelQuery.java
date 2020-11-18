/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

import org.gradle.api.Action;
import org.gradle.tooling.BuildAction;
import org.gradle.tooling.BuildController;
import org.gradle.tooling.model.gradle.GradleBuild;

/**
 * Build action to query a model for all participants in a composite.
 *
 * @param <T> The requested model type
 * @author Donat Csikos
 */
public final class CompositeModelQuery<T, U> implements BuildAction<Collection<T>> {

    private static final long serialVersionUID = 1L;

    private final Class<T> modelType;

    private final Action<? super U> parameter;

    private Class<U> parameterType;

    public CompositeModelQuery(Class<T> modelType) {
        this(modelType, null, null);
    }

    public CompositeModelQuery(Class<T> modelType, Class<U> parameterType, Action<? super U> parameter) {
        this.modelType = modelType;
        this.parameterType = parameterType;
        this.parameter = parameter;
    }

    @Override
    public Collection<T> execute(BuildController controller) {
        Collection<T> models = new ArrayList<>();
        Collection<File> visitedBuilds = new HashSet<>();
        collectRootModels(controller, controller.getBuildModel(), models, visitedBuilds);
        return models;
    }

    private void collectRootModels(BuildController controller, GradleBuild build, Collection<T> models, Collection<File> visitedBuilds) {
        if (this.parameter != null) {
            models.add(controller.getModel(build.getRootProject(), this.modelType, this.parameterType, this.parameter));
        } else {
            models.add(controller.getModel(build.getRootProject(), this.modelType));

        }

        for (GradleBuild includedBuild : build.getIncludedBuilds()) {
        	if (!visitedBuilds.contains(includedBuild.getRootProject().getProjectDirectory())) {
        		visitedBuilds.add(includedBuild.getRootProject().getProjectDirectory());
        		collectRootModels(controller, includedBuild, models, visitedBuilds);
        	}
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.modelType, this.parameter);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CompositeModelQuery<?, ?> other = (CompositeModelQuery<?, ?>) obj;
        return Objects.equals(this.modelType, other.modelType) && Objects.equals(this.parameter, other.parameter);
    }

}
