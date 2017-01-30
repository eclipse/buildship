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
import java.util.List;
import java.util.Properties;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;

import org.eclipse.core.resources.IProject;
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

    private static final String PROPERTY_BUILD_DIR = "buildDir";
    private static final String PROPERTY_SUBPROJECTS = "subprojectPaths";
    private static final String PROPERTY_CLASSPATH = "classpath";
    private static final String PROPERTY_DERIVED_RESOURCES = "derivedResources";
    private static final String PROPERTY_LINKED_RESOURCES = "linkedResources";

    private final IProject project;

    private IPath buildDir;
    private Collection<IPath> subprojectPaths;
    private List<IClasspathEntry> classpath;
    private Collection<IPath> derivedResources;
    private Collection<IPath> linkedResources;

    private DefaultPersistentModel(IProject project) {
        this.project = Preconditions.checkNotNull(project);
    }

    private DefaultPersistentModel(IProject project, IPath buildDir, Collection<IPath> subprojectPaths, List<IClasspathEntry> classpath, Collection<IPath> derivedResources,
            Collection<IPath> linkedResources) {
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
    public IPath getBuildDir() {
        return this.buildDir;
    }

    @Override
    public void setBuildDir(IPath buildDir) {
        this.buildDir = buildDir;
    }

    @Override
    public Collection<IPath> getSubprojectPaths() {
        return this.subprojectPaths;
    }

    @Override
    public void setSubprojectPaths(Collection<IPath> subprojectPaths) {
        this.subprojectPaths = subprojectPaths;
    }

    @Override
    public List<IClasspathEntry> getClasspath() {
        return this.classpath;
    }

    @Override
    public void setClasspath(List<IClasspathEntry> classpath) {
        this.classpath = classpath;
    }

    @Override
    public Collection<IPath> getDerivedResources() {
        return this.derivedResources;
    }

    @Override
    public void setDerivedResources(Collection<IPath> derivedResources) {
        this.derivedResources = derivedResources;
    }

    @Override
    public Collection<IPath> getLinkedResources() {
        return this.linkedResources;
    }

    @Override
    public void setLinkedResources(Collection<IPath> linkedResources) {
        this.linkedResources = linkedResources;
    }

    Properties asProperties() {
        Properties properties = new Properties();

        storeValue(properties, PROPERTY_BUILD_DIR, this.buildDir, new Function<IPath, String>() {

            @Override
            public String apply(IPath path) {
                return path.toPortableString();
            }
        });
        storeList(properties, PROPERTY_SUBPROJECTS, this.subprojectPaths, new Function<IPath, String>() {

            @Override
            public String apply(IPath path) {
                return path.toPortableString();
            }
        });
        storeValue(properties, PROPERTY_CLASSPATH, this.classpath, new Function<List<IClasspathEntry>, String>() {

            @Override
            public String apply(List<IClasspathEntry> classpath) {
                IJavaProject javaProject = JavaCore.create(DefaultPersistentModel.this.project);
                return ClasspathConverter.toXml(javaProject, classpath);
            }
        });
        storeList(properties, PROPERTY_DERIVED_RESOURCES, this.derivedResources, new Function<IPath, String>() {

            @Override
            public String apply(IPath path) {
                return path.toPortableString();
            }
        });

        storeList(properties, PROPERTY_LINKED_RESOURCES, this.linkedResources, new Function<IPath, String>() {

            @Override
            public String apply(IPath linkedResource) {
                return linkedResource.toPortableString();
            }
        });

        return properties;
    }

    static DefaultPersistentModel fromEmpty(IProject project) {
        return new DefaultPersistentModel(project);
    }

    static DefaultPersistentModel fromProperties(final IProject project, Properties properties) {
        IPath buildDir = loadValue(properties, PROPERTY_BUILD_DIR, new Function<String, IPath>() {

            @Override
            public IPath apply(String path) {
                return new Path(path);
            }
        });
        Collection<IPath> subprojects = loadList(properties, PROPERTY_SUBPROJECTS, new Function<String, IPath>() {

            @Override
            public IPath apply(String path) {
                return new Path(path);
            }
        });
        List<IClasspathEntry> classpath = loadValue(properties, PROPERTY_CLASSPATH, new Function<String, List<IClasspathEntry>>() {

            @Override
            public List<IClasspathEntry> apply(String classpath) {
                IJavaProject javaProject = JavaCore.create(project);
                return ClasspathConverter.toEntries(javaProject, classpath);
            }
        });
        Collection<IPath> derivedResources = loadList(properties, PROPERTY_DERIVED_RESOURCES, new Function<String, IPath>() {

            @Override
            public IPath apply(String path) {
                return new Path(path);
            }
        });
        Collection<IPath> linkedResources = loadList(properties, PROPERTY_LINKED_RESOURCES, new Function<String, IPath>() {

            @Override
            public IPath apply(String path) {
                return new Path(path);
            }
        });
        return new DefaultPersistentModel(project, buildDir, subprojects, classpath, derivedResources, linkedResources);
    }

    private static <T> T loadValue(Properties properties, String key, Function<String, T> conversion) {
        String value = (String) properties.get(key);
        if (value == null) {
            return null;
        } else {
            return conversion.apply(value);
        }
    }

    private static <T> List<T> loadList(Properties properties, String key, Function<String, T> conversion) {
        String values = (String) properties.get(key);
        if (values == null) {
            return null;
        } else {
            List<String> collection = Splitter.on(File.pathSeparator).omitEmptyStrings().splitToList(values);
            return FluentIterable.from(collection).transform(conversion).filter(Predicates.notNull()).toList();
        }
    }

    private static <T> void storeValue(Properties properties, String key, T value, Function<T, String> conversion) {
        if (value != null) {
            properties.put(key, conversion.apply(value));
        } else if (properties.containsKey(key)) {
            properties.remove(key);
        }
    }

    private static <T> void storeList(Properties properties, String key, Collection<T> values, Function<T, String> conversion) {
        if (values != null) {
            List<String> stringList = FluentIterable.from(values).transform(conversion).filter(Predicates.notNull()).toList();
            properties.put(key, Joiner.on(File.pathSeparator).join(stringList));
        } else if (properties.containsKey(key)) {
            properties.remove(key);
        }
    }
}
