/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.preferences.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * Event handler interface to react when a project is deleted or renamed in the workspace.
 *
 * @author Donat Csikos
 * @see ProjectChangeListener
 */
public interface ProjectChangeHandler {

    /**
     * Called when a project in the workspace is moved to a new location.
     *
     * @param from the previous location path
     * @param to the new project
     */
    void projectMoved(IPath from, IProject to) throws Exception;

    /**
     * Called when a project is deleted from the workspace.
     *
     * @param project the deleted project
     */
    void projectDeleted(IProject project) throws Exception;
}