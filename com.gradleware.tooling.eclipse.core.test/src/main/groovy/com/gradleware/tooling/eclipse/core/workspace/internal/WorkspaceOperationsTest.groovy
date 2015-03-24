/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package com.gradleware.tooling.eclipse.core.workspace.internal

import com.google.common.collect.ImmutableList
import com.gradleware.tooling.eclipse.core.CorePlugin
import com.gradleware.tooling.eclipse.core.GradleNature
import com.gradleware.tooling.eclipse.core.GradlePluginsRuntimeException
import com.gradleware.tooling.eclipse.core.configuration.ProjectConfiguration
import com.gradleware.tooling.eclipse.core.workspace.ClasspathDefinition
import com.gradleware.tooling.eclipse.core.workspace.WorkspaceOperations
import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes
import org.eclipse.core.resources.IMarker
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.launching.JavaRuntime
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

class WorkspaceOperationsTest extends Specification {

    @Shared
    WorkspaceOperations workspaceOperations = CorePlugin.workspaceOperations();

    @Rule
    TemporaryFolder tempFolder

    def cleanup() {
        workspaceOperations.deleteAllProjects(new NullProgressMonitor())
    }

    def "can create a new project"() {
        setup:
        workspaceOperations.deleteAllProjects(new NullProgressMonitor())
        def projectFolder = tempFolder.newFolder("sample-project-folder")

        when:
        IProject project = workspaceOperations.createProject("sample-project", projectFolder, ImmutableList.of(), ImmutableList.of(GradleNature.ID), null)

        then:
        project.exists()
        project.isAccessible()
        project.hasNature(GradleNature.ID)
        project.getDescription().natureIds.length == 1
    }

    def "Project can be created without any natures"() {
        setup:
        workspaceOperations.deleteAllProjects(new NullProgressMonitor())
        def projectFolder = tempFolder.newFolder("sample-project-folder")

        when:
        IProject project = workspaceOperations.createProject("sample-project", projectFolder, ImmutableList.of(), ImmutableList.of(), null)

        then:
        project.getDescription().natureIds.length == 0
    }

    def "Project can be created with multiple natures"() {
        setup:
        workspaceOperations.deleteAllProjects(new NullProgressMonitor())
        def projectFolder = tempFolder.newFolder("sample-project-folder")

        when:
        IProject project = workspaceOperations.createProject("sample-project", projectFolder, ImmutableList.of(), ImmutableList.of("dummy-nature-1", "dummy-nature-2"), new NullProgressMonitor())

        then:
        project.getDescription().natureIds.length == 2
        project.getDescription().natureIds[0] == "dummy-nature-1"
        project.getDescription().natureIds[1] == "dummy-nature-2"

    }

    def "Importing nonexisting folder fails"() {
        when:
        workspaceOperations.createProject("projectname", new File("path-to-nonexisting-folder"), ImmutableList.of(), ImmutableList.of(), new NullProgressMonitor())

        then:
        thrown(IllegalArgumentException.class)
    }

    def "Importing a file is considered invalid"() {
        when:
        workspaceOperations.createProject("projectname", tempFolder.newFile("filename"), ImmutableList.of(), ImmutableList.of(), new NullProgressMonitor())

        then:
        thrown(IllegalArgumentException)
    }

    def "Importing project with name existing already in workspace fails"() {
        setup:
        def projectFolder = tempFolder.newFolder("projectname")

        when:
        workspaceOperations.createProject("projectname", projectFolder, ImmutableList.of(), ImmutableList.of(), new NullProgressMonitor())
        workspaceOperations.createProject("projectname", projectFolder, ImmutableList.of(), ImmutableList.of(), new NullProgressMonitor())

        then:
        thrown(GradlePluginsRuntimeException.class)
    }

    def "Project name can't be empty when created"() {
        setup:
        def projectFolder = tempFolder.newFolder("projectname")

        when:
        workspaceOperations.createProject("", projectFolder, ImmutableList.of(), ImmutableList.of(), new NullProgressMonitor())

        then:
        thrown(IllegalArgumentException)
    }

    def "Project name can't be null when created"() {
        setup:
        def projectFolder = tempFolder.newFolder("projectname")

        when:
        workspaceOperations.createProject(null, projectFolder, ImmutableList.of(), ImmutableList.of(), new NullProgressMonitor())

        then:
        thrown(NullPointerException)
    }

