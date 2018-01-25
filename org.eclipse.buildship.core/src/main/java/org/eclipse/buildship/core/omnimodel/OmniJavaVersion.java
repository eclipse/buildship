/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel;

/**
 * Describes a Java version.
 *
 * @author Donát Csikós
 */
public interface OmniJavaVersion {

    /**
     * Returns the name of the Java version in a [major_version].[minor_version] format.
     *
     * @return the name of the Java version
     */
    String getName();

}
