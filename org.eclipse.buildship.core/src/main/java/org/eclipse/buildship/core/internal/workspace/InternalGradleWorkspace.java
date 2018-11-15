/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.workspace;

import java.util.Set;

import org.eclipse.buildship.core.GradleWorkspace;
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration;

/**
 * Manages the Gradle builds that are contained in the current Eclipse workspace.
 *
 * @author Stefan Oehme
 */
public interface InternalGradleWorkspace extends GradleWorkspace {

    /**
     * Returns the {@link InternalGradleBuild} represented by the given request attributes.
     *
     * @param buildConfiguration the configuration for the requested build
     * @return the Gradle build, never null
     */
    public InternalGradleBuild getGradleBuild(BuildConfiguration buildConfiguration);

    /**
     * Returns all Gradle builds from the workspace.
     *
     * @param projects the projects for which to find the corresponding builds
     * @return the set of Gradle builds
     */
    public Set<InternalGradleBuild> getGradleBuilds();
}
