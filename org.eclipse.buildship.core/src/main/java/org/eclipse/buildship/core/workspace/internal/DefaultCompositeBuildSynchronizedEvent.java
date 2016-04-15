/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.buildship.core.workspace.internal;

import org.eclipse.buildship.core.workspace.CompositeBuildSynchronizedEvent;
import org.eclipse.buildship.core.workspace.CompositeGradleBuild;

/**
 * Default implementation of {@link CompositeBuildSynchronizedEvent}.
 *
 * @author Stefan Oehme
 *
 */
public class DefaultCompositeBuildSynchronizedEvent implements CompositeBuildSynchronizedEvent {

    private final CompositeGradleBuild compositeBuild;

    public DefaultCompositeBuildSynchronizedEvent(CompositeGradleBuild compositeBuild) {
        this.compositeBuild = compositeBuild;
    }

    @Override
    public CompositeGradleBuild getCompositeBuild() {
        return this.compositeBuild;
    }

}
