/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.workspace;

import java.util.ArrayList;
import java.util.Collection;
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
        collectRootModels(controller, controller.getBuildModel(), models);
        return models;
    }

    private void collectRootModels(BuildController controller, GradleBuild build, Collection<T> models) {
        if (this.parameter != null) {
            models.add(controller.getModel(build.getRootProject(), this.modelType, this.parameterType, this.parameter));
        } else {
            models.add(controller.getModel(build.getRootProject(), this.modelType));

        }

        for (GradleBuild includedBuild : build.getIncludedBuilds()) {
            collectRootModels(controller, includedBuild, models);
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
