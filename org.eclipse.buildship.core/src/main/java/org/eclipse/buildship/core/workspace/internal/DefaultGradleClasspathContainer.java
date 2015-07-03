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

package org.eclipse.buildship.core.workspace.internal;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;

import org.eclipse.buildship.core.workspace.GradleClasspathContainer;

/**
 * Default {@link GradleClasspathContainer} implementation.
 */
public final class DefaultGradleClasspathContainer extends GradleClasspathContainer {

    private final IPath containerPath;
    private final IClasspathEntry[] classpathEntries;

    public DefaultGradleClasspathContainer(IPath containerPath, List<IClasspathEntry> classpathEntries) {
        this.containerPath = Preconditions.checkNotNull(containerPath);
        this.classpathEntries = Iterables.toArray(classpathEntries, IClasspathEntry.class);
    }

    @Override
    public String getDescription() {
        return "Project and External Dependencies"; //$NON-NLS-1$
    }

    @Override
    public IPath getPath() {
        return this.containerPath;
    }

    @Override
    public IClasspathEntry[] getClasspathEntries() {
        return this.classpathEntries;
    }

    @Override
    public int getKind() {
        return IClasspathContainer.K_APPLICATION;
    }

}
