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

package com.gradleware.tooling.eclipse.core.util.file;

import java.io.File;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

/**
 * Contains helper methods related to file operations.
 */
public final class FileUtils {

    private FileUtils() {
    }

    /**
     * Derives a {@code File} instance with absolute path from the specified path.
     *
     * @param path the relative or absolute path of the {@code File} instance to derive
     * @return the absolute {@code File} if the path is not {@code null} or empty, otherwise
     *         {@link Optional#absent()}
     */
    public static Optional<File> getAbsoluteFile(String path) {
        if (Strings.isNullOrEmpty(path)) {
            return Optional.absent();
        } else {
            return Optional.of(new File(path.trim()).getAbsoluteFile());
        }
    }

    /**
     * Derives the absolute path from the specified {@code File} instance.
     *
     * @param file the file from which to get the absolute path
     * @return the absolute path if the file is not {@code null}, otherwise
     *         {@link Optional#absent()}
     */
    public static Optional<String> getAbsolutePath(File file) {
        if (file == null) {
            return Optional.absent();
        } else {
            return Optional.of(file.getAbsolutePath());
        }
    }

}
