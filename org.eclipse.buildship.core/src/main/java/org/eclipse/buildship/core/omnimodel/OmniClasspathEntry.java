/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel;

import java.util.List;

import com.google.common.base.Optional;

/**
 * A classpath entry in an Eclipse Java project.
 *
 * @author Stefan Oehme
 */
public interface OmniClasspathEntry {

    /**
     * Returns the classpath attributes of this entry.
     *
     * @return the attributes, never null
     */
    Optional<List<OmniClasspathAttribute>> getClasspathAttributes();

    /**
     * Returns the access rules of this entry.
     *
     * @return the access rules, never null
     */
    Optional<List<OmniAccessRule>> getAccessRules();
}
