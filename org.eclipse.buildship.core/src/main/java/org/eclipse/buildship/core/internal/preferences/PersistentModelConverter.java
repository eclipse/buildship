/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.preferences;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.buildship.core.internal.util.gradle.GradleVersion;

/**
 * Contains helper methods for the {@link PersistentModel} <-> {@link Properties} conversion.
 */
final class PersistentModelConverter {

    private static final String PROPERTY_BUILD_DIR = "buildDir";
    private static final String PROPERTY_BUILD_SCRIPT_PATH = "buildScriptPath";
    private static final String PROPERTY_SUBPROJECTS = "subprojectPaths";
    private static final String PROPERTY_CLASSPATH = "classpath";
    private static final String PROPERTY_DERIVED_RESOURCES = "derivedResources";
    private static final String PROPERTY_LINKED_RESOURCES = "linkedResources";
    private static final String PROPERTY_MANAGED_NATURES = "managedNatures";
    private static final String PROPERTY_MANAGED_BUILDERS = "managedBuilders";
    private static final String PROPERTY_HAS_AUTOBUILD_TASKS = "hasAutoBuildTasks";
    private static final String PROPERTY_GRADLE_VERSION = "gradleVersion";

    public static Properties toProperties(final PersistentModel model) {
        Properties properties = new Properties();

        storeValue(properties, PROPERTY_BUILD_DIR, model.getBuildDir(), new Function<IPath, String>() {

            @Override
            public String apply(IPath path) {
                return path.toPortableString();
            }
        });
        storeValue(properties, PROPERTY_BUILD_SCRIPT_PATH, model.getbuildScriptPath(), new Function<IPath, String>() {

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

        storeList(properties, PROPERTY_MANAGED_NATURES, model.getManagedNatures(), Functions.<String>identity());

        storeValue(properties, PROPERTY_MANAGED_BUILDERS, model.getManagedBuilders(), new Function<List<ICommand>, String>(){

            @Override
            public String apply(List<ICommand> commands) {
                return BuildCommandConverter.toXml(model.getProject(), commands);
            }
        });

        storeValue(properties, PROPERTY_HAS_AUTOBUILD_TASKS, model.hasAutoBuildTasks(), new Function<Boolean, String>() {

            @Override
            public String apply(Boolean hasAutoBuildTasks) {
                return hasAutoBuildTasks.toString();
            }
        });

        storeValue(properties, PROPERTY_GRADLE_VERSION, model.getGradleVersion(), new Function<GradleVersion, String>(){
            @Override
            public String apply(GradleVersion gradleVersion) {
                return gradleVersion.getVersion();
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
        IPath buildScriptPath = loadValue(properties, PROPERTY_BUILD_SCRIPT_PATH, new Path("build.gradle"), new Function<String, IPath>() {

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

        Collection<String> managedNatures = loadList(properties, PROPERTY_MANAGED_NATURES, Functions.<String>identity());

        Collection<ICommand> managedBuilders = loadValue(properties, PROPERTY_MANAGED_BUILDERS, ImmutableList.<ICommand>of(), new Function<String, List<ICommand>>() {

            @Override
            public List<ICommand> apply(String commands) {
                return BuildCommandConverter.toEntries(project, commands);
            }
        });

        boolean hasAutoBuildTasks = loadValue(properties, PROPERTY_HAS_AUTOBUILD_TASKS, Boolean.FALSE, new Function<String, Boolean>(){

            @Override
            public Boolean apply(String hasAutoBuildTasks) {
                return Boolean.valueOf(hasAutoBuildTasks);
            }});

        GradleVersion gradleVersion = loadValue(properties, PROPERTY_GRADLE_VERSION, null,  new Function<String, GradleVersion>(){
            @Override
            public GradleVersion apply(String version) {
                return GradleVersion.version(version);
            }
        });
        if (gradleVersion == null) {
            return new AbsentPersistentModel(project);
        } else {
            return new DefaultPersistentModel(project, buildDir, buildScriptPath, subprojects, classpath, derivedResources, linkedResources, managedNatures, managedBuilders, hasAutoBuildTasks, gradleVersion);
        }
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
