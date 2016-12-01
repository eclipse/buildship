/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.workspace.internal;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.preferences.ProjectPluginStatePreferences;

/**
 * Base updater class to load and store updated item names.
 *
 * @author Donat Csikos
 */
public abstract class PersistentUpdater {

    private final ProjectPluginStatePreferences preferences;
    protected final String name;
    protected final IProject project;

    public PersistentUpdater(IProject project, String name) {
        this.project = project;
        this.name = name;
        this.preferences = CorePlugin.projectPluginStatePreferenceStore().loadProjectPrefs(project);
    }

    protected Collection<String> getKnownItems() throws CoreException {
        String serializedForm = this.preferences.getValue(this.name, null);
        if (serializedForm == null) {
            return Collections.emptyList();
        }
        return Splitter.on(File.pathSeparator).omitEmptyStrings().splitToList(serializedForm);
    }

    protected void setKnownItems(Collection<String> items) throws CoreException {
        this.preferences.setValue(this.name, Joiner.on(File.pathSeparator).join(items));
        this.preferences.flush();
    }
}
