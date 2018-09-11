/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core;

import java.net.URI;

/**
 * A reference to a remote Gradle distribution.
 *
 * @author Donat Csikos
 * @since 3.0
 * @noimplement this interface is not intended to be implemented by clients
 */
public interface RemoteGradleDistribution extends GradleDistribution {

    /**
     * The URL pointing to the the remote Gradle distribution.
     *
     * @return the remote distribution location
     */
    URI getUrl();
}
