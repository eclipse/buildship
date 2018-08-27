/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Represents a Gradle build that can be imported into or synchronized with the workspace.
 *
 * @author Donat Csikos
 * @since 3.0
 */
public interface GradleBuild {

    /**
     * Synchronizes this build with the workspace.
     *
     * @param monitor the progress monitor
     * @return the synchronization result
     */
    SynchronizationResult synchronize(IProgressMonitor monitor);
}
