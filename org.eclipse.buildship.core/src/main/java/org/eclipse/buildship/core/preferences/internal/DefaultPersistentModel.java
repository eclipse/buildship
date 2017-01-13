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
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.buildship.core.preferences.PersistentModel;

/**
 * Default implementation for {@link PersistentModel}.
 *
 * @author Donat Csikos
 */
class DefaultPersistentModel implements PersistentModel {

    private static final String PROPERTY_SUBPROJECTS = "subprojectPaths";
    private static final String PROPERTY_BUILD_DIR = "buildDir";
    private static final String PROPERTY_CLASSPATH = "classpath";
    private static final String PROPERTY_DERIVED_RESOURCES = "derivedResources";
    private static final String PROPERTY_LINKED_RESOURCES = "linkedResources";

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
    public Optional<IPath> getBuildDir() {
        String buildDir = getValue(PROPERTY_BUILD_DIR, null);
        return buildDir == null ? Optional.<IPath>absent() : Optional.<IPath>of(new Path(buildDir));
    }

    @Override
    public void setBuildDir(Optional<IPath> buildDirPath) {
        String buildDir = buildDirPath.isPresent() ? buildDirPath.get().toPortableString() : null;
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
    public Optional<List<IClasspathEntry>> getClasspath() {
        String classpath = getValue(PROPERTY_CLASSPATH, null);
        if (classpath == null) {
            return Optional.absent();
        } else {
            IJavaProject javaProject = JavaCore.create(this.project);
            return Optional.of(ClasspathConverter.toEntries(javaProject, classpath));
        }
    }

    @Override
    public void setClasspath(List<IClasspathEntry> classpath) {
       IJavaProject javaProject = JavaCore.create(this.project);
       String serialized = ClasspathConverter.toXml(javaProject, classpath);
       setValue(PROPERTY_CLASSPATH, serialized);
    }

    @Override
    public Collection<IResource> getDerivedResources() {
        Collection<IResource> result = Lists.newArrayList();
        Collection<String> resourcePaths = getValues(PROPERTY_DERIVED_RESOURCES, Collections.<String>emptyList());
        for (String path : resourcePaths) {
            IResource resource = this.project.findMember(path);
            if (resource != null) {
                result.add(resource);
            }
        }
        return result;
    }

    @Override
    public void setDerivedResources(Collection<IResource> derivedResources) {
        Collection<String> result = Lists.newArrayList();
        for (IResource resource : derivedResources) {
            String path = resource.getProjectRelativePath().toPortableString();
            result.add(path);
        }
        setValues(PROPERTY_DERIVED_RESOURCES, result);
    }

    @Override
    public Collection<IFolder> getLinkedResources() {
        Collection<IFolder> result = Lists.newArrayList();
        Collection<String> resources = getValues(PROPERTY_LINKED_RESOURCES, Collections.<String>emptyList());
        for (String resource : resources) {
            result.add(this.project.getFolder(resource));
        }
        return result;
    }

    @Override
    public void setLinkedResources(Collection<IFolder> linkedResources) {
        Collection<String> result = Lists.newArrayList();
        for (IFolder linkedResource : linkedResources) {
            result.add(projectRelativePath(linkedResource));
        }
        setValues(PROPERTY_LINKED_RESOURCES, result);
    }

    private String projectRelativePath(IFolder folder) {
        return folder.getFullPath().makeRelativeTo(this.project.getFullPath()).toPortableString();
    }

    public String getValue(String key, String defaultValue) {
        String value = this.entries.get(key);
        return value != null ? value : defaultValue;
    }

    public void setValue(String key, String value) {
        if (value != null) {
            this.entries.put(key, value);
        } else if (this.entries.containsKey(key)) {
            this.entries.remove(key);
        }
    }

    public Collection<String> getValues(String key, Collection<String> defaultValues) {
        String serializedForm = getValue(key, null);
        if (serializedForm == null) {
            return defaultValues;
        }
        return Splitter.on(File.pathSeparator).omitEmptyStrings().splitToList(serializedForm);
    }

    public void setValues(String key, Collection<String> values) {
        setValue(key, values == null ? null : Joiner.on(File.pathSeparator).join(values));
    }
}
