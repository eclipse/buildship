/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel.internal;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.gradle.api.specs.Spec;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.ProjectIdentifier;
import org.gradle.tooling.model.gradle.GradleScript;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import org.eclipse.buildship.core.omnimodel.OmniBuildInvocations;
import org.eclipse.buildship.core.omnimodel.OmniBuildInvocationsContainer;
import org.eclipse.buildship.core.omnimodel.OmniGradleProject;
import org.eclipse.buildship.core.omnimodel.OmniGradleScript;
import org.eclipse.buildship.core.omnimodel.OmniProjectTask;
import org.eclipse.buildship.core.omnimodel.OmniTaskSelector;
import org.eclipse.buildship.core.util.gradle.Maybe;
import org.eclipse.buildship.core.util.gradle.Path;

/**
 * Default implementation of the {@link OmniGradleProject} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniGradleProject implements OmniGradleProject {

    private final HierarchyHelper<OmniGradleProject> hierarchyHelper;
    private String name;
    private String description;
    private Path path;
    private Maybe<File> projectDirectory;
    private ProjectIdentifier projectIdentifier;
    private Maybe<File> buildDirectory;
    private Maybe<OmniGradleScript> buildScript;
    private ImmutableList<OmniProjectTask> projectTasks;
    private ImmutableList<OmniTaskSelector> taskSelectors;

    private DefaultOmniGradleProject(Comparator<? super OmniGradleProject> comparator) {
        this.hierarchyHelper = new HierarchyHelper<OmniGradleProject>(this, Preconditions.checkNotNull(comparator));
    }

    @Override
    public String getName() {
        return this.name;
    }

    private void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Path getPath() {
        return this.path;
    }

    private void setPath(Path path) {
        this.path = path;
    }

    @Override
    public Maybe<File> getProjectDirectory() {
        return this.projectDirectory;
    }

    private void setProjectDirectory(Maybe<File> projectDirectory) {
        this.projectDirectory = projectDirectory;
    }


    @Override
    public ProjectIdentifier getProjectIdentifier() {
        return this.projectIdentifier;
    }

    private void setProjectIdentifier(ProjectIdentifier projectIdentifier) {
        this.projectIdentifier = projectIdentifier;
    }

    @Override
    public Maybe<File> getBuildDirectory() {
        return this.buildDirectory;
    }

    public void setBuildDirectory(Maybe<File> buildDirectory) {
        this.buildDirectory = buildDirectory;
    }

    @Override
    public Maybe<OmniGradleScript> getBuildScript() {
        return this.buildScript;
    }

    public void setBuildScript(Maybe<OmniGradleScript> buildScript) {
        this.buildScript = buildScript;
    }

    @Override
    public ImmutableList<OmniProjectTask> getProjectTasks() {
        return this.projectTasks;
    }

    public void setProjectTasks(List<OmniProjectTask> projectTasks) {
        this.projectTasks = ImmutableList.copyOf(projectTasks);
    }

    @Override
    public ImmutableList<OmniTaskSelector> getTaskSelectors() {
        return this.taskSelectors;
    }

    public void setTaskSelectors(List<OmniTaskSelector> taskSelectors) {
        this.taskSelectors = ImmutableList.copyOf(taskSelectors);
    }

    @Override
    public OmniGradleProject getRoot() {
        return this.hierarchyHelper.getRoot();
    }

    @Override
    public OmniGradleProject getParent() {
        return this.hierarchyHelper.getParent();
    }

    private void setParent(DefaultOmniGradleProject parent) {
        this.hierarchyHelper.setParent(parent);
    }

    @Override
    public ImmutableList<OmniGradleProject> getChildren() {
        return this.hierarchyHelper.getChildren();
    }

    private void addChild(DefaultOmniGradleProject child) {
        child.setParent(this);
        this.hierarchyHelper.addChild(child);
    }

    @Override
    public ImmutableList<OmniGradleProject> getAll() {
        return this.hierarchyHelper.getAll();
    }

    @Override
    public ImmutableList<OmniGradleProject> filter(Spec<? super OmniGradleProject> predicate) {
        return this.hierarchyHelper.filter(predicate);
    }

    @Override
    public Optional<OmniGradleProject> tryFind(Spec<? super OmniGradleProject> predicate) {
        return this.hierarchyHelper.tryFind(predicate);
    }

    public static DefaultOmniGradleProject from(GradleProject project) {
        return from(project, Maps.<ProjectIdentifier, DefaultOmniGradleProject>newHashMap());
    }

    public static DefaultOmniGradleProject from(GradleProject project, Map<ProjectIdentifier, DefaultOmniGradleProject> knownProjects) {
        OmniBuildInvocationsContainer buildInvocationsContainer = DefaultOmniBuildInvocationsContainerBuilder.build(project);
        return convert(project, buildInvocationsContainer, knownProjects);
    }

    private static DefaultOmniGradleProject convert(GradleProject project, OmniBuildInvocationsContainer buildInvocationsContainer, Map<ProjectIdentifier, DefaultOmniGradleProject> knownProjects) {
        ProjectIdentifier id = project.getProjectIdentifier();
        if (knownProjects.containsKey(id)) {
            return knownProjects.get(id);
        }
        DefaultOmniGradleProject gradleProject = new DefaultOmniGradleProject(OmniGradleProjectComparator.INSTANCE);
        knownProjects.put(id, gradleProject);
        gradleProject.setName(project.getName());
        gradleProject.setDescription(project.getDescription());
        gradleProject.setPath(Path.from(project.getPath()));
        gradleProject.setProjectIdentifier(project.getProjectIdentifier());
        setProjectDirectory(gradleProject, project);
        setBuildDirectory(gradleProject, project);
        setBuildScript(gradleProject, project);
        OmniBuildInvocations buildInvocations = buildInvocationsContainer.asMap().get(Path.from(project.getPath()));
        gradleProject.setProjectTasks(buildInvocations.getProjectTasks());
        gradleProject.setTaskSelectors(buildInvocations.getTaskSelectors());

        for (GradleProject child : project.getChildren()) {
            DefaultOmniGradleProject gradleProjectChild = convert(child, buildInvocationsContainer, knownProjects);
            gradleProject.addChild(gradleProjectChild);
        }

        return gradleProject;
    }

    /**
     * GradleProject#getProjectDirectory is only available in Gradle versions >= 2.4.
     *
     * @param gradleProject the project to populate
     * @param project the project model
     */
    private static void setProjectDirectory(DefaultOmniGradleProject gradleProject, GradleProject project) {
        try {
            File projectDirectory = project.getProjectDirectory();
            gradleProject.setProjectDirectory(Maybe.of(projectDirectory));
        } catch (Exception ignore) {
            gradleProject.setProjectDirectory(Maybe.<File>absent());
        }
    }

    /**
     * GradleProject#getBuildDirectory is only available in Gradle versions >= 2.0.
     *
     * @param gradleProject the project to populate
     * @param project the project model
     */
    private static void setBuildDirectory(DefaultOmniGradleProject gradleProject, GradleProject project) {
        try {
            File buildDirectory = project.getBuildDirectory();
            gradleProject.setBuildDirectory(Maybe.of(buildDirectory));
        } catch (Exception ignore) {
            gradleProject.setBuildDirectory(Maybe.<File>absent());
        }
    }

    /**
     * GradleProject#getBuildScript is only available in Gradle versions >= 1.8.
     *
     * @param gradleProject the project to populate
     * @param project the project model
     */
    private static void setBuildScript(DefaultOmniGradleProject gradleProject, GradleProject project) {
        try {
            GradleScript buildScript = project.getBuildScript();
            gradleProject.setBuildScript(Maybe.<OmniGradleScript>of(DefaultOmniGradleScript.from(buildScript)));
        } catch (Exception ignore) {
            gradleProject.setBuildScript(Maybe.<OmniGradleScript>absent());
        }
    }

    /**
     * Singleton comparator to compare {@code OmniGradleProject} instances by their project path.
     */
    private enum  OmniGradleProjectComparator implements Comparator<OmniGradleProject> {

        INSTANCE;

        @Override
        public int compare(OmniGradleProject o1, OmniGradleProject o2) {
            return o1.getPath().compareTo(o2.getPath());
        }

    }

}

