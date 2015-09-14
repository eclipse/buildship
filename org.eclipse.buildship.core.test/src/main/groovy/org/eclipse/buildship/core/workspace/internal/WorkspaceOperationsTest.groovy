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

package org.eclipse.buildship.core.workspace.internal

import com.google.common.collect.ImmutableList
import org.eclipse.buildship.core.workspace.WorkspaceOperations
import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.Path
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes
import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.GradlePluginsRuntimeException
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.configuration.ProjectConfiguration
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.launching.JavaRuntime
import org.junit.Rule
import org.junit.rules.TemporaryFolder

import spock.lang.Shared
import spock.lang.Specification

class WorkspaceOperationsTest extends Specification {

    @Shared
    WorkspaceOperations workspaceOperations = CorePlugin.workspaceOperations()

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
        IProject project = workspaceOperations.createProject("sample-project", projectFolder, ImmutableList.of(), ImmutableList.of(GradleProjectNature.ID), null)

        then:
        project.exists()
        project.isAccessible()
        project.hasNature(GradleProjectNature.ID)
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
        IProject project = workspaceOperations.createProject("sample-project", projectFolder, ImmutableList.of(), ImmutableList.of(JavaCore.NATURE_ID, GradleProjectNature.ID), new NullProgressMonitor())

        then:
        project.getDescription().natureIds.length == 2
        project.getDescription().natureIds[0] == JavaCore.NATURE_ID
        project.getDescription().natureIds[1] == GradleProjectNature.ID

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
        workspaceOperations.refreshProject(project, new NullProgressMonitor())

        then:
        1 * project.refreshLocal(_, _)
    }

    def "Non-accessible cannot be refreshed"() {
        setup:
        IProject project = Mock(IProject)
        project.isAccessible() >> false

        when:
        workspaceOperations.refreshProject(project, new NullProgressMonitor())

        then:
        thrown(IllegalArgumentException)
    }

    def "Null project can't be refreshed"() {
        when:
        workspaceOperations.refreshProject(null, new NullProgressMonitor())

        then:
        thrown(NullPointerException)
    }

    ///////////////////////////////////
    // tests for createJavaProject() //
    ///////////////////////////////////

    def "Java project can be created"() {
        setup:
        def rootFolder = tempFolder.newFolder()
        IProject project = workspaceOperations.createProject("sample-project", rootFolder, ImmutableList.of(), ImmutableList.of(), new NullProgressMonitor())
        def attributes = new FixedRequestAttributes(rootFolder, null, GradleDistribution.fromBuild(), null, ImmutableList.<String> of(), ImmutableList.<String> of())
        ProjectConfiguration projectConfiguration = ProjectConfiguration.from(attributes, Path.from(':'), rootFolder)
        CorePlugin.projectConfigurationManager().saveProjectConfiguration(projectConfiguration, project)
        def jrePath = JavaRuntime.getDefaultJREContainerEntry().getPath()

        when:
        IJavaProject javaProject = workspaceOperations.createJavaProject(project, jrePath, new NullProgressMonitor())

        then:
        javaProject != null
        javaProject.getProject() == project
    }

    def "Java project can't be created from null project"() {
        setup:
        def jrePath = JavaRuntime.getDefaultJREContainerEntry().getPath()

        when:
        workspaceOperations.createJavaProject(null, jrePath, new NullProgressMonitor())

        then:
        thrown(NullPointerException)
    }

    def "Java project can't be created from not accessible project"() {
        setup:
        IProject project = Mock(IProject)
        project.isAccessible() >> false
        def jrePath = JavaRuntime.getDefaultJREContainerEntry().getPath()

        when:
        workspaceOperations.createJavaProject(project, jrePath, new NullProgressMonitor())

        then:
        thrown(IllegalArgumentException)
    }

    def "Java project can't be created without JRE path"() {
        setup:
        IProject project = workspaceOperations.createProject("sample-project", tempFolder.newFolder(), ImmutableList.of(), ImmutableList.of(JavaCore.NATURE_ID), new NullProgressMonitor())

        when:
        workspaceOperations.createJavaProject(project, null, new NullProgressMonitor())

        then:
        thrown(NullPointerException)
    }

}
