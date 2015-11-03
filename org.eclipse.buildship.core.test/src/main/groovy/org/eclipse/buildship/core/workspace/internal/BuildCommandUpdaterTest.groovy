package org.eclipse.buildship.core.workspace.internal

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import com.google.common.base.Optional

import com.gradleware.tooling.toolingmodel.OmniEclipseBuildCommand

import org.eclipse.core.resources.ICommand
import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.test.fixtures.EclipseProjects

class BuildCommandUpdaterTest extends Specification {

    @Rule
    TemporaryFolder tempFolder

    def cleanup() {
        CorePlugin.workspaceOperations().deleteAllProjects(new NullProgressMonitor())
    }

    def "Build command can be set on a project"() {
        given:
        def project = EclipseProjects.newProject('sample-project', tempFolder.newFolder())

        when:
        BuildCommandUpdater.update(project, buildCommand('customBuildCommand', ['key' : 'value']), new NullProgressMonitor())

        then:
        project.description.buildSpec.length == 1
        project.description.buildSpec[0].builderName == 'customBuildCommand'
        project.description.buildSpec[0].arguments == ['key' : 'value']
    }

    def "Build commands are removed if they no longer exist in the Gradle model"() {
        given:
        def project = EclipseProjects.newProject('sample-project', tempFolder.newFolder())

        when:
        BuildCommandUpdater.update(project, buildCommand('customBuildCommand', ['key' : 'value']), new NullProgressMonitor())
        BuildCommandUpdater.update(project, Optional.of([]), new NullProgressMonitor())

        then:
        project.description.buildSpec.length == 0
    }

    def "Manually added build commands are preserved"() {
        given:
        def project = EclipseProjects.newProject('sample-project', tempFolder.newFolder())
        def description = project.description
        def command = description.newCommand()
        command.setBuilderName('manuallyCreatedBuildCommand')
        command.setArguments([:])
        def commands = description.buildSpec + command
        description.setBuildSpec(commands as ICommand[])
        project.setDescription(description, new NullProgressMonitor())

        when:
        BuildCommandUpdater.update(project, Optional.of([]), new NullProgressMonitor())

        then:
        project.description.buildSpec.length == 1
        project.description.buildSpec[0].builderName == 'manuallyCreatedBuildCommand'
        project.description.buildSpec[0].arguments == [:]
    }

    def "Build commands that were previously defined manually are transformed to model elements"() {
        given:
        def project = EclipseProjects.newProject('sample-project', tempFolder.newFolder())
        def description = project.description
        def command = description.newCommand()
        command.setBuilderName('buildCommand')
        command.setArguments([:])
        def commands = description.buildSpec + command
        description.setBuildSpec(commands as ICommand[])
        project.setDescription(description, new NullProgressMonitor())

        when:
        BuildCommandUpdater.update(project, buildCommand('buildCommand'), new NullProgressMonitor())
        BuildCommandUpdater.update(project, Optional.of([]), new NullProgressMonitor())

        then:
        project.description.buildSpec == []
    }

    private def buildCommand(name, arguments = [:]) {
        def mockedBuildCommand = Mock(OmniEclipseBuildCommand)
        mockedBuildCommand.name >> name
        mockedBuildCommand.arguments >> arguments
        Optional.of([mockedBuildCommand])
    }
}
