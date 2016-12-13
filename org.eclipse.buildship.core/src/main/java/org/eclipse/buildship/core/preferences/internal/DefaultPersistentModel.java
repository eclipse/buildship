/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.preferences.internal;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.buildship.core.preferences.PersistentModel;

/**
 * Default implementation for {@link PersistentModel}.
 *
 * @author Donat Csikos
 */
class DefaultPersistentModel implements PersistentModel {

    private static final String PROPERTY_SUBPROJECTS = "subprojectPaths";
    private static final String PROPERTY_BUILD_DIR = "buildDir";

    private final IProject project;
    private final Map<String, String> entries;

    DefaultPersistentModel(IProject project, Map<String, String> entries) {
        this.project = Preconditions.checkNotNull(project);
        this.entries = Maps.newHashMap(entries);
    }

    Map<String, String> getEntries() {
        return this.entries;
    }

    IProject getProject() {
        return this.project;
    }

    @Override
    public String getBuildDir() {
        return getValue(PROPERTY_BUILD_DIR, null);
    }

    @Override
    public void setBuildDir(String buildDir) {
        setValue(PROPERTY_BUILD_DIR, buildDir);
    }

    @Override
    public Collection<IPath> getSubprojectPaths() {
        Collection<String> paths = getValues(PROPERTY_SUBPROJECTS, Collections.<String>emptyList());
        List<IPath> result = Lists.newArrayListWithCapacity(paths.size());
        for(String path : paths) {
            result.add(new Path(path));
        }
        return result;
    }

    @Override
    public void setSubprojectPaths(Collection<IPath> subprojectPaths) {
        List<String> paths = Lists.newArrayListWithCapacity(subprojectPaths.size());
        for (IPath path : subprojectPaths) {
            paths.add(path.toPortableString());
        }
        setValues(PROPERTY_SUBPROJECTS, paths);
    }

    @Override
    public String getValue(String key, String defaultValue) {
        String value = this.entries.get(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public void setValue(String key, String value) {
        if (value != null) {
            this.entries.put(key, value);
        } else if (this.entries.containsKey(key)) {
            this.entries.remove(key);
        }
    }

    @Override
    public Collection<String> getValues(String key, Collection<String> defaultValues) {
        String serializedForm = getValue(key, null);
        if (serializedForm == null) {
            return defaultValues;
        }
        return Splitter.on(File.pathSeparator).omitEmptyStrings().splitToList(serializedForm);
    }

    @Override
    public void setValues(String key, Collection<String> values) {
        setValue(key, values == null ? null : Joiner.on(File.pathSeparator).join(values));
    }
}
