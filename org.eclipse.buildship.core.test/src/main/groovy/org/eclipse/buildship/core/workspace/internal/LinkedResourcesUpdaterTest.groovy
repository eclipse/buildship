package org.eclipse.buildship.core.workspace.internal

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import com.gradleware.tooling.toolingmodel.OmniEclipseLinkedResource

import org.eclipse.core.resources.IFolder
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.CorePlugin

class LinkedResourcesUpdaterTest extends Specification {

    @Rule
    TemporaryFolder tempFolder

    def cleanup() {
        CorePlugin.workspaceOperations().deleteAllProjects(new NullProgressMonitor())
    }

    def "Can define a linked resource"() {
        given:
        File projectDir = tempFolder.newFolder('root', 'project-name').getCanonicalFile()
        File externalDir = tempFolder.newFolder('root', 'another').getCanonicalFile()
        IProject project = CorePlugin.workspaceOperations().createProject('project-name', projectDir, [], [], null)
        OmniEclipseLinkedResource linkedResource =  newFolderLinkedResource(externalDir.name, externalDir)

        when:
        LinkedResourcesUpdater.update(project, [linkedResource], new NullProgressMonitor())

        then:
        project.members().findAll { it.isLinked() }.size() == 1
        IFolder eclipseFolder = project.getFolder('another')
        eclipseFolder.exists()
        eclipseFolder.isLinked() == true
        eclipseFolder.location.toFile().equals(externalDir)
    }

    def "Defining a linked resource is idempotent" () {
        given:
        File projectDir = tempFolder.newFolder('root', 'project-name').getCanonicalFile()
        File externalDir = tempFolder.newFolder('root', 'another').getCanonicalFile()
        IProject project = CorePlugin.workspaceOperations().createProject('project-name', projectDir, [], [], null)
        OmniEclipseLinkedResource linkedResource =  newFolderLinkedResource(externalDir.name, externalDir)

        when:
        LinkedResourcesUpdater.update(project, [linkedResource], new NullProgressMonitor())
        linkedResource = newFolderLinkedResource(externalDir.name, externalDir)
        LinkedResourcesUpdater.update(project, [linkedResource], new NullProgressMonitor())

        then:
        project.members().findAll { it.isLinked() }.size() == 1
    }


    def "Only local folder linked resources are set on the project" () {
        given:
        File projectDir = tempFolder.newFolder('root', 'project-name').getCanonicalFile()
        File externalDir = tempFolder.newFolder('root', 'another').getCanonicalFile()
        File externalFile = tempFolder.newFile('file').getCanonicalFile()
        IProject project = CorePlugin.workspaceOperations().createProject('project-name', projectDir, [], [], null)
        OmniEclipseLinkedResource localFolder =  newFolderLinkedResource(externalDir.name, externalDir)
        OmniEclipseLinkedResource localFile =  newFileLinkedResource(externalFile.name, externalFile)
        OmniEclipseLinkedResource virtualResource =  newVirtualLinkedResource()

        when:
        LinkedResourcesUpdater.update(project, [localFile, localFolder, virtualResource], new NullProgressMonitor())

        then:
        project.members().findAll { it.isLinked() }.size() == 1
    }

    def "A folder can be linked even if a local folder already exists with the same name" () {
        given:
        File projectDir = tempFolder.newFolder('root', 'project-name').getCanonicalFile()
        IProject project = CorePlugin.workspaceOperations().createProject('project-name', projectDir, [], [], null)
        project.getFolder('foldername').create(true, true, null)
        File externalDir = tempFolder.newFolder('root', 'foldername').getCanonicalFile()
        OmniEclipseLinkedResource linkedResource =  newFolderLinkedResource(externalDir.name, externalDir)

        when:
        LinkedResourcesUpdater.update(project, [linkedResource], new NullProgressMonitor())

        then:
        project.members().findAll { it.isLinked() }.size() == 1
        def IFolder linkedFolder = project.members().find { it.isLinked() }
        linkedFolder.getName().contains('foldername')
        !linkedFolder.getName().equals('foldername')
    }

    def "A linked resource is deleted if no longer part of the Gradle model"() {
        given:
        File projectDir = tempFolder.newFolder('root', 'project-name').getCanonicalFile()
        File externalDirA = tempFolder.newFolder('root', 'another1').getCanonicalFile()
        File externalDirB = tempFolder.newFolder('root', 'another2').getCanonicalFile()
        IProject project = CorePlugin.workspaceOperations().createProject('project-name', projectDir, [], [], null)
        OmniEclipseLinkedResource linkedResourceA =  newFolderLinkedResource(externalDirB.name, externalDirB)
        OmniEclipseLinkedResource linkedResourceB =  newFolderLinkedResource(externalDirB.name, externalDirB)

        when:
        LinkedResourcesUpdater.update(project, [linkedResourceA], new NullProgressMonitor())
        LinkedResourcesUpdater.update(project, [linkedResourceB], new NullProgressMonitor())

        then:
        project.members().findAll { it.isLinked() }.size() == 1
        !project.getFolder('another1').exists()
        IFolder eclipseFolder = project.getFolder('another2')
        eclipseFolder.exists()
        eclipseFolder.isLinked() == true
        eclipseFolder.location.toFile().equals(externalDirB)
    }

    private def newFolderLinkedResource(String name, File location) {
        OmniEclipseLinkedResource linkedResource = Mock()
        linkedResource.name >> name
        linkedResource.type >> '2'
        linkedResource.location >> location.path
        linkedResource.locationUri >> null
        linkedResource
    }

    private def newVirtualLinkedResource() {
        OmniEclipseLinkedResource linkedResource = Mock()
        linkedResource.name >> 'example'
        linkedResource.type >> '1'
        linkedResource.location >> null
        linkedResource.locationUri >> 'http://example.com'
        linkedResource
    }

    private def newFileLinkedResource(String name, File file) {
        assert file.isFile()
        OmniEclipseLinkedResource linkedResource = Mock()
        linkedResource.name >> name
        linkedResource.type >> '1'
        linkedResource.location >> file.path
        linkedResource.locationUri >> null
        linkedResource
    }

}
