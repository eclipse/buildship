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
import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.Path
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes
import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.GradlePluginsRuntimeException
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.configuration.ProjectConfiguration
import org.eclipse.buildship.core.test.fixtures.EclipseProjects;
import org.eclipse.buildship.core.test.fixtures.LegacyEclipseSpockTestHelper
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification;
import org.eclipse.buildship.core.workspace.GradleClasspathContainer
import org.eclipse.buildship.core.workspace.WorkspaceOperations

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IProjectDescription
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.launching.JavaRuntime
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

class WorkspaceOperationsTest extends WorkspaceSpecification {

    @Shared
    WorkspaceOperations workspaceOperations = CorePlugin.workspaceOperations()

    def "can create a new project"() {
        setup:
        deleteAllProjects(false)
        def projectFolder = dir("sample-project-folder")

        when:
        IProject project = workspaceOperations.createProject("sample-project", projectFolder, ImmutableList.of(GradleProjectNature.ID), null)

        then:
        project.exists()
        project.isAccessible()
        project.hasNature(GradleProjectNature.ID)
        project.getDescription().natureIds.length == 1
    }

    def "Project can be created without any natures"() {
        setup:
        deleteAllProjects(false)
        def projectFolder = dir("sample-project-folder")

        when:
        IProject project = workspaceOperations.createProject("sample-project", projectFolder, ImmutableList.of(), null)

        then:
        project.getDescription().natureIds.length == 0
    }

    def "Project can be created with multiple natures"() {
        setup:
        deleteAllProjects(false)
        def projectFolder = dir("sample-project-folder")

        when:
        IProject project = workspaceOperations.createProject("sample-project", projectFolder, ImmutableList.of(JavaCore.NATURE_ID, GradleProjectNature.ID), new NullProgressMonitor())

        then:
        project.getDescription().natureIds.length == 2
        project.getDescription().natureIds[0] == JavaCore.NATURE_ID
        project.getDescription().natureIds[1] == GradleProjectNature.ID

    }

    def "Importing nonexisting folder fails"() {
        when:
        workspaceOperations.createProject("projectname", new File("path-to-nonexisting-folder"), ImmutableList.of(), new NullProgressMonitor())

        then:
        thrown(IllegalArgumentException.class)
    }

    def "Importing a file is considered invalid"() {
        when:
        workspaceOperations.createProject("projectname", file("filename"), ImmutableList.of(), new NullProgressMonitor())

        then:
        thrown(IllegalArgumentException)
    }

    def "Importing project with name existing already in workspace fails"() {
        setup:
        def projectFolder = dir("projectname")

        when:
        workspaceOperations.createProject("projectname", projectFolder, ImmutableList.of(), new NullProgressMonitor())
        workspaceOperations.createProject("projectname", projectFolder, ImmutableList.of(), new NullProgressMonitor())

        then:
        thrown(GradlePluginsRuntimeException.class)
    }

    def "Project name can't be empty when created"() {
        setup:
        def projectFolder = dir("projectname")

        when:
        workspaceOperations.createProject("", projectFolder, ImmutableList.of(), new NullProgressMonitor())

        then:
        thrown(IllegalArgumentException)
    }

    def "Project name can't be null when created"() {
        setup:
        def projectFolder = dir("projectname")

        when:
        workspaceOperations.createProject(null, projectFolder, ImmutableList.of(), new NullProgressMonitor())

        then:
        thrown(NullPointerException)
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
        def rootFolder = testDir
        IProject project = workspaceOperations.createProject("sample-project", rootFolder, ImmutableList.of(), new NullProgressMonitor())
        def attributes = new FixedRequestAttributes(rootFolder, null, GradleDistribution.fromBuild(), null, ImmutableList.<String> of(), ImmutableList.<String> of())
        ProjectConfiguration projectConfiguration = ProjectConfiguration.from(attributes, Path.from(':'))
        CorePlugin.projectConfigurationManager().saveProjectConfiguration(projectConfiguration, project)
        def jrePath = JavaRuntime.getDefaultJREContainerEntry().getPath()

        when:
        IJavaProject javaProject = workspaceOperations.createJavaProject(project, jrePath, GradleClasspathContainer.newClasspathEntry(), new NullProgressMonitor())

        then:
        javaProject != null
        javaProject.getProject() == project
    }

    def "Java project can't be created from null project"() {
        setup:
        def jrePath = JavaRuntime.getDefaultJREContainerEntry().getPath()

        when:
        workspaceOperations.createJavaProject(null, jrePath, GradleClasspathContainer.newClasspathEntry(), new NullProgressMonitor())

        then:
        thrown(NullPointerException)
    }

