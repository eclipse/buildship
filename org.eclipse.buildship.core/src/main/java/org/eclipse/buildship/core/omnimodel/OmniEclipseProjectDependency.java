/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel;

/**
 * Describes a dependency on another Eclipse project.
 *
 * @author Etienne Studer
 */
public interface OmniEclipseProjectDependency extends OmniClasspathEntry {

    /**
     * Returns the path to use for this project dependency.
     *
     * @return the path to use for this project dependency
     */
    String getPath();

    /**
     * Returns whether this project dependency should be exported.
     *
     * @return {@code true} if this project dependency should be exported
     */
    boolean isExported();

}
