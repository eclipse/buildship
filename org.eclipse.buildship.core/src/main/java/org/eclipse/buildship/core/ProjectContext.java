/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core;

import org.eclipse.core.resources.IProject;

/**
 * Describes a workspace project being synchronized.
 *
 * @author Donat Csikos
 * @since 3.0
 * @see ProjectConfigurator
 */
public interface ProjectContext extends SynchronizationContext {

    /**
     * @return the current project being synchronized
     */
    IProject getProject();
}
