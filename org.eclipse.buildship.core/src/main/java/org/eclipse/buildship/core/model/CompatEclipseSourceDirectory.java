/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.model;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.gradle.tooling.model.eclipse.EclipseSourceDirectory;

import com.google.common.base.Optional;

import org.eclipse.buildship.core.util.gradle.Maybe;

/**
 * Decorated {@link EclipseSourceDirectory} providing some backward compatibility.
 *
 * @author Donat Csikos
 */
public class CompatEclipseSourceDirectory extends CompatEclipseClasspathEntry<EclipseSourceDirectory> implements EclipseSourceDirectory {

    public CompatEclipseSourceDirectory(EclipseSourceDirectory delegate) {
        super(delegate);
    }

    @Override
    public File getDirectory() {
        return this.delegate.getDirectory();
    }

    @Override
    public List<String> getExcludes() {
        try {
            return this.delegate.getExcludes();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public Optional<List<String>> getExcludesOrAbsent() {
        try {
            return Optional.of(this.delegate.getExcludes());
        } catch (Exception e) {
            return Optional.absent();
        }
    }

    @Override
    public List<String> getIncludes() {
        try {
            return this.delegate.getIncludes();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public Optional<List<String>> getIncludesOrAbsent() {
        try {
            return Optional.of(this.delegate.getIncludes());
        } catch (Exception e) {
            return Optional.absent();
        }
    }

    @Override
    public String getOutput() {
        try {
            return this.delegate.getOutput();
        } catch (Exception e) {
            return null;
        }
    }

    public Maybe<String> getOutputMaybe() {
        try {
            return Maybe.of(this.delegate.getOutput());
        } catch (Exception e) {
            return Maybe.absent();
        }
    }

    @Override
    public String getPath() {
        return this.delegate.getPath();
    }
}
