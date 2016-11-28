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
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import org.eclipse.buildship.core.CorePlugin;

/**
 * Base updater class to load and store updated item names.
 *
 * @author Donat Csikos
 */
public abstract class PersistentUpdater<T> {

    protected final IProject project;
    private final QualifiedName propertyName;

    public PersistentUpdater(IProject project, String propertyName) {
        this.propertyName = new QualifiedName(CorePlugin.PLUGIN_ID, propertyName);
        this.project = Preconditions.checkNotNull(project);
    }

    protected Collection<String> getKnownItems() throws CoreException {
        String serializedForm = this.project.getPersistentProperty(this.propertyName);
        if (serializedForm == null) {
            return Collections.emptyList();
        }
        return Splitter.on(File.pathSeparator).omitEmptyStrings().splitToList(serializedForm);
    }

    protected void setKnownItems(Collection<String> items) throws CoreException {
        this.project.setPersistentProperty(this.propertyName, Joiner.on(File.pathSeparator).join(items));
    }
}
