/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel;

/**
 * Describes a nature in an Eclipse project.
 *
 * @author Donát Csikós
 */
public interface OmniEclipseProjectNature {

    /**
     * Returns the unique identifier of the nature.
     *
     * @return the nature id
     */
    String getId();

}