    ///////////////////////////////////
    // tests for deleteAllProjects() //
    ///////////////////////////////////

    def "Delete succeeds even the workspace is empty"() {
        when:
        workspaceOperations.deleteAllProjects(new NullProgressMonitor())

        then:
        workspaceOperations.allProjects.empty
    }

    def "A project can be deleted"() {
        setup:
        workspaceOperations.createProject("sample-project", tempFolder.newFolder(), ImmutableList.of(), ImmutableList.of(), new NullProgressMonitor())
        assert workspaceOperations.allProjects.size() == 1

        when:
        workspaceOperations.deleteAllProjects(new NullProgressMonitor())

        then:
        assert workspaceOperations.allProjects.size() == 0
    }

    def "Closed projects can be deleted"() {
        setup:
        IProject project = workspaceOperations.createProject("sample-project", tempFolder.newFolder(), ImmutableList.of(), ImmutableList.of(), new NullProgressMonitor())
        project.close(null)

        when:
        workspaceOperations.deleteAllProjects(new NullProgressMonitor())

        then:
        assert workspaceOperations.allProjects.size() == 0
    }

    /////////////////////////
    // tests for refresh() //
    /////////////////////////

    def "Refresh project calls for resource refresh"() {
        setup:
        IProject project = Mock(IProject)
        project.isAccessible() >> true

        when:
        workspaceOperations.refresh(project, new NullProgressMonitor())

        then:
        1 * project.refreshLocal(_, _)
    }

    def "Non-accessible cannot be refreshed"() {
        setup:
        IProject project = Mock(IProject)
        project.isAccessible() >> false

        when:
        workspaceOperations.refresh(project, new NullProgressMonitor())

        then:
        thrown(IllegalArgumentException)
    }

    def "Null project can't be refreshed"() {
        when:
        workspaceOperations.refresh(null, new NullProgressMonitor())

        then:
        thrown(NullPointerException)
    }

    ///////////////////////////////////
    // tests for createJavaProject() //
    ///////////////////////////////////

    def "A Java project can be created"() {
        setup:
        def rootFolder = tempFolder.newFolder()
        IProject project = workspaceOperations.createProject("sample-project", rootFolder, ImmutableList.of(), ImmutableList.of("dummy-project"), new NullProgressMonitor())
        def attributes = new FixedRequestAttributes(rootFolder, null, GradleDistribution.fromBuild(), null, ImmutableList.<String> of(), ImmutableList.<String> of())
        ProjectConfiguration projectConfiguration = ProjectConfiguration.from(attributes, com.gradleware.tooling.toolingmodel.Path.from(':'), rootFolder);
        CorePlugin.projectConfigurationManager().saveProjectConfiguration(projectConfiguration, project);

        when:
        ClasspathDefinition classpath = new ClasspathDefinition(ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), JavaRuntime.getDefaultJREContainerEntry().getPath())
        IJavaProject javaProject = workspaceOperations.createJavaProject(project, classpath, new NullProgressMonitor())

        then:
        javaProject != null
        javaProject.getProject() == project
    }

    def "A Java project can't be created from null project"() {
        setup:
        ClasspathDefinition classpath = new ClasspathDefinition(ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), JavaRuntime.getDefaultJREContainerEntry().getPath())

        when:
        workspaceOperations.createJavaProject(null, classpath, new NullProgressMonitor())

        then:
        thrown(NullPointerException)
    }

    def "A Java project can't be created from not accessible project"() {
        setup:
        IProject project = Mock(IProject)
        project.isAccessible() >> false
        ClasspathDefinition classpath = new ClasspathDefinition(ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), JavaRuntime.getDefaultJREContainerEntry().getPath())

        when:
        workspaceOperations.createJavaProject(project, classpath, new NullProgressMonitor())

        then:
        thrown(IllegalArgumentException)
    }

    def "A Java classpath can't be null"() {
        setup:
        IProject project = workspaceOperations.createProject("sample-project", tempFolder.newFolder(), ImmutableList.of(), ImmutableList.of("dummy-project"), new NullProgressMonitor())

        when:
        workspaceOperations.createJavaProject(project, null, new NullProgressMonitor())

        then:
        thrown(NullPointerException)
    }
}
