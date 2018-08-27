/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core;

import org.eclipse.core.runtime.IStatus;

/**
 * Contains the result of a Gradle project synchronization.
 *
 * @author Donat Csikos
 * @since 3.0
 *
 */
public interface SynchronizationResult {

    /**
     * Returns the status object containing the result status.
     *
     * @return the result status
     */
    IStatus getStatus();
}
