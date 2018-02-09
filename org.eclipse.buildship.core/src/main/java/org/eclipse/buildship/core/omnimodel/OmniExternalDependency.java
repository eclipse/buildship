/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel;

import java.io.File;

/**
 * Describes an external artifact dependency.
 *
 * @author Etienne Studer
 */
public interface OmniExternalDependency extends OmniClasspathEntry {

    /**
     * Returns the file for this dependency.
     *
     * @return the file for this dependency
     */
    File getFile();

    /**
     * Returns the source directory or archive for this dependency, or {@code null} if no source is available.
     *
     * @return the source directory or archive for this dependency, or {@code null} if no source is available
     */
    File getSource();

    /**
     * Returns the Javadoc directory or archive for this dependency, or {@code null} if no Javadoc is available.
     *
     * @return the Javadoc directory or archive for this dependency, or {@code null} if no Javadoc is available
     */
    File getJavadoc();

    /**
     * Returns whether this dependency should be exported.
     *
     * @return {@code true} if this dependency should be exported
     */
    boolean isExported();

}
