/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel;

/**
 * The output location of an Eclipse project.
 *
 * @author Donat Csikos
 */
public interface OmniEclipseOutputLocation {

    /**
     * Returns the project-relative path to the output location.
     *
     * @return The path to the output location. Does not return null.
     */
    String getPath();
}
