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
import org.eclipse.buildship.core.preferences.PersistentModel;

/**
 * Helper class for the updater class to load and store persistent models.
 *
 * @author Donat Csikos
 */
public final class PersistentUpdaterUtils {

    private PersistentUpdaterUtils() {
    }

    public static Collection<String> getKnownItems(IProject project, String propertyName) throws CoreException {
        PersistentModel preferences = CorePlugin.modelPersistence().loadModel(project);
        String serializedForm = preferences.getValue(propertyName, null);
        if (serializedForm == null) {
            return Collections.emptyList();
        }
        return Splitter.on(File.pathSeparator).omitEmptyStrings().splitToList(serializedForm);
    }

    public static void setKnownItems(IProject project, String propertyName, Collection<String> items) throws CoreException {
        PersistentModel preferences = CorePlugin.modelPersistence().loadModel(project);
        preferences.setValue(propertyName, Joiner.on(File.pathSeparator).join(items));
        preferences.flush();
    }
}
