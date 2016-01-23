/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.workspace;

import org.eclipse.core.resources.IProjectDescription;

/**
 * This handler decides whether existing .project files should be overwritten when importing a Gradle project.
 */
public interface ExistingDescriptorHandler {

    ExistingDescriptorHandler ALWAYS_KEEP = new ExistingDescriptorHandler() {

        @Override
        public boolean shouldOverwriteDescriptor(IProjectDescription project) {
            return false;
        }
    };

    ExistingDescriptorHandler ALWAYS_OVERWRITE = new ExistingDescriptorHandler() {

        @Override
        public boolean shouldOverwriteDescriptor(IProjectDescription project) {
            return true;
        }
    };

    /**
     * Decides whether to keep or overwrite the descriptor of the given project.
     *
     * @param project the target project
     * @return {@code true} if an existing .project file should be overwritten, {@code false} otherwise
     */
    boolean shouldOverwriteDescriptor(IProjectDescription project);

}
