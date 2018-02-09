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
 * Describes the Java Runtime for a Java project.
 *
 * @author Donát Csikós
 */
public interface OmniJavaRuntime {

    /**
     * Returns the Java language level supported by the current runtime.
     *
     * @return the supported Java language level
     */
    OmniJavaVersion getJavaVersion();

    /**
     * Returns the Java Runtime installation directory.
     *
     * @return the installation directory
     */
    File getHomeDirectory();

}
