/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.util.gradle;

import java.io.File;
import java.util.List;

import org.gradle.api.JavaVersion;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.ProjectIdentifier;
import org.gradle.tooling.model.eclipse.EclipseBuildCommand;
import org.gradle.tooling.model.eclipse.EclipseClasspathContainer;
import org.gradle.tooling.model.eclipse.EclipseExternalDependency;
import org.gradle.tooling.model.eclipse.EclipseJavaSourceSettings;
import org.gradle.tooling.model.eclipse.EclipseLinkedResource;
import org.gradle.tooling.model.eclipse.EclipseOutputLocation;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.eclipse.EclipseProjectDependency;
import org.gradle.tooling.model.eclipse.EclipseProjectNature;
import org.gradle.tooling.model.eclipse.EclipseSourceDirectory;
import org.gradle.tooling.model.java.InstalledJdk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;

/**
 * Compatibility decorator for {@link EclipseProject}.
 *
 * @author Donat Csikos
 */
public class CompatEclipseProject extends CompatModelElement<EclipseProject> implements EclipseProject {

    private static final DomainObjectSet<? extends EclipseClasspathContainer> UNSUPPORTED_CONTAINERS = ModelUtils.emptyDomainObjectSet();

    static final EclipseJavaSourceSettings FALLBACK_JAVA_SOURCE_SETTINGS = new FallbackJavaSourceSettings();
    static final EclipseOutputLocation FALLBACK_OUTPUT_LOCATION = new FallbackOutputLocation();

    CompatEclipseProject(EclipseProject delegate) {
        super(delegate);
    }

    @Override
    public DomainObjectSet<? extends EclipseBuildCommand> getBuildCommands() {
        // returns an empty collection for Gradle versions < 2.9
        try {
            return getElement().getBuildCommands();
        } catch (Exception ignore) {
            return ModelUtils.emptyDomainObjectSet();
        }
    }

    @Override
    public DomainObjectSet<? extends EclipseProject> getChildren() {
        Builder<EclipseProject> result = ImmutableList.builder();
        for (EclipseProject child : getElement().getChildren()) {
            result.add(new CompatEclipseProject(child));
        }
        return ModelUtils.asDomainObjectSet(result.build());
    }

    @Override
    public DomainObjectSet<? extends EclipseExternalDependency> getClasspath() {
        DomainObjectSet<? extends EclipseExternalDependency> dependencies = getElement().getClasspath();
        List<EclipseExternalDependency> result = Lists.newArrayListWithCapacity(dependencies.size());
        for (EclipseExternalDependency dependency : dependencies) {
            result.add(new CompatEclipseExternalDependency(dependency));
        }
        return ModelUtils.asDomainObjectSet(result);
    }

    @Override
    public DomainObjectSet<? extends EclipseClasspathContainer> getClasspathContainers() {
        try {
            return getElement().getClasspathContainers();
        } catch (Exception ignore) {
            return UNSUPPORTED_CONTAINERS;
        }
    }

    @Override
    public String getDescription() {
        return getElement().getDescription();
    }

    @Override
    public GradleProject getGradleProject() {
        return new CompatGradleProject(getElement().getGradleProject());
    }

    @Override
    public EclipseJavaSourceSettings getJavaSourceSettings() {
        // returns fallback settings for Gradle versions < 2.10
        try {
            EclipseJavaSourceSettings sourceSettings = getElement().getJavaSourceSettings();
            return sourceSettings == null ? null : new CompatSourceSettings(sourceSettings);
        } catch (Exception e) {
            return getSourceDirectories().isEmpty() ? null : FALLBACK_JAVA_SOURCE_SETTINGS;
        }
    }

    @Override
    public DomainObjectSet<? extends EclipseLinkedResource> getLinkedResources() {
        return getElement().getLinkedResources();
    }

    @Override
    public String getName() {
        return getElement().getName();
    }

    @Override
    public EclipseOutputLocation getOutputLocation() {
        // returns the 'bin' folder for Gradle versions < 3.0
        try {
            EclipseOutputLocation outputLocation = getElement().getOutputLocation();
            return outputLocation != null ? outputLocation : FALLBACK_OUTPUT_LOCATION;
        } catch (Exception ignore) {
            return FALLBACK_OUTPUT_LOCATION;
        }
    }

    @Override
    public EclipseProject getParent() {
        EclipseProject parent = getElement().getParent();
        return parent == null ? parent : new CompatEclipseProject(parent);
    }

    @Override
    public DomainObjectSet<? extends EclipseProjectDependency> getProjectDependencies() {
        DomainObjectSet<? extends EclipseProjectDependency> projectDependencies = getElement().getProjectDependencies();
        List<EclipseProjectDependency> result = Lists.newArrayListWithCapacity(projectDependencies.size());
        for (EclipseProjectDependency dependency : projectDependencies) {
            result.add(new CompatEclipseProjectDependency(dependency));
        }
        return ModelUtils.asDomainObjectSet(result);
    }

    @Override
    public File getProjectDirectory() {
        return getElement().getProjectDirectory();
    }

    @Override
    public ProjectIdentifier getProjectIdentifier() {
        return getElement().getProjectIdentifier();
    }

    @Override
    public DomainObjectSet<? extends EclipseProjectNature> getProjectNatures() {
        // returns an empty set for Gradle versions < 2.9
        try {
            return getElement().getProjectNatures();
        } catch (Exception e) {
            return ModelUtils.emptyDomainObjectSet();
        }
    }

    @Override
    public DomainObjectSet<? extends EclipseSourceDirectory> getSourceDirectories() {
        DomainObjectSet<? extends EclipseSourceDirectory> directories = getElement().getSourceDirectories();
        List<CompatEclipseSourceDirectory> result = Lists.newArrayListWithCapacity(directories.size());
        for (EclipseSourceDirectory directory : directories) {
            result.add(new CompatEclipseSourceDirectory(directory));
        }
        return ModelUtils.<CompatEclipseSourceDirectory> asDomainObjectSet(result);
    }

    /**
     * Supplies a default output location for older Gradle versions.
     */
    private static final class FallbackOutputLocation implements EclipseOutputLocation {

        @Override
        public String getPath() {
            return "bin";
        }
    }

    /**
     * Source settings to use for older Gradle distributions.
     */
    private static final class FallbackJavaSourceSettings implements EclipseJavaSourceSettings {

        @Override
        public JavaVersion getTargetBytecodeVersion() {
            return JavaVersion.current();
        }

        @Override
        public JavaVersion getSourceLanguageLevel() {
            return JavaVersion.current();
        }

        @Override
        public InstalledJdk getJdk() {
            return new InstalledJdk() {

                @Override
                public JavaVersion getJavaVersion() {
                    return JavaVersion.current();
                }

                @Override
                public File getJavaHome() {
                    return new File(System.getProperty("java.home")).getAbsoluteFile();
                }
            };
        }
    }

    public static boolean supportsClasspathContainers(EclipseProject project) {
        return project.getClasspathContainers() != UNSUPPORTED_CONTAINERS;
    }
}
