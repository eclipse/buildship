/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel;

/**
 * An access rule defined on a classpath entry.
 *
 * @author Donat Csikos
 */
public interface OmniAccessRule {

    /**
     * Returns the access rule type.
     *
     * @return the access rule type
     */
    int getKind();

    /**
     * Returns the file pattern of this access rule.
     *
     * @return the access rule file pattern
     */
    String getPattern();
}
