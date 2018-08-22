package org.eclipse.buildship.core.internal.workspace.impl

import org.gradle.tooling.model.eclipse.EclipseLinkedResource

import org.eclipse.core.resources.IFolder
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path

import org.eclipse.buildship.core.internal.test.fixtures.WorkspaceSpecification

class LinkedResourcesUpdaterTest extends WorkspaceSpecification {

    def "Can define a linked resource"() {
        given:
        File externalDir = dir('another')
        IProject project = newProject('project-name')
        EclipseLinkedResource linkedResource =  newFolderLinkedResource(externalDir.name, externalDir)
        PersistentModelBuilder persistentModel = persistentModelBuilder(project)

        when:
        LinkedResourcesUpdater.update(project, [linkedResource], persistentModel, new NullProgressMonitor())
        Collection<IResource> linkedresources = linkedResources(project)

        then:
        linkedresources.size() == 1
        linkedresources[0].name == 'another'
        linkedresources[0].exists()
        linkedresources[0].location.toFile().equals(externalDir)
    }

    def "Can define a linked resource even if the resource does not exist"() {
        given:
            File externalDir = getDir('another')
            IProject project = newProject('project-name')
            EclipseLinkedResource linkedResource =  newFolderLinkedResource(externalDir.name, externalDir)
            PersistentModelBuilder persistentModel = persistentModelBuilder(project)

            when:
            LinkedResourcesUpdater.update(project, [linkedResource], persistentModel, new NullProgressMonitor())
            Collection<IResource> linkedResources = linkedResources(project)

            then:
            linkedResources.size() == 1
            linkedResources[0].name == 'another'
            linkedResources[0].exists()
            linkedResources[0].location.toFile().equals(externalDir)
    }

    def "Defining a linked resource is idempotent"() {
        given:
        File externalDir = dir('another')
        IProject project = newProject('project-name')
        EclipseLinkedResource linkedResource =  newFolderLinkedResource(linkName, externalDir)
        PersistentModelBuilder persistentModel = persistentModelBuilder(project)
        LinkedResourcesUpdater.update(project, [linkedResource], persistentModel, new NullProgressMonitor())
        linkedResource = newFolderLinkedResource(linkName, externalDir)

        persistentModel = persistentModelBuilder(persistentModel.build())

        when:
        LinkedResourcesUpdater.update(project, [linkedResource], persistentModel, new NullProgressMonitor())
        linkedResource = newFolderLinkedResource(linkName, externalDir)

        persistentModel = persistentModelBuilder(persistentModel.build())

        LinkedResourcesUpdater.update(project, [linkedResource], persistentModel, new NullProgressMonitor())
        Collection<IResource> linkedResources = linkedResources(project)

        then:
        linkedResources.size() == 1
        linkedResources[0].name == expectedFolderName
        linkedResources[0].fullPath.toPortableString() == expectedFolderPath

        where:
        linkName  | expectedFolderName | expectedFolderPath
        'another' | 'another'          | '/project-name/another'
        'a/b/c'   | 'c'                | '/project-name/a/b/c'
    }

    def "Only local linked resources are created" () {
        given:
        File externalDir = dir('another')
        File externalFile = file('file')
        IProject project = newProject('project-name')
        EclipseLinkedResource localFolder =  newFolderLinkedResource(externalDir.name, externalDir)
        EclipseLinkedResource localFile =  newFileLinkedResource(externalFile.name, externalFile)
        EclipseLinkedResource virtualResource =  newVirtualLinkedResource()
        PersistentModelBuilder persistentModel = persistentModelBuilder(project)

        when:
        LinkedResourcesUpdater.update(project, [localFile, localFolder, virtualResource], persistentModel, new NullProgressMonitor())

        then:
        linkedResources(project).size() == 2
    }

    def "If a linked resource name matches to an existing folder, then the folder is replaced" () {
        given:
        IProject project = newProject('project-name')
        project.getFolder('foldername').create(true, true, null)
        File externalDir = dir('foldername')
        EclipseLinkedResource linkedResource =  newFolderLinkedResource(externalDir.name, externalDir)
        PersistentModelBuilder persistentModel = persistentModelBuilder(project)

        when:
        LinkedResourcesUpdater.update(project, [linkedResource], persistentModel, new NullProgressMonitor())
        Collection<IResource> linkedResources = linkedResources(project)

        then:
        linkedResources.size() == 1
        linkedResources[0].name == 'foldername'
    }

