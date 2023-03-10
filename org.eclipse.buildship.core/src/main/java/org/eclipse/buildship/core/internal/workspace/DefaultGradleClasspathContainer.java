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

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;

import org.eclipse.buildship.core.internal.i18n.CoreMessages;

/**
 * Default {@link GradleClasspathContainer} implementation.
 */
final class DefaultGradleClasspathContainer extends GradleClasspathContainer {

    private final IPath containerPath;
    private final IClasspathEntry[] classpathEntries;

    public DefaultGradleClasspathContainer(IPath containerPath, List<IClasspathEntry> classpathEntries) {
        this.containerPath = Preconditions.checkNotNull(containerPath);
        this.classpathEntries = Iterables.toArray(classpathEntries, IClasspathEntry.class);
    }

    @Override
    public String getDescription() {
        return CoreMessages.ClasspathContainer_Label;
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

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Arrays.hashCode(this.classpathEntries);
        result = 31 * result + this.containerPath.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        DefaultGradleClasspathContainer other = (DefaultGradleClasspathContainer) obj;
        return Arrays.equals(this.classpathEntries, other.classpathEntries) && this.containerPath.equals(other.containerPath);
    }

}
