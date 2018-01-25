/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel;

import java.io.File;
import java.util.List;

import com.google.common.base.Optional;

import org.eclipse.buildship.core.util.gradle.Maybe;

/**
 * Describes a source directory in an Eclipse project.
 *
 * @author Etienne Studer
 */
public interface OmniEclipseSourceDirectory extends OmniClasspathEntry {

    /**
     * Returns the source directory.
     *
     * @return the source directory
     */
    File getDirectory();

    /**
     * Returns the relative path of this source directory.
     *
     * @return the relative path of this source directory
     */
    String getPath();


    /**
     * Returns the exclude patterns of this source directory.
     *
     * @return the exclude patterns
     */
    Optional<List<String>> getExcludes();

    /**
     * Returns the include patterns of this directory.
     *
     * @return the include patterns
     */
    Optional<List<String>> getIncludes();

    /**
     * Returns the output path of this source directory.
     *
     * @return the output path
     */
    Maybe<String> getOutput();
}
