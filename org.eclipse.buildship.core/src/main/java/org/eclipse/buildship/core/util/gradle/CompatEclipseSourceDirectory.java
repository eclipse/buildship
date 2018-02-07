/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.util.gradle;

import java.io.File;
import java.util.List;

import org.gradle.tooling.model.eclipse.EclipseSourceDirectory;

import org.eclipse.buildship.core.GradlePluginsRuntimeException;

/**
 * Compatibility decorator for {@link EclipseSourceDirectory}.
 *
 * @author Donat Csikos
 */
class CompatEclipseSourceDirectory extends CompatEclipseClasspathEntry<EclipseSourceDirectory> implements EclipseSourceDirectory {

    public CompatEclipseSourceDirectory(EclipseSourceDirectory delegate) {
        super(delegate);
    }

    @Override
    public File getDirectory() {
        return getElement().getDirectory();
    }

    @Override
    public List<String> getExcludes() {
        throw new GradlePluginsRuntimeException(ModelUtils.unsupportedMessage("getExcludes"));
    }

    @Override
    public List<String> getIncludes() {
        throw new GradlePluginsRuntimeException(ModelUtils.unsupportedMessage("getIncludes"));
    }

    @Override
    public String getOutput() {
        throw new GradlePluginsRuntimeException(ModelUtils.unsupportedMessage("getOutput"));
    }

    @Override
    public String getPath() {
        return getElement().getPath();
    }
}
