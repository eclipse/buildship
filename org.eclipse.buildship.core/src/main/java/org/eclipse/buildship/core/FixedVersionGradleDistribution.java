/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core;

/**
 * A a reference to a specific version of Gradle. The appropriate distribution is downloaded and
 * installed into the user's Gradle home directory.
 *
 * @author Donat Csikos
 * @since 3.0
 * @noimplement this interface is not intended to be implemented by clients
 */
public interface FixedVersionGradleDistribution extends GradleDistribution {

    /**
     * The Gradle version to use.
     *
     * @return the Gradle version
     */
    String getVersion();
}
