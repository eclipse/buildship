/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core;

import org.eclipse.core.runtime.IStatus;

/**
 * Contains the result of a Gradle project synchronization.
 *
 * @author Donat Csikos
 * @since 3.0
 * @noimplement this interface is not intended to be implemented by clients
 */
public interface SynchronizationResult {

    /**
     * Returns the status object containing the result status.
     *
     * @return the result status
     */
    IStatus getStatus();
}
