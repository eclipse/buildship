/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.workspace.internal;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.io.Files;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.buildship.core.CorePlugin;

/**
 * Base updater class to load and store updated item names.
 *
 * @author Donat Csikos
 */
public abstract class PersistentUpdater {

    protected final IProject project;
    private final String propertyName;

    public PersistentUpdater(IProject project, String propertyName) {
        this.propertyName = Preconditions.checkNotNull(propertyName);
        this.project = Preconditions.checkNotNull(project);
    }

    protected Collection<String> getKnownItems() throws CoreException {
        String serializedForm = read();
        if (serializedForm == null) {
            return Collections.emptyList();
        }
        return Splitter.on(File.pathSeparator).omitEmptyStrings().splitToList(serializedForm);
    }

    protected void setKnownItems(Collection<String> items) throws CoreException {
        write(Joiner.on(File.pathSeparator).join(items));
    }

    private String read() {
        File stateLocation = storageFile();
        if (!stateLocation.exists()) {
            return "";
        }
        try {
            return Files.toString(stateLocation, Charsets.UTF_8);
        } catch (Exception e) {
            CorePlugin.logger().error(String.format("Can't load property %s for project %s.", this.propertyName, this.project.getName()), e);
            return "";
        }
    }

    private void write(String content) {
        File stateLocation = storageFile();
        try {
            Files.createParentDirs(stateLocation);
            Files.write(content, stateLocation, Charsets.UTF_8);
        } catch (IOException e) {
            CorePlugin.logger().error(String.format("Can't store property %s for project %s.", this.propertyName, this.project.getName()), e);
        }
    }

    private File storageFile() {
        return CorePlugin.getInstance().getStateLocation().append(this.propertyName).append(this.project.getName()).toFile();
    }
}
