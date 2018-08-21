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

package org.eclipse.buildship.core.internal.util.file;

import java.io.File;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;

/**
 * Contains helper methods related to file operations.
 */
public final class FileUtils {

    private FileUtils() {
    }

    /**
     * Derives a {@code File} instance with absolute path from the specified {@code File} instance.
     *
     * @param file the relative or absolute file of the {@code File} instance to derive
     * @return the absolute {@code File} if the file is not {@code null}, otherwise
     * {@link Optional#absent()}
     */
    public static Optional<File> getAbsoluteFile(File file) {
        if (file == null) {
            return Optional.absent();
        } else {
            return Optional.of(file.isAbsolute() ? file : file.getAbsoluteFile());
        }
    }

    /**
     * Derives a {@code File} instance with absolute path from the specified path.
     *
     * @param path the relative or absolute path of the {@code File} instance to derive
     * @return the absolute {@code File} if the path is not {@code null} or empty, otherwise
     * {@link Optional#absent()}
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
     * {@link Optional#absent()}
     */
    public static Optional<String> getAbsolutePath(File file) {
        if (file == null) {
            return Optional.absent();
        } else {
            return Optional.of(file.getAbsolutePath());
        }
    }

    /**
     * Ensures the given folder and its parent hierarchy are created if they do not already exist.
     *
     * @param folder the folder whose hierarchy to ensure to exist
     */
    public static void ensureFolderHierarchyExists(IFolder folder) {
        if (!folder.exists()) {
            if (folder.getParent() instanceof IFolder) {
                ensureFolderHierarchyExists((IFolder) folder.getParent());
            }

            try {
                folder.create(true, true, null);
            } catch (CoreException e) {
                String message = String.format("Cannot create folder %s.", folder);
                throw new GradlePluginsRuntimeException(message, e);
            }
        }
    }

    /**
     * Ensures the given folder's parent hierarchy is created if they do not already exist.
     *
     * @param resource the folder whose parent's hierarchy to ensure to exist
     */
    public static void ensureParentFolderHierarchyExists(IResource resource) {
        IContainer parent = resource.getParent();
        if (parent instanceof IFolder) {
            ensureFolderHierarchyExists((IFolder) parent);
        }
    }

    /**
     * Deletes the given file or directory. In case of a directory, all its content is deleted recursively.
     *
     * @param file the file or directory to be deleted
     * @return <code>true</code> iff deletion fully succeeded
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean deleteRecursively(File file) {
        if (file.isDirectory()) {
            boolean success = true;
            File[] children = file.listFiles();
            for (File child : children) {
                success &= deleteRecursively(child);
            }
            return success && file.delete();
        } else {
            return file.delete();
        }
    }

}