    def "Java project can't be created from not accessible project"() {
        setup:
        IProject project = Mock(IProject)
        project.isAccessible() >> false
        def jrePath = JavaRuntime.getDefaultJREContainerEntry().getPath()

        when:
        workspaceOperations.createJavaProject(project, jrePath, GradleClasspathContainer.newClasspathEntry(), new NullProgressMonitor())

        then:
        thrown(IllegalArgumentException)
    }

    def "Java project can't be created without JRE path"() {
        setup:
        IProject project = workspaceOperations.createProject("sample-project", testDir, ImmutableList.of(JavaCore.NATURE_ID), new NullProgressMonitor())

        when:
        workspaceOperations.createJavaProject(project, null, GradleClasspathContainer.newClasspathEntry(), new NullProgressMonitor())

        then:
        thrown(NullPointerException)
    }

    //////////////////////////////////////////////
    // tests for addNature() and removeNature() //
    //////////////////////////////////////////////

    def "Can add a nature to a project"() {
        setup:
        IProject sampleProject = createSampleProject()

        when:
        workspaceOperations.addNature(sampleProject, GradleProjectNature.ID, new NullProgressMonitor())

        then:
        sampleProject.hasNature(GradleProjectNature.ID)
    }

    def "Adding a project nature is idempotent"() {
        setup:
        IProject sampleProject = createSampleProject()

        when:
        workspaceOperations.addNature(sampleProject, GradleProjectNature.ID, new NullProgressMonitor())
        workspaceOperations.addNature(sampleProject, GradleProjectNature.ID, new NullProgressMonitor())

        then:
        sampleProject.description.natureIds.findAll{ it == GradleProjectNature.ID }.size() == 1
    }

    def "Assigning a nature to a non-accessible project results in a runtime exception"() {
        setup:
        IProject sampleProject = createSampleProject()
        sampleProject.close(null)

        when:
        workspaceOperations.addNature(sampleProject, GradleProjectNature.ID, new NullProgressMonitor())

        then:
        thrown(GradlePluginsRuntimeException)
    }

    def "Adding a project nature not defined by Eclipse is ignored"() {
        setup:
        IProject project = createSampleProject()

        when:
        workspaceOperations.addNature(project, "nonexisting.project.nature", new NullProgressMonitor())

        then:
        notThrown(Throwable)
        project.description.natureIds == []
    }

    def "Can remove a nature from a project"() {
        setup:
        IProject sampleProject = createSampleProject()
        workspaceOperations.addNature(sampleProject, JavaCore.NATURE_ID, new NullProgressMonitor())
        workspaceOperations.addNature(sampleProject, GradleProjectNature.ID, new NullProgressMonitor())

        expect:
        sampleProject.description.natureIds.length == 2

        when:
        workspaceOperations.removeNature(sampleProject, GradleProjectNature.ID, new NullProgressMonitor())

        then:
        sampleProject.description.natureIds.length == 1
        sampleProject.description.natureIds[0] == JavaCore.NATURE_ID
    }

    def "Removing a nature which was not present on a project changes nothing"() {
        setup:
        IProject sampleProject = createSampleProject()
        String[] originalNatureIds = sampleProject.description.natureIds

        when:
        workspaceOperations.removeNature(sampleProject, GradleProjectNature.ID, new NullProgressMonitor())

        then:
        !sampleProject.description.natureIds.is(originalNatureIds)
        sampleProject.description.natureIds == originalNatureIds
    }

    def "Removing a nature from a non-accessible project results in a runtime exception"() {
        setup:
        IProject sampleProject = createSampleProject()
        sampleProject.close(null)

        when:
        workspaceOperations.removeNature(sampleProject, GradleProjectNature.ID, new NullProgressMonitor())

        then:
        thrown(GradlePluginsRuntimeException)
    }

    //////////////////////////////////////////////////////////
    // tests for addBuildCommand() and removeBuildCommand() //
    //////////////////////////////////////////////////////////

    def "Can assign a build command to a project"() {
        setup:
        IProject project = createSampleProject()

        when:
        workspaceOperations.addBuildCommand(project, 'buildCommand', ['builderArgKey' : 'builderArgValue'], new NullProgressMonitor())

        then:
        project.description.buildSpec.length == 1
        project.description.buildSpec[0].builderName == 'buildCommand'
        project.description.buildSpec[0].arguments.keySet().size() == 1
        project.description.buildSpec[0].arguments['builderArgKey'] == 'builderArgValue'
    }

    def "Assigning a build command to a project is idempotent"() {
        setup:
        IProject project = createSampleProject()

        when:
        workspaceOperations.addBuildCommand(project, 'buildCommand', ['builderArgKey' : 'builderArgValue'], new NullProgressMonitor())
        workspaceOperations.addBuildCommand(project, 'buildCommand', ['builderArgKey' : 'builderArgValue'], new NullProgressMonitor())

        then:
        project.description.buildSpec.length == 1
        project.description.buildSpec[0].builderName == 'buildCommand'
        project.description.buildSpec[0].arguments.keySet().size() == 1
        project.description.buildSpec[0].arguments['builderArgKey'] == 'builderArgValue'
    }

