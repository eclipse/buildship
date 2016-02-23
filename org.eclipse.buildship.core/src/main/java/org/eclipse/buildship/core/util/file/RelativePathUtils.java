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

package org.eclipse.buildship.core.util.file;

import java.io.File;

import com.google.common.base.Preconditions;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Contains helper methods to convert absolute file paths to relative paths and vica versa.
 */
public final class RelativePathUtils {

    private RelativePathUtils() {
    }

    /**
     * Calculates the relative file path from a base to a target file.
     *
     * @param base the base directory to make the target file relative to
     * @param target the target file to which the relative path is calculated
     * @return the relative path from the base to the target
     */
    public static String getRelativePath(File base, File target) {
        Preconditions.checkNotNull(base != null);
        Preconditions.checkNotNull(target != null);

        String basePath = base.getAbsolutePath();
        String targetPath = target.getAbsolutePath();
        return getRelativePath(new Path(basePath), new Path(targetPath));
    }

    private static String getRelativePath(IPath basePath, IPath targetPath) {
        if (basePath.equals(targetPath)) {
            return ".";
        }

        int numOfSharedSegments = basePath.matchingFirstSegments(targetPath);

        StringBuilder relativePath = new StringBuilder();
        String sep = "";
        for (int i = numOfSharedSegments; i < basePath.segmentCount(); i++) {
            relativePath.append(sep);
            relativePath.append("..");
            sep = File.separator;
        }

        for (int i = numOfSharedSegments; i < targetPath.segmentCount(); i++) {
            relativePath.append(sep);
            relativePath.append(targetPath.segment(i));
            sep = File.separator;
        }

        return relativePath.toString();
    }

    /**
     * Calculates an absolute path from a base file to a relative location and returns a file with
     * that path.
     *
     * @param base the base directory
     * @param relativePath the location of the result file relative to the base
     * @return the file instance with the absolute path
     */
    public static File getAbsoluteFile(File base, String relativePath) {
        Preconditions.checkNotNull(base != null);
        Preconditions.checkNotNull(relativePath != null);

        IPath basePath = new Path(base.getAbsolutePath());
        return new File(basePath.append(relativePath).toFile().getAbsolutePath());
    }
}