    def "A linked resource is deleted if no longer part of the Gradle model"() {
        given:
        File externalDirA = dir('another1')
        File externalDirB = dir('another2')
        IProject project = newProject('project-name')
        EclipseLinkedResource linkedResourceA =  newFolderLinkedResource(linkName, externalDirA)
        EclipseLinkedResource linkedResourceB =  newFolderLinkedResource(externalDirB.name, externalDirB)
        PersistentModelBuilder persistentModel = persistentModelBuilder(project)
        LinkedResourcesUpdater.update(project, [linkedResourceA], persistentModel, new NullProgressMonitor())

        persistentModel = persistentModelBuilder(persistentModel.build())

        when:
        LinkedResourcesUpdater.update(project, [linkedResourceB], persistentModel, new NullProgressMonitor())
        Collection<IResource> linkedResources = linkedResources(project)

        then:
        !project.getFolder(linkName).exists()
        linkedResources.size() == 1
        linkedResources[0].name == 'another2'
        linkedResources[0].exists()
        linkedResources[0].location.toFile().equals(externalDirB)

        where:
        linkName << ['another', 'a/b/c']
    }

    def "Only linked resources are removed from the project"() {
        given:
        File externalDirA = dir('another1')
        File externalDirB = dir('another2')
        IProject project = newProject('project-name')
        EclipseLinkedResource linkedResourceA =  newFolderLinkedResource(externalDirA.name, externalDirA)
        EclipseLinkedResource linkedResourceB =  newFolderLinkedResource(externalDirB.name, externalDirB)
        PersistentModelBuilder persistentModel = persistentModelBuilder(project)
        LinkedResourcesUpdater.update(project, [linkedResourceA], persistentModel, new NullProgressMonitor())

        persistentModel = persistentModelBuilder(persistentModel.build())

        when:
        project.getFolder('another1').delete(false, new NullProgressMonitor())
        project.getFolder('another1').create(true, true, new NullProgressMonitor())

        LinkedResourcesUpdater.update(project, [linkedResourceB], persistentModel, new NullProgressMonitor())
        Collection<IResource> linkedResources = linkedResources(project)

        then:
        project.getFolder('another1').exists()
    }

    def "Model linked resources that were previously defined manually are transformed to model linked resources"() {
        given:
        File externalDir = dir('another')
        IProject project = newProject('project-name')
        IPath linkedFolderPath = new Path(externalDir.absolutePath)
        PersistentModelBuilder persistentModel = persistentModelBuilder(project)
        IFolder manuallyDefinedLinkedFolder = project.getFolder(externalDir.name)
        manuallyDefinedLinkedFolder.createLink(linkedFolderPath, IResource.NONE, null);
        EclipseLinkedResource linkedResource = newFolderLinkedResource(externalDir.name, externalDir)
        LinkedResourcesUpdater.update(project, [linkedResource], persistentModel, new NullProgressMonitor())

        persistentModel = persistentModelBuilder(persistentModel.build())

        when:
        LinkedResourcesUpdater.update(project, [], persistentModel, new NullProgressMonitor())

        then:
        !project.getFolder('another').isLinked()
    }

    def "Can create linked resources in the subfolders" () {
        given:
        File externalDir = dir('ext')
        IProject project = newProject('project-name')
        EclipseLinkedResource linkedResource =  newFolderLinkedResource('links/link-to-ext', externalDir)
        PersistentModelBuilder persistentModel = persistentModelBuilder(project)

        when:
        LinkedResourcesUpdater.update(project, [linkedResource], persistentModel, new NullProgressMonitor())

        then:
        linkedResources(project).size() == 1
        project.getFolder('links/link-to-ext').isLinked()
    }

    private EclipseLinkedResource newFolderLinkedResource(String name, File location) {
        EclipseLinkedResource linkedResource = Mock()
        linkedResource.name >> name
        linkedResource.type >> '2'
        linkedResource.location >> location.path
        linkedResource.locationUri >> null
        linkedResource
    }

    private EclipseLinkedResource newVirtualLinkedResource() {
        EclipseLinkedResource linkedResource = Mock()
        linkedResource.name >> 'example'
        linkedResource.type >> '1'
        linkedResource.location >> null
        linkedResource.locationUri >> 'http://example.com'
        linkedResource
    }

    private EclipseLinkedResource newFileLinkedResource(String name, File file) {
        assert file.isFile()
        EclipseLinkedResource linkedResource = Mock()
        linkedResource.name >> name
        linkedResource.type >> '1'
        linkedResource.location >> file.path
        linkedResource.locationUri >> null
        linkedResource
    }

    private Collection<IResource> linkedResources(IProject project) {
        collectLinkedResources(project.members() as List)
    }

    private Collection<IResource> collectLinkedResources(Collection resources, Collection result = []) {
        resources.each { IResource resource ->
            if (resource.linked) {
                result.add(resource)
            } else if (resource instanceof IFolder) {
                collectLinkedResources(resource.members() as List, result)
            }
        }
        result
    }
}
