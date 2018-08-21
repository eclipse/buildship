/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.internal.util.file;

import com.google.common.base.Preconditions;

import org.eclipse.core.runtime.IPath;

/**
 * Contains helper methods to convert absolute paths to relative paths and vica versa.
 */
public final class RelativePathUtils {

    private RelativePathUtils() {
    }

    /**
     * Calculates the relative path from a base to a target path.
     *
     * @param base the base path to make the target path relative to
     * @param target the target file to which the relative path is calculated
     * @return the relative path from the base to the target location
     * @throws NullPointerException if an argument is {@code null}
     * @throws IllegalArgumentException if an argument does not denote an absolute path
     */
    public static IPath getRelativePath(IPath base, IPath target) {
        Preconditions.checkNotNull(base);
        Preconditions.checkNotNull(target);
        Preconditions.checkArgument(base.isAbsolute());
        Preconditions.checkArgument(target.isAbsolute());

        return target.makeRelativeTo(base);
    }

    /**
     * Calculates the absolute path from a base to a relative target location.
     *
     * @param base the base path
     * @param relativePath the relative path from the base to the target
     * @return the absolute path to the target location
     * @throws NullPointerException if an argument is {@code null}
     * @throws IllegalArgumentException if a) the base path does not denote an absolute path b) the
     *             target path does not denote a relative path, or c) the relative path is invalid
     *             (i.e. points above the root folder)
     */
    public static IPath getAbsolutePath(IPath base, IPath relativePath) {
        Preconditions.checkNotNull(base);
        Preconditions.checkNotNull(relativePath);
        Preconditions.checkArgument(base.isAbsolute());
        Preconditions.checkArgument(!relativePath.isAbsolute());

        IPath result = base;
        for (String segment : relativePath.segments()) {
            IPath newResult = result.append(segment);

            // Appending a '..' segment to the root path does not fail but returns a new path object
            // see org.eclipse.core.runtime.Path.removeLastSegment(int)
            if (segment.equals("..") && newResult.segmentCount() >= result.segmentCount()) {
                throw new IllegalArgumentException(String.format("Relative path can't point beyond the root (base=%s, relativePath=%s).", base, relativePath));
            }

            result = newResult;
        }
        return result;
    }

}
