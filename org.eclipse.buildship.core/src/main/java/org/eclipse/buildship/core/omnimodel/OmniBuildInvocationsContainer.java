/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel;

import java.util.SortedMap;

import com.google.common.base.Optional;

import org.eclipse.buildship.core.util.gradle.Path;

/**
 * Holds the {@link OmniBuildInvocations} for a given set of projects. Each project is identified by its unique full path.
 * <p/>
 * The primary advantage of this container is that it allows to work with a generics-free type compared to <code>Map&lt;String, OmniBuildInvocations&gt;</code>.
 *
 * @author Etienne Studer
 */
public interface OmniBuildInvocationsContainer {

    /**
     * Returns the {@code OmniBuildInvocations} for the given project, where the project is identified by its unique full path.
     *
     * @param projectPath the full path of the project for which to get the build invocations
     * @return the build invocations, if present
     */
    Optional<OmniBuildInvocations> get(Path projectPath);

    /**
     * A {@code Map} of {@code OmniBuildInvocations} per project, where each project is identified by its unique full path.
     *
     * @return the mapping of projects to build invocations
     */
    SortedMap<Path, OmniBuildInvocations> asMap();

}
