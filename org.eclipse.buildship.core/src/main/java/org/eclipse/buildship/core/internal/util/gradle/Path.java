/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.util.gradle;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Represents a path in Gradle. The path can point to a project, task, etc.
 *
 * @author Etienne Studer
 */
public final class Path implements Comparable<Path> {

    private static final String PATH_SEPARATOR = ":";
    private static final Path ROOT_PATH = new Path(PATH_SEPARATOR);

    private final String path;

    private Path(String path) {
        this.path = Preconditions.checkNotNull(path);
        Preconditions.checkArgument(path.startsWith(PATH_SEPARATOR));
    }

    public String getPath() {
        return this.path;
    }

    /**
     * Returns a copy of this path with the last segment removed. If this path points to the root,
     * then the root path is returned.
     *
     * @return the new path
     */
    public Path dropLastSegment() {
        int lastPathChar = this.path.lastIndexOf(PATH_SEPARATOR);
        return lastPathChar <= 0 ? ROOT_PATH : new Path(this.path.substring(0, lastPathChar));
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(Path other) {
        Preconditions.checkNotNull(other);
        return PathComparator.INSTANCE.compare(this.path, other.path);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Path that = (Path) other;
        return Objects.equal(this.path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.path);
    }

    public static Path from(String path) {
        return new Path(path);
    }

    /**
     * Singleton comparator to compare {@code Path} instances.
     */
    public enum Comparator implements java.util.Comparator<Path> {

        INSTANCE;

        @Override
        public int compare(Path o1, Path o2) {
            return o1.compareTo(o2);
        }

    }

    /**
     * Compares two paths first by their depth and in case of equal depth lexicographically by their segments, starting with the left-most segment. See also {@code
     * org.gradle.tooling.internal.consumer.converters.TaskNameComparator}.
     *
     * @author Etienne Studer
     */
    private static enum PathComparator implements java.util.Comparator<String> {

        INSTANCE;

        @Override
        public int compare(String path1, String path2) {
            int depthDiff = getDepth(path1) - getDepth(path2);
            if (depthDiff != 0) {
                return depthDiff;
            }
            return compareSegments(path1, path2);
        }

        private int compareSegments(String path1, String path2) {
            int colon1 = path1.indexOf(':');
            int colon2 = path2.indexOf(':');
            if (colon1 > 0 && colon2 > 0) {
                int diff = path1.substring(0, colon1).compareTo(path2.substring(0, colon2));
                if (diff != 0) {
                    return diff;
                }
            }
            return colon1 == -1 ? path1.compareTo(path2) : compareSegments(path1.substring(colon1 + 1), path2.substring(colon2 + 1));
        }

        private int getDepth(String taskName) {
            int counter = 0;
            for (char c : taskName.toCharArray()) {
                if (c == ':') {
                    counter++;
                }
            }
            return counter;
        }

    }


}
