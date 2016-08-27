package org.eclipse.buildship.core.workspace.internal

import com.google.common.base.Optional

import com.gradleware.tooling.toolingmodel.OmniEclipseBuildCommand

import org.eclipse.core.resources.ICommand
import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification

class BuildCommandUpdaterTest extends WorkspaceSpecification {

    def "Build command can be set on a project"() {
        given:
        def project = newProject('sample-project')

        when:
        BuildCommandUpdater.update(project, Optional.of([buildCommand('customBuildCommand', ['key' : 'value'])]), new NullProgressMonitor())

        then:
        project.description.buildSpec.length == 1
        project.description.buildSpec[0].builderName == 'customBuildCommand'
        project.description.buildSpec[0].arguments == ['key' : 'value']
    }

    def "Can have two build commands with same id and different arguments"() {
        given:
        def project = newProject('sample-project')

        when:
        BuildCommandUpdater.update(
            project,
            Optional.of(
                [
                    buildCommand('customBuildCommand', ['key' : 'value']),
                    buildCommand('customBuildCommand', ['key' : 'otherValue']),
                ]
            ),
            new NullProgressMonitor()
        )

        then:
        project.description.buildSpec.length == 2
        project.description.buildSpec[0].builderName == 'customBuildCommand'
        project.description.buildSpec[0].arguments == ['key' : 'value']
        project.description.buildSpec[0].builderName == 'customBuildCommand'
        project.description.buildSpec[1].arguments == ['key' : 'otherValue']
    }

    def "Build commands are removed if they no longer exist in the Gradle model"() {
        given:
        def project = newProject('sample-project')

        when:
        BuildCommandUpdater.update(project, Optional.of([buildCommand('customBuildCommand', ['key' : 'value'])]), new NullProgressMonitor())
        BuildCommandUpdater.update(project, Optional.of([]), new NullProgressMonitor())

        then:
        project.description.buildSpec.length == 0
    }

    def "Manually added build commands are preserved if Gradle model does not provide them"() {
        given:
        def project = newProject('sample-project')
        def description = project.description
        def command = description.newCommand()
        command.setBuilderName('manuallyCreatedBuildCommand')
        command.setArguments([:])
        def commands = description.buildSpec + command
        description.setBuildSpec(commands as ICommand[])
        project.setDescription(description, new NullProgressMonitor())

        when:
        BuildCommandUpdater.update(project, Optional.absent(), new NullProgressMonitor())

        then:
        project.description.buildSpec.length == 1
        project.description.buildSpec[0].builderName == 'manuallyCreatedBuildCommand'
        project.description.buildSpec[0].arguments == [:]
    }

    def "Manually added build commands are removed if Gradle model provides them"() {
        given:
        def project = newProject('sample-project')
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
        project.description.buildSpec.length == 0
    }

    private def buildCommand(name, arguments = [:]) {
        def mockedBuildCommand = Mock(OmniEclipseBuildCommand)
        mockedBuildCommand.name >> name
        mockedBuildCommand.arguments >> arguments
        mockedBuildCommand
    }

}
