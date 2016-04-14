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

package org.eclipse.buildship.ui.view.task;

import com.google.common.base.Preconditions;

import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;

/**
 * Encapsulates the content backing the {@link TaskView}. The content consists of the
 * {@link FetchStrategy} to apply when querying for the Gradle project/task models in the content
 * provider.
 */
public final class TaskViewContent {

    private final FetchStrategy modelFetchStrategy;

    public TaskViewContent(FetchStrategy modelFetchStrategy) {
        this.modelFetchStrategy = Preconditions.checkNotNull(modelFetchStrategy);
    }

    public FetchStrategy getModelFetchStrategy() {
        return this.modelFetchStrategy;
    }

}
