/*
 * Copyright (c) 2017 the original author or authors.
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
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.buildship.core.preferences.PersistentModel;

/**
 * Contains helper methods for the {@link PersistentModel} <-> {@link Properties} conversion.
 */
final class PersistentModelConverter {

    private static final String PROPERTY_BUILD_DIR = "buildDir";
    private static final String PROPERTY_SUBPROJECTS = "subprojectPaths";
    private static final String PROPERTY_CLASSPATH = "classpath";
    private static final String PROPERTY_DERIVED_RESOURCES = "derivedResources";
    private static final String PROPERTY_LINKED_RESOURCES = "linkedResources";

    public static Properties toProperties(final PersistentModel model) {
        Properties properties = new Properties();

        storeValue(properties, PROPERTY_BUILD_DIR, model.getBuildDir(), new Function<IPath, String>() {

            @Override
            public String apply(IPath path) {
                return path.toPortableString();
            }
        });
        storeList(properties, PROPERTY_SUBPROJECTS, model.getSubprojectPaths(), new Function<IPath, String>() {

            @Override
            public String apply(IPath path) {
                return path.toPortableString();
            }
        });
        storeValue(properties, PROPERTY_CLASSPATH, model.getClasspath(), new Function<List<IClasspathEntry>, String>() {

            @Override
            public String apply(List<IClasspathEntry> classpath) {
                IJavaProject javaProject = JavaCore.create(model.getProject());
                return ClasspathConverter.toXml(javaProject, classpath);
            }
        });
        storeList(properties, PROPERTY_DERIVED_RESOURCES, model.getDerivedResources(), new Function<IPath, String>() {

            @Override
            public String apply(IPath path) {
                return path.toPortableString();
            }
        });

        storeList(properties, PROPERTY_LINKED_RESOURCES, model.getLinkedResources(), new Function<IPath, String>() {

            @Override
            public String apply(IPath linkedResource) {
                return linkedResource.toPortableString();
            }
        });

        return properties;
    }

    public static PersistentModel toModel(final IProject project, Properties properties) {
        IPath buildDir = loadValue(properties, PROPERTY_BUILD_DIR, new Path("build"), new Function<String, IPath>() {

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
        List<IClasspathEntry> classpath = loadValue(properties, PROPERTY_CLASSPATH, ImmutableList.<IClasspathEntry>of(), new Function<String, List<IClasspathEntry>>() {

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
        return PersistentModel.builder(project).buildDir(buildDir).subprojectPaths(subprojects).classpath(classpath).derivedResources(derivedResources)
                .linkedResources(linkedResources).build();
    }

    private static <T> T loadValue(Properties properties, String key, T defaultValue, Function<String, T> conversion) {
        String value = (String) properties.get(key);
        if (value == null) {
            return defaultValue;
        } else {
            return conversion.apply(value);
        }
    }

    private static <T> List<T> loadList(Properties properties, String key, Function<String, T> conversion) {
        String values = (String) properties.get(key);
        if (values == null) {
            return ImmutableList.of();
        } else {
            List<String> collection = Splitter.on(File.pathSeparator).omitEmptyStrings().splitToList(values);
            return FluentIterable.from(collection).transform(conversion).filter(Predicates.notNull()).toList();
        }
    }

    private static <T> void storeValue(Properties properties, String key, T value, Function<T, String> conversion) {
        properties.put(key, conversion.apply(value));
    }

    private static <T> void storeList(Properties properties, String key, Collection<T> values, Function<T, String> conversion) {
        List<String> stringList = FluentIterable.from(values).transform(conversion).filter(Predicates.notNull()).toList();
        properties.put(key, Joiner.on(File.pathSeparator).join(stringList));
    }
}
