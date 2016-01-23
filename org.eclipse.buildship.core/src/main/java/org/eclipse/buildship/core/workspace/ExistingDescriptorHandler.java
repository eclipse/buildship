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

/**
 * This handler decides whether existing .project files should be deleted when importing a
 * Gradle project.
 */
public interface ExistingDescriptorHandler {

    ExistingDescriptorHandler ALWAYS_KEEP = new ExistingDescriptorHandler() {

        @Override
        public boolean shouldDeleteDescriptor() {
            return false;
        }
    };

    ExistingDescriptorHandler ALWAYS_DELETE = new ExistingDescriptorHandler() {

        @Override
        public boolean shouldDeleteDescriptor() {
            return true;
        }
    };

    /**
     * @return {@code true} if an existig .project file should be deleted, {@code false} otherwise.
     */
    boolean shouldDeleteDescriptor();

}
