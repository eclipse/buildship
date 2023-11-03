/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace

import org.gradle.tooling.model.eclipse.EclipseBuildCommand

import org.eclipse.core.resources.ICommand
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.internal.configuration.GradleProjectBuilder
import org.eclipse.buildship.core.internal.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.internal.workspace.BuildCommandUpdater
import org.eclipse.buildship.core.internal.workspace.PersistentModelBuilder

class BuildCommandUpdaterTest extends WorkspaceSpecification {

    def "Build command can be set on a project"() {
        given:
        def project = newProject('sample-project')

        when:
        BuildCommandUpdater.update(project, oneBuildCommand('customBuildCommand', ['key' : 'value']), persistentModelBuilder(project), new NullProgressMonitor())

        then:
        hasBuildCommand(project, 'customBuildCommand', ['key' : 'value'])
    }

    def "Gradle builder is added when nature information is absent"() {
        given:
        def project = newProject('sample-project')

        when:
        BuildCommandUpdater.update(project, unsupportedBuildCommands(), persistentModelBuilder(project), new NullProgressMonitor())

        then:
        hasBuildCommand(project, GradleProjectBuilder.ID)
    }

    def "Gradle builder is added when nature information is present"() {
        given:
        def project = newProject('sample-project')

        when:
        BuildCommandUpdater.update(project, zeroBuildCommands(), persistentModelBuilder(project), new NullProgressMonitor())

        then:
        hasBuildCommand(project, GradleProjectBuilder.ID)
    }

    def "Gradle builder is added only once"() {
        given:
        def project = newProject('sample-project')

        when:
        BuildCommandUpdater.update(project, oneBuildCommand(GradleProjectBuilder.ID), persistentModelBuilder(project), new NullProgressMonitor())

        then:
        project.description.buildSpec.length == 1
        hasBuildCommand(project, GradleProjectBuilder.ID)
    }

    def "Can have two build commands with same id and different arguments"() {
        given:
        def project = newProject('sample-project')

        when:
        def buildCommands = twoBuildCommands('customBuildCommand', ['key' : 'value'], 'customBuildCommand', ['key' : 'otherValue'])
        BuildCommandUpdater.update(project, buildCommands, persistentModelBuilder(project), new NullProgressMonitor())

        then:
        hasBuildCommand(project, 'customBuildCommand', ['key' : 'value'])
        hasBuildCommand(project, 'customBuildCommand', ['key' : 'otherValue'])
    }

    def "Build commands are removed if they were added by Gradle and no longer exist in the model"() {
        given:
        def project = newProject('sample-project')
        PersistentModelBuilder persistentModel = persistentModelBuilder(project)

        when:
        BuildCommandUpdater.update(project, oneBuildCommand('customBuildCommand', ['key' : 'value']), persistentModel, new NullProgressMonitor())

        then:
        hasBuildCommand(project, 'customBuildCommand', ['key' : 'value'])

        when:
        BuildCommandUpdater.update(project, zeroBuildCommands(), persistentModelBuilder(persistentModel.build()), new NullProgressMonitor())

        then:
        !hasBuildCommand(project, 'customBuildCommand', ['key' : 'value'])
    }

    def "Manually added build commands are preserved when Gradle model does not provide them"() {
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
        BuildCommandUpdater.update(project, unsupportedBuildCommands(), persistentModelBuilder(project), new NullProgressMonitor())

        then:
        hasBuildCommand(project, 'manuallyCreatedBuildCommand')
    }

    def "Manually added natures are preserved if they were added manually"() {
        given:
        def project = newProject('sample-project')
        def description = project.description
        def command = description.newCommand()
        command.setBuilderName('manuallyCreatedBuildCommand')
        command.setArguments([:])
        def commands = description.buildSpec + command
        description.setBuildSpec(commands as ICommand[])
        project.setDescription(description, new NullProgressMonitor())
        PersistentModelBuilder persistentModel = persistentModelBuilder(project)

        when:
        BuildCommandUpdater.update(project, zeroBuildCommands(), persistentModel, new NullProgressMonitor())

        then:
        hasBuildCommand(project, 'manuallyCreatedBuildCommand')

        when:
        persistentModel = persistentModelBuilder(persistentModel.build())
        BuildCommandUpdater.update(project, oneBuildCommand('manuallyCreatedBuildCommand'), persistentModel, new NullProgressMonitor())

        then:
        hasBuildCommand(project, 'manuallyCreatedBuildCommand')

        when:
        persistentModel = persistentModelBuilder(persistentModel.build())
        BuildCommandUpdater.update(project, zeroBuildCommands(), persistentModel, new NullProgressMonitor())

        then:
        hasBuildCommand(project, 'manuallyCreatedBuildCommand')
    }

    private List unsupportedBuildCommands() {
        []
    }

    private List zeroBuildCommands() {
        []
    }

    private List oneBuildCommand(name, arguments = [:]) {
        [buildCommand(name, arguments)]
    }

    private List twoBuildCommands(name1, arguments1, name2, arguments2) {
        [buildCommand(name1, arguments1), buildCommand(name2, arguments2)]
    }

    private EclipseBuildCommand buildCommand(name, arguments = [:]) {
        EclipseBuildCommand mockedBuildCommand = Mock(EclipseBuildCommand)
        mockedBuildCommand.name >> name
        mockedBuildCommand.arguments >> arguments
        mockedBuildCommand
    }

    private Boolean hasBuildCommand(IProject project, String builderName, Map arguments = [:]) {
        project.description.buildSpec.find { it.builderName == builderName && it.arguments == arguments } != null
    }

}
