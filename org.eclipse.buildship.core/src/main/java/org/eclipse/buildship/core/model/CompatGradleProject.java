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

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.GradleTask;
import org.gradle.tooling.model.ProjectIdentifier;
import org.gradle.tooling.model.UnsupportedMethodException;
import org.gradle.tooling.model.gradle.GradleScript;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

/**
 * Decorator class for {@link GradleProject} providing sensible defaults for older models.
 *
 * @author Donat Csikos
 */
public final class CompatGradleProject implements GradleProject {

    private final GradleProject delegate;

    public CompatGradleProject(GradleProject delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getDescription() {
        return this.delegate.getDescription();
    }

    @Override
    public String getName() {
        return this.delegate.getName();
    }

    @Override
    public GradleProject findByPath(String path) {
        return this.delegate.findByPath(path);
    }

    /**
     * If Gradle versions < 1.8 then <code>null</code> is returned.
     */
    @Override
    public File getBuildDirectory() {
        try {
            return this.delegate.getBuildDirectory();
        } catch (Exception ignore) {
            return null;
        }
    }

    /**
     * If Gradle versions < 1.8 then <code>null</code> is returned.
     */
    @Override
    public GradleScript getBuildScript() throws UnsupportedMethodException {
        try {
            return this.delegate.getBuildScript();
        } catch (Exception ignore) {
            return null;
        }
    }

    @Override
    public DomainObjectSet<? extends GradleProject> getChildren() {
        Builder<GradleProject> result = ImmutableList.builder();
        for (GradleProject child : this.delegate.getChildren()) {
            result.add(new CompatGradleProject(child));
        }
        return CompatHelper.asDomainSet(result.build());
    }

    @Override
    public GradleProject getParent() {
        GradleProject parent  = this.delegate.getParent();
        return parent == null ? parent : new CompatGradleProject(parent);
    }

    @Override
    public String getPath() {
        return this.delegate.getPath();
    }

    /**
     * If Gradle versions < 2.4 then <code>null</code> is returned.
     */
    @Override
    public File getProjectDirectory() {
        try {
            return this.delegate.getProjectDirectory();
        } catch (Exception ignore) {
            return null;
        }
    }

    @Override
    public ProjectIdentifier getProjectIdentifier() {
        return this.delegate.getProjectIdentifier();
    }

    @Override
    public DomainObjectSet<? extends GradleTask> getTasks() {
        ImmutableList<GradleTask> tasks = FluentIterable.from(this.delegate.getTasks()).transform(new Function<GradleTask, GradleTask>() {

            @Override
            public GradleTask apply(GradleTask task) {
                return new CompatTask(task);
            }
        }).toList();
        return CompatHelper.asDomainSet(tasks);
    }

    public static GradleProject getRoot(GradleProject project) {
        HierarchyHelper<GradleProject> hierarchyHelper = new HierarchyHelper<GradleProject>(project, Preconditions.checkNotNull(GradleProjectComparator.INSTANCE));
        return hierarchyHelper.getRoot();
    }

    public static List<GradleProject> getAll(GradleProject project) {
        HierarchyHelper<GradleProject> hierarchyHelper = new HierarchyHelper<GradleProject>(project, Preconditions.checkNotNull(GradleProjectComparator.INSTANCE));
        return hierarchyHelper.getAll();
    }

    /**
     * Compares projects based on their paths.
     */
    private enum GradleProjectComparator implements Comparator<GradleProject> {

        INSTANCE;

        @Override
        public int compare(GradleProject o1, GradleProject o2) {
            return o1.getPath().compareTo(o2.getPath());
        }
    }
}
