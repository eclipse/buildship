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

import java.util.HashMap;
import java.util.Map;
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
public final class CompositeModelQuery<T, U> implements BuildAction<Map<String, T>> {

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
    public Map<String, T> execute(BuildController controller) {
        Map<String,T> acc = new HashMap<>();
        // ':' represents the root build
        collectRootModels(controller, controller.getBuildModel(), acc, ":", controller.getBuildModel().getRootProject().getName());
        return acc;
    }

    private void collectRootModels(BuildController controller, GradleBuild build, Map<String, T> models, String buildPath, String rootBuildRootProjectName) {
        if (models.containsKey(buildPath)) {
            return; // can happen when there's a cycle in the included builds
        }

        if (this.parameter != null) {
            models.put(buildPath, controller.getModel(build.getRootProject(), this.modelType, this.parameterType, this.parameter));
        } else {
            models.put(buildPath, controller.getModel(build.getRootProject(), this.modelType));
        }

        for (GradleBuild includedBuild : build.getEditableBuilds()) { // TODO add cross-version coverage
            String includedBuildRootProjectName = includedBuild.getRootProject().getName();
            if (!includedBuildRootProjectName.equals(rootBuildRootProjectName)) {
                collectRootModels(controller, includedBuild, models, includedBuildRootProjectName, rootBuildRootProjectName);
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
