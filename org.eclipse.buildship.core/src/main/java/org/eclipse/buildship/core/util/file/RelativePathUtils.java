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
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

/**
 * Contains helper methods to convert absolute file paths to relative paths and vica versa.
 */
public final class RelativePathUtils {

    private RelativePathUtils() {
    }

    /**
     * Calculates the relative file path from a base directory to a target file.
     * <p/>
     * If the supplied base object is a file, then the relative path will be calculated against the file's container
     * directory.
     *
     * @param base the base file to make the target file relative to
     * @param target the target file to which the relative path is calculated
     * @return the relative path from the base to the target
     * @throws IllegalArgumentException if any of the input is {@code null}.
     */
    public static String getRelativePath(File base, File target) {
        Preconditions.checkArgument(base != null);
        Preconditions.checkArgument(target != null);
        String basePath = base.isFile() ? base.getParentFile().getAbsolutePath() : base.getAbsolutePath();
        String targetPath = target.getAbsolutePath();
       return getRelativePath(basePath, targetPath, File.separator);
    }

    private static String getRelativePath(String base, String target, String separator) {
        Preconditions.checkArgument(base != null);
        Preconditions.checkArgument(target != null);
        Preconditions.checkArgument(separator != null);

        List<String> baseSegments = Splitter.on(separator).trimResults().omitEmptyStrings().splitToList(base);
        List<String> targetSegments = Splitter.on(separator).trimResults().omitEmptyStrings().splitToList(target);

        if (baseSegments.equals(targetSegments)) {
            return ".";
        }

        List<String> sharedSegments = new ArrayList<String>(10);
        for (int i = 0; i < baseSegments.size() && i < targetSegments.size() && baseSegments.get(i).equals(targetSegments.get(i)); i++) {
            sharedSegments.add(baseSegments.get(i));
        }

        StringBuilder relativePath = new StringBuilder();
        String sep = "";
        for (int i = sharedSegments.size(); i < baseSegments.size(); i++) {
            relativePath.append(sep);
            relativePath.append("..");
            sep = separator;
        }

        for (int i = sharedSegments.size(); i < targetSegments.size(); i++) {
            relativePath.append(sep);
            relativePath.append(targetSegments.get(i));
            sep = separator;
        }

        return relativePath.toString();
    }

    /**
     * Calculates an absolute path from a base file to a relative location and returns a file with that path.
     * <p/>
     * If the supplied base object is a file, then the relative path will be calculated against the
     * file's container directory.
     *
     * @param base the base file
     * @param relativePath the location of the result file relative to the base
     * @return the file instance with the absolute path
     * @throws IllegalArgumentException if any of the input is {@code null} or if the relative path
     *             is invalid (points above the root folder).
     */
    public static File getAbsoluteFile(File base, String relativePath) {
        Preconditions.checkArgument(base != null);
        Preconditions.checkArgument(relativePath != null);

        String basePath = base.isFile() ? base.getParentFile().getAbsolutePath() : base.getAbsolutePath();
        return new File(getAbsolutePath(basePath, relativePath, File.separator));
    }

    private static String getAbsolutePath(String base, String relativePath, String separator) {
        base = base.trim();
        relativePath = relativePath.trim();
        if (base.endsWith(separator) && !base.equals(separator)) {
            base = base.substring(0, base.length() - 1);
        }
        if (relativePath.endsWith(separator)) {
            relativePath = relativePath.substring(0, relativePath.length() - 1);
        }

        if (relativePath.equals(".") || relativePath.isEmpty()) {
            return base;
        } else {
            List<String> relativeSegments = new ArrayList<String>(Splitter.on(separator).trimResults().omitEmptyStrings().splitToList(relativePath));

            for (int i = 0; i < relativeSegments.size(); i++) {
                String segment = relativeSegments.get(i);
                if (segment.equals("..")) {
                    // step one level up
                    int sepIndex = base.lastIndexOf(separator);
                    if (sepIndex < 0) {
                        throw new IllegalArgumentException(String.format("Relative path can't go beyond the root of "
                                + "the base path (base=%s, relativePath=%s, separator=%s).", base, relativePath, separator));
                    } else {
                        base = base.substring(0, sepIndex).trim();
                    }
                } else {
                    // step one level down
                    base += separator + segment;
                }
            }
            return base.isEmpty() ? separator : base;
        }

    }

}
