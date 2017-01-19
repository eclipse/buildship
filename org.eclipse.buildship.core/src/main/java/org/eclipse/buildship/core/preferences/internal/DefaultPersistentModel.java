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
import java.util.Properties;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

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

    private final IProject project;

    private IPath buildDir;
    private Collection<IPath> subprojectPaths;
    private List<IClasspathEntry> classpath;
    private Collection<IResource> derivedResources;
    private Collection<IFolder> linkedResources;

    private DefaultPersistentModel(IProject project) {
        this.project = Preconditions.checkNotNull(project);
    }

    private DefaultPersistentModel(IProject project, IPath buildDir, Collection<IPath> subprojectPaths, List<IClasspathEntry> classpath, Collection<IResource> derivedResources, Collection<IFolder> linkedResources) {
        this(project);
        this.buildDir = buildDir;
        this.subprojectPaths = subprojectPaths;
        this.classpath = classpath;
        this.derivedResources = derivedResources;
        this.linkedResources = linkedResources;
    }

    IProject getProject() {
        return this.project;
    }

    @Override
    public Optional<IPath> getBuildDir() {
        return this.buildDir == null ? Optional.<IPath>absent() : Optional.of(this.buildDir);
    }

    @Override
    public void setBuildDir(Optional<IPath> buildDir) {
       this.buildDir = buildDir.orNull();
    }

    @Override
    public Collection<IPath> getSubprojectPaths() {
        return this.subprojectPaths == null ? Collections.<IPath>emptyList() : this.subprojectPaths;
    }

    @Override
    public void setSubprojectPaths(Collection<IPath> subprojectPaths) {
        this.subprojectPaths = subprojectPaths;
    }

    @Override
    public Optional<List<IClasspathEntry>> getClasspath() {
        return this.classpath == null ? Optional.<List<IClasspathEntry>>absent() : Optional.of(this.classpath);
    }

    @Override
    public void setClasspath(List<IClasspathEntry> classpath) {
        this.classpath = classpath;
    }

    @Override
    public Collection<IResource> getDerivedResources() {
        return this.derivedResources == null ? Collections.<IResource>emptyList() : this.derivedResources;
    }

    @Override
    public void setDerivedResources(Collection<IResource> derivedResources) {
        this.derivedResources = derivedResources;
    }

    @Override
    public Collection<IFolder> getLinkedResources() {
        return this.linkedResources == null ? Collections.<IFolder>emptyList() : this.linkedResources;
    }

    @Override
    public void setLinkedResources(Collection<IFolder> linkedResources) {
        this.linkedResources = linkedResources;
    }

    public Properties asProperties() {
        return Storage.toProperties(this);
    }

    public static DefaultPersistentModel fromProperties(IProject project, Properties properties) {
        return Storage.fromProperties(project, properties);
    }

    public static DefaultPersistentModel fromEmpty(IProject project) {
        return new DefaultPersistentModel(project);
    }

    private static class Storage {
        private static final String PROPERTY_BUILD_DIR = "buildDir";
        private static final String PROPERTY_SUBPROJECTS = "subprojectPaths";
        private static final String PROPERTY_CLASSPATH = "classpath";
        private static final String PROPERTY_DERIVED_RESOURCES = "derivedResources";
        private static final String PROPERTY_LINKED_RESOURCES = "linkedResources";

        static DefaultPersistentModel fromProperties(IProject project, Properties properties) {
            String prop = getValue(PROPERTY_BUILD_DIR, null, properties);
            IPath buildDir = prop == null ? null : new Path(prop);

            Collection<String> entries = getValues(PROPERTY_SUBPROJECTS, Collections.<String>emptyList(), properties);
            List<IPath> subprojects = Lists.newArrayListWithCapacity(entries.size());
            for(String path : entries) {
                subprojects.add(new Path(path));
            }

            prop = getValue(PROPERTY_CLASSPATH, null, properties);
            List<IClasspathEntry> classpath;
            if (prop == null) {
                classpath = null;
            } else {
                IJavaProject javaProject = JavaCore.create(project);
                classpath = ClasspathConverter.toEntries(javaProject, prop);
            }

            entries = getValues(PROPERTY_DERIVED_RESOURCES, Collections.<String>emptyList(), properties);
            Collection<IResource> derivedResources = Lists.newArrayListWithCapacity(entries.size());
            for (String path : entries) {
                IResource resource = project.findMember(path);
                if (resource != null) {
                    derivedResources.add(resource);
                }
            }

            entries = getValues(PROPERTY_LINKED_RESOURCES, Collections.<String>emptyList(), properties);
            Collection<IFolder> linkedResources = Lists.newArrayListWithCapacity(entries.size());
            for (String path : entries) {
                linkedResources.add(project.getFolder(path));
            }

            return new DefaultPersistentModel(project, buildDir, subprojects, classpath, derivedResources, linkedResources);
        }

        static Properties toProperties(DefaultPersistentModel model) {
            Properties properties = new Properties();

            String buildDir = model.buildDir == null ? null : model.buildDir.toPortableString();
            setValue(PROPERTY_BUILD_DIR, buildDir, properties);

            List<String> paths = Lists.newArrayListWithCapacity(model.subprojectPaths.size());
            for (IPath path : model.subprojectPaths) {
                paths.add(path.toPortableString());
            }
            setValues(PROPERTY_SUBPROJECTS, paths, properties);

            IJavaProject javaProject = JavaCore.create(model.project);
            String serialized = ClasspathConverter.toXml(javaProject, model.classpath);
            setValue(PROPERTY_CLASSPATH, serialized, properties);

            Collection<String> resources = Lists.newArrayList();
            for (IResource resource : model.derivedResources) {
                String path = resource.getProjectRelativePath().toPortableString();
                resources.add(path);
            }
            setValues(PROPERTY_DERIVED_RESOURCES, resources, properties);

            Collection<String> linkedResources = Lists.newArrayList();
            for (IFolder linkedResource : model.linkedResources) {
                linkedResources.add(projectRelativePath(model.project, linkedResource));
            }
            setValues(PROPERTY_LINKED_RESOURCES, linkedResources, properties);

            return properties;
        }

        private static String projectRelativePath(IProject project, IFolder folder) {
            return folder.getFullPath().makeRelativeTo(project.getFullPath()).toPortableString();
        }


        private static String getValue(String key, String defaultValue, Properties properties) {
            String value = (String) properties.getOrDefault(key, defaultValue);
            return value != null ? value : defaultValue;
        }

        private static void setValue(String key, String value, Properties properties) {
            if (value != null) {
                properties.put(key, value);
            } else if (properties.containsKey(key)) {
                properties.remove(key);
            }
        }

        private static Collection<String> getValues(String key, Collection<String> defaultValues, Properties properties) {
            String serializedForm = (String) properties.get(key);
            if (serializedForm == null) {
                return defaultValues;
            }
            return Splitter.on(File.pathSeparator).omitEmptyStrings().splitToList(serializedForm);
        }

        private static void setValues(String key, Collection<String> values, Properties properties) {
            setValue(key, values == null ? null : Joiner.on(File.pathSeparator).join(values), properties);
        }

    }



}
