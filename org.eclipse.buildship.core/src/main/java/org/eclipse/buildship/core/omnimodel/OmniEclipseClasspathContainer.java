/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel;

/**
 * A classpath container associated to an Eclipse project.
 *
 * @author Donat Csikos
 */
public interface OmniEclipseClasspathContainer extends OmniClasspathEntry {

    /**
     * Returns the path of the classpath container.
     *
     * @return the classpath container path, never null
     */
    String getPath();

    /**
     * Returns if the container is exported to dependent project.
     *
     * @return true if the container is exported
     */
    boolean isExported();
}
