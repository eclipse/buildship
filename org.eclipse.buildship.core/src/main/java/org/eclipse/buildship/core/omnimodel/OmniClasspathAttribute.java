/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel;

/**
 * A classpath attribute on a classpath entry.
 * @author Stefan Oehme
 */
public interface OmniClasspathAttribute {

    /**
     * Returns the name of this classpath attribute.
     * @return the name, never null
     */
    String getName();

    /**
     * Returns the value of this classpath attribute.
     * @return the value, never null
     */
    String getValue();
}