    def "Assigning a build command to a non-accessible project results in a runtime exception"() {
        setup:
        IProject project = createSampleProject()
        project.close()

        when:
        workspaceOperations.addBuildCommand(project, 'buildCommand', ['builderArgKey' : 'builderArgValue'], new NullProgressMonitor())

        then:
        thrown(RuntimeException)
    }

    def "Assigning an existing build command to a project with different arguments updates the build command"() {
        setup:
        IProject project = createSampleProject()

        when:
        workspaceOperations.addBuildCommand(project, 'buildCommand', ['builderArgKey' : 'builderArgValue'], new NullProgressMonitor())
        workspaceOperations.addBuildCommand(project, 'buildCommand', ['updatedArgKey' : 'updatedArgValue'], new NullProgressMonitor())

        then:
        project.description.buildSpec.length == 1
        project.description.buildSpec[0].builderName == 'buildCommand'
        project.description.buildSpec[0].arguments.keySet().size() == 1
        project.description.buildSpec[0].arguments['updatedArgKey'] == 'updatedArgValue'
    }

    def "Can remove a build command from a project"() {
        setup:
        IProject project = createSampleProject()

        when:
        workspaceOperations.addBuildCommand(project, 'buildCommand', ['builderArgKey' : 'builderArgValue'], new NullProgressMonitor())
        workspaceOperations.removeBuildCommand(project, 'buildCommand', new NullProgressMonitor())

        then:
        project.description.buildSpec.length == 0
    }

    def "Removing a non-exsting build command from a project doesn't do anything"() {
        setup:
        IProject project = createSampleProject()

        when:
        workspaceOperations.removeBuildCommand(project, 'buildCommand', new NullProgressMonitor())

        then:
        project.description.buildSpec.length == 0
    }

    def "Removing a build command from a non-accessible project results in a runtime exception"() {
        setup:
        IProject project = createSampleProject()
        project.close()

        when:
        workspaceOperations.removeBuildCommand(project, 'buildCommand', new NullProgressMonitor())

        then:
        thrown(RuntimeException)
    }

    def "A marked build folder can be identified"() {
        setup:
        def project = createSampleProject()
        def build = project.getFolder("build")
        build.create(true, true, null)

        when:
        workspaceOperations.markAsBuildFolder(build)

        then:
        workspaceOperations.isBuildFolder(build)
    }

    def "Projects in the workspace root get their default name"() {
        expect:
        workspaceOperations.normalizeProjectName("foo", workspaceDir("bar")) == "bar"
    }

    def "Projects in an external location can have custom names"() {
        expect:
        workspaceOperations.normalizeProjectName("foo", dir("bar")) == "foo"
    }

    def "A project name cannot be calculated with null arguments"() {
        when:
        workspaceOperations.normalizeProjectName("foo", null)

        then:
        thrown NullPointerException

        when:
        workspaceOperations.normalizeProjectName(null, dir("bar"))

        then:
        thrown NullPointerException
    }

    def "Can rename a project"() {
        setup:
        IProject sampleProject = createSampleProject()

        expect:
        CorePlugin.workspaceOperations().allProjects.size() == 1
        CorePlugin.workspaceOperations().findProjectByName('sample-project').isPresent()

        when:
        workspaceOperations.renameProject(sampleProject, 'new-name', new NullProgressMonitor())

        then:
        CorePlugin.workspaceOperations().allProjects.size() == 1
        CorePlugin.workspaceOperations().findProjectByName('new-name').isPresent()
    }

    def "Renaming fails with RuntimeException if project is in the default location"() {
        setup:
        IProject sampleProject = createSampleProjectInWorkspace()

        expect:
        CorePlugin.workspaceOperations().allProjects.size() == 1
        CorePlugin.workspaceOperations().findProjectByName('sample-project').isPresent()

        when:
        workspaceOperations.renameProject(sampleProject, 'new-name', new NullProgressMonitor())

        then:
        thrown(GradlePluginsRuntimeException)
    }

    def "Renaming fails with RuntimeException if target name is already taken" () {
        setup:
        IProject projectA = newProject('project-a')
        IProject projectB = newProject('project-b')

        when:
        workspaceOperations.renameProject(projectA, 'project-b', new NullProgressMonitor())

        then:
        thrown(GradlePluginsRuntimeException)
    }

    private IProject createSampleProject() {
        newProject("sample-project")
    }

    private IProject createSampleProjectInWorkspace() {
        EclipseProjects.newProject("sample-project")
    }
}
