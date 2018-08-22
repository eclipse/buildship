package org.eclipse.buildship.core.internal.preferences.impl

import org.eclipse.core.resources.ICommand
import org.eclipse.core.resources.IProject

import org.eclipse.buildship.core.internal.test.fixtures.WorkspaceSpecification

class BuildCommandConverterTest extends WorkspaceSpecification {

    private IProject project

    def setup() {
        project = newProject('sample-project')
    }

    def "can save and load zero commands"() {
        setup:
        List<ICommand> commands = []
        String xml = BuildCommandConverter.toXml(project, commands)

        expect:
        commands == BuildCommandConverter.toEntries(project, xml)
    }

    def "can save and load command without arguments"() {
        setup:
        List<ICommand> commands = [command('name')]
        String xml = BuildCommandConverter.toXml(project, commands)

        expect:
        commands == BuildCommandConverter.toEntries(project, xml)
    }

    def "can save and load command with arguments"() {
        setup:
        List<ICommand> commands = [command('name', 'arg1key', 'arg1value', 'arg2key', 'arg2value')]
        String xml = BuildCommandConverter.toXml(project, commands)

        expect:
        commands == BuildCommandConverter.toEntries(project, xml)
    }

    def "can save and load multiple commands"() {
        setup:
        List<ICommand> commands = [command('name', 'arg1key', 'arg1value', 'arg2key', 'arg2value'), command('another')]
        String xml = BuildCommandConverter.toXml(project, commands)

        expect:
        commands == BuildCommandConverter.toEntries(project, xml)
    }

    private ICommand command(String name, k1 = null, v1 = null, k2 = null, v2 = null) {
        ICommand command = project.description.newCommand()
        command.builderName = name
        if (k1 != null && v1 != null && k2 != null && v2 != null) {
             command.arguments = [k1 : v1, k2 : v2]
        }
        command
    }
}
