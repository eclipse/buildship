/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.model;

import java.io.File;
import java.util.Comparator;
import java.util.List;

import org.gradle.api.JavaVersion;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.ProjectIdentifier;
import org.gradle.tooling.model.UnsupportedMethodException;
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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;

import org.eclipse.buildship.core.util.gradle.Path;

/**
 * {@link EclipseProject} decorator that provide default values when the model is returned from
 * older target Gradle version.
 *
 * @author Donat Csikos
 */
public final class CompatEclipseProject implements EclipseProject {

    static final EclipseJavaSourceSettings FALLBACK_JAVA_SOURCE_SETTINGS = new FallbackJavaSourceSettings();
    static final EclipseOutputLocation FALLBACK_OUTPUT_LOCATION = new FallbackOutputLocation();

    private final EclipseProject delegate;

    public CompatEclipseProject(EclipseProject delegate) {
        this.delegate = delegate;
    }

    /**
     * Returns an empty collection for Gradle versions < 2.9.
     */
    @Override
    public DomainObjectSet<? extends EclipseBuildCommand> getBuildCommands() {
        try {
            return this.delegate.getBuildCommands();
        } catch (Exception ignore) {
            return CompatHelper.emptyDomainSet();
        }
    }

    @Override
    public DomainObjectSet<? extends EclipseProject> getChildren() {
        Builder<EclipseProject> result = ImmutableList.builder();
        for (EclipseProject child : this.delegate.getChildren()) {
            result.add(new CompatEclipseProject(child));
        }
        return CompatHelper.asDomainSet(result.build());
    }

    @Override
    public DomainObjectSet<? extends EclipseExternalDependency> getClasspath() {
        DomainObjectSet<? extends EclipseExternalDependency> dependencies = this.delegate.getClasspath();
        List<EclipseExternalDependency> result = Lists.newArrayListWithCapacity(dependencies.size());
        for (EclipseExternalDependency dependency : dependencies) {
            result.add(new CompatEclipseExternalDependency(dependency));
        }
        return CompatHelper.asDomainSet(result);
    }

    /**
     * Returns null for Gradle versions < 3.0.
     */
    @Override
    public DomainObjectSet<? extends EclipseClasspathContainer> getClasspathContainers() {
        try {
            return this.delegate.getClasspathContainers();
        } catch (Exception ignore) {
            return null;
        }
    }

    @Override
    public String getDescription() {
        return this.delegate.getDescription();
    }

    @Override
    public GradleProject getGradleProject() {
        return new CompatGradleProject(this.delegate.getGradleProject());
    }

    /**
     * For Java projects with Gradle versions < 2.10 a default result is returned.
     */
    @Override
    public EclipseJavaSourceSettings getJavaSourceSettings() {
        try {
            EclipseJavaSourceSettings sourceSettings = this.delegate.getJavaSourceSettings();
            return sourceSettings == null ? null : new CompatSourceSettings(sourceSettings);
        } catch (Exception e) {
            return getSourceDirectories().isEmpty() ? null : FALLBACK_JAVA_SOURCE_SETTINGS;
        }
    }

    @Override
    public DomainObjectSet<? extends EclipseLinkedResource> getLinkedResources() {
        return this.delegate.getLinkedResources();
    }

    @Override
    public String getName() {
        return this.delegate.getName();
    }

    /**
     * Returns the 'bin' folder for Gradle versions < 3.0
     */
    @Override
    public EclipseOutputLocation getOutputLocation() {
        try {
            return this.delegate.getOutputLocation();
        } catch (Exception ignore) {
            return FALLBACK_OUTPUT_LOCATION;
        }
    }

    @Override
    public EclipseProject getParent() {
        EclipseProject parent  = this.delegate.getParent();
        return parent == null ? parent : new CompatEclipseProject(parent);
    }

    @Override
    public DomainObjectSet<? extends EclipseProjectDependency> getProjectDependencies() {
        DomainObjectSet<? extends EclipseProjectDependency> projectDependencies = this.delegate.getProjectDependencies();
        List<EclipseProjectDependency> result = Lists.newArrayListWithCapacity(projectDependencies.size());
        for (EclipseProjectDependency dependency : projectDependencies) {
            result.add(new CompatEclipseProjectDependency(dependency));
        }
        return CompatHelper.asDomainSet(result);
    }

    @Override
    public File getProjectDirectory() throws UnsupportedMethodException {
        return this.delegate.getProjectDirectory();
    }

    @Override
    public ProjectIdentifier getProjectIdentifier() {
        return this.delegate.getProjectIdentifier();
    }

    /**
     * Returns an empty set for Gradle versions < 2.9.
     */
    @Override
    public DomainObjectSet<? extends EclipseProjectNature> getProjectNatures() {
        try {
            return this.delegate.getProjectNatures();
        } catch (Exception e) {
            return CompatHelper.emptyDomainSet();
        }
    }

    @Override
    public DomainObjectSet<? extends CompatEclipseSourceDirectory> getSourceDirectories() {
        // TODO (donat) remove duplication; maybe move the creation to a static factory
        DomainObjectSet<? extends EclipseSourceDirectory> directories = this.delegate.getSourceDirectories();
        List<CompatEclipseSourceDirectory> result = Lists.newArrayListWithCapacity(directories.size());
        for (EclipseSourceDirectory directory : directories) {
            result.add(new CompatEclipseSourceDirectory(directory));
        }
        return CompatHelper.<CompatEclipseSourceDirectory>asDomainSet(result);
    }

    public static EclipseProject getRoot(EclipseProject project) {
        // TODO (donat) remove duplication
        HierarchyHelper<EclipseProject> hierarchyHelper = new HierarchyHelper<EclipseProject>(project, Preconditions.checkNotNull(EclipseProjectComparator.INSTANCE));
        return hierarchyHelper.getRoot();
    }

    // TODO (donat) move it inside CompatEclipseProject
    public static List<EclipseProject> getAll(EclipseProject project) {
        HierarchyHelper<EclipseProject> hierarchyHelper = new HierarchyHelper<EclipseProject>(project, Preconditions.checkNotNull(EclipseProjectComparator.INSTANCE));
        return hierarchyHelper.getAll();
    }

    public List<CompatEclipseProject> getAll() {
        HierarchyHelper<CompatEclipseProject> hierarchyHelper = new HierarchyHelper<CompatEclipseProject>(this, Preconditions.checkNotNull(EclipseProjectComparator.INSTANCE));
        return hierarchyHelper.getAll();
    }

    private static final class FallbackOutputLocation implements EclipseOutputLocation {

        @Override
        public String getPath() {
            return "bin";
        }

    }

    private static enum EclipseProjectComparator implements Comparator<EclipseProject> {

        INSTANCE;

        @Override
        public int compare(EclipseProject o1, EclipseProject o2) {
            Path p1 = Path.from(o1.getGradleProject().getPath());
            Path p2 = Path.from(o2.getGradleProject().getPath());
            return p1.compareTo(p2);
        }

    }

    private static final class FallbackJavaSourceSettings implements EclipseJavaSourceSettings {

        @Override
        public JavaVersion getTargetBytecodeVersion() throws UnsupportedMethodException {
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
}
