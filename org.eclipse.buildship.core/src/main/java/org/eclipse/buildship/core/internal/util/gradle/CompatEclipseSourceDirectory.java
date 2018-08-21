/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.util.gradle;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.gradle.tooling.model.eclipse.EclipseSourceDirectory;

/**
 * Compatibility decorator for {@link EclipseSourceDirectory}.
 *
 * @author Donat Csikos
 */
public class CompatEclipseSourceDirectory extends CompatEclipseClasspathEntry<EclipseSourceDirectory> implements EclipseSourceDirectory {

    private static List<String> UNSUPPORTED_EXCLUDES = Collections.emptyList();
    private static List<String> UNSUPPORTED_INCLUDES = Collections.emptyList();
    private static String UNSUPPORTED_OUTPUT = "bin";

    CompatEclipseSourceDirectory(EclipseSourceDirectory delegate) {
        super(delegate);
    }

    @Override
    public File getDirectory() {
        return getElement().getDirectory();
    }

    @Override
    public List<String> getExcludes() {
        try {
            return getElement().getExcludes();
        } catch (Exception e) {
            return UNSUPPORTED_EXCLUDES;
        }
    }

    @Override
    public List<String> getIncludes() {
        try {
            return getElement().getIncludes();
        } catch (Exception e) {
            return UNSUPPORTED_INCLUDES;
        }
    }

    @Override
    public String getOutput() {
        try {
            return getElement().getOutput();
        } catch (Exception ignore) {
            return UNSUPPORTED_OUTPUT;
        }
    }

    @Override
    public String getPath() {
        return getElement().getPath();
    }

    public static boolean supportsExcludes(EclipseSourceDirectory sourceDirectory) {
        return sourceDirectory.getExcludes() != UNSUPPORTED_EXCLUDES;
    }

    public static boolean supportsIncludes(EclipseSourceDirectory sourceDirectory) {
        return sourceDirectory.getIncludes() != UNSUPPORTED_INCLUDES;
    }

    public static boolean supportsOutput(EclipseSourceDirectory sourceDirectory) {
        return sourceDirectory.getOutput() != UNSUPPORTED_OUTPUT;
    }
}
