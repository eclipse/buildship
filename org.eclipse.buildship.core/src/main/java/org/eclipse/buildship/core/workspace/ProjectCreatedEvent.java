/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.workspace;

import com.google.common.collect.ImmutableList;
import org.eclipse.buildship.core.event.Event;
import org.eclipse.core.resources.IProject;

/**
 * Event informing that a new {@link IProject} has been created during a Gradle project import.
 */
public interface ProjectCreatedEvent extends Event {

    /**
     * The project created during a Gradle project import.
     *
     * @return the created project
     */
    IProject getProject();

    /**
     * The names of the working sets to which the created project need to be added.
     *
     * @return the names of the working sets to which to add the created project
     */
    ImmutableList<String> getWorkingSets();

}
