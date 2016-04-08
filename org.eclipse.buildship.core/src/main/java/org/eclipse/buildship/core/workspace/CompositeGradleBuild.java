/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.core.workspace;

import java.util.Set;

/**
 * A set of {@link GradleBuild}s that are built together.

 * @author Stefan Oehme
 *
 */
//TODO use composite build infrastructure instead of iterating over the builds internally
public interface CompositeGradleBuild {

    /**
     * Attempts to synchronize all contained builds with the workspace.
     * <p/>
     * The synchronization happens asynchronously. In case of a failure, the user will be notified
     * once after all builds have finished.
     *
     * @param newProjectHandler how to handle newly added projects
     */
    void synchronize(NewProjectHandler newProjectHandler);

    /**
     * Returns the builds participating in this composite.
     * @return the builds, never null
     */
    //TODO remove as soon as composite build can handle everything you can do with a single build
    Set<GradleBuild> getParticipantBuilds();
}
