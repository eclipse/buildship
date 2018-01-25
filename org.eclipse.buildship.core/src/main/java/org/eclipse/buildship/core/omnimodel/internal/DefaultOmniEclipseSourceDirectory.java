/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel.internal;

import java.io.File;
import java.util.List;

import org.gradle.tooling.model.eclipse.EclipseSourceDirectory;

import com.google.common.base.Optional;

import org.eclipse.buildship.core.omnimodel.OmniAccessRule;
import org.eclipse.buildship.core.omnimodel.OmniClasspathAttribute;
import org.eclipse.buildship.core.omnimodel.OmniEclipseSourceDirectory;
import org.eclipse.buildship.core.util.gradle.Maybe;

/**
 * Default implementation of the {@link OmniEclipseSourceDirectory} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniEclipseSourceDirectory extends AbstractOmniClasspathEntry implements OmniEclipseSourceDirectory {

    private final File directory;
    private final String path;
    private final Optional<List<String>> excludes;
    private final Optional<List<String>> includes;
    private final Maybe<String> output;

    private DefaultOmniEclipseSourceDirectory(File directory, String path,
                                              Optional<List<String>> excludes, Optional<List<String>> includes,
                                              Maybe<String> output, Optional<List<OmniClasspathAttribute>> attributes,
                                              Optional<List<OmniAccessRule>> accessRules) {
        super(attributes, accessRules);
        this.directory = directory;
        this.path = path;
        this.excludes = excludes;
        this.includes = includes;
        this.output = output;
    }

    @Override
    public File getDirectory() {
        return this.directory;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public Optional<List<String>> getExcludes() {
        return this.excludes;
    }

    @Override
    public Optional<List<String>> getIncludes() {
        return this.includes;
    }

    @Override
    public Maybe<String> getOutput() {
        return this.output;
    }

    public static DefaultOmniEclipseSourceDirectory from(EclipseSourceDirectory sourceDirectory) {
        return new DefaultOmniEclipseSourceDirectory(
                sourceDirectory.getDirectory(),
                sourceDirectory.getPath(),
                getExcludes(sourceDirectory),
                getIncludes(sourceDirectory),
                getOutput(sourceDirectory),
                getClasspathAttributes(sourceDirectory),
                getAccessRules(sourceDirectory));
    }

    private static Optional<List<String>> getExcludes(EclipseSourceDirectory sourceDirectory) {
        try {
            return Optional.of(sourceDirectory.getExcludes());
        } catch(Exception ignore) {
            return Optional.absent();
        }
    }

    private static Optional<List<String>> getIncludes(EclipseSourceDirectory sourceDirectory) {
        try {
            return Optional.of(sourceDirectory.getIncludes());
        } catch(Exception ignore) {
            return Optional.absent();
        }
    }

    private static Maybe<String> getOutput(EclipseSourceDirectory sourceDirectory) {
        try {
            return Maybe.of(sourceDirectory.getOutput());
        } catch (Exception ignore) {
            return Maybe.absent();
        }
    }

}
