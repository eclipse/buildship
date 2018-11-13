/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.workspace;

import org.eclipse.core.resources.IProject;

/**
 * Project closed event.
 *
 * @author Prashanth Rao
 */
public final class ProjectClosedEvent extends BaseProjectChangedEvent {

    public ProjectClosedEvent(IProject project) {
       super(project);
    }
}
