/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel;

import com.google.common.base.Optional;
import org.gradle.api.specs.Spec;

import java.util.List;

/**
 * A hierarchical model belongs to a parent model, unless it is the root model, and may itself contain child models. All models in the hierarchy are of the same type.
 *
 * @param <T> the model type
 * @author Etienne Studer
 */
public interface HierarchicalModel<T extends HierarchicalModel<T>> {

    /**
     * Returns the root model of this model.
     *
     * @return the root model, never null
     */
    T getRoot();

    /**
     * Returns the parent model of this model.
     *
     * @return the parent model, can be null
     */
    T getParent();

    /**
     * Returns the immediate child models of this model.
     *
     * @return the immediate child models of this model
     */
    List<T> getChildren();

    /**
     * Returns this model and all the nested child models in its hierarchy.
     *
     * @return this model and all the nested child models in its hierarchy
     */
    List<T> getAll();

    /**
     * Returns all models that match the given criteria.
     *
     * @param predicate the criteria to match
     * @return the matching models
     */
    List<T> filter(Spec<? super T> predicate);

    /**
     * Returns the first model that matches the given criteria, if any.
     *
     * @param predicate the criteria to match
     * @return the matching model, if any
     */
    Optional<T> tryFind(Spec<? super T> predicate);

}
