/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace;

import org.eclipse.core.resources.IProject;

/**
 * Project deleted event.
 *
 * @author Donat Csikos
 */
public final class ProjectDeletedEvent extends BaseProjectChangedEvent {

    public ProjectDeletedEvent(IProject project) {
        super(project);
    }


}
