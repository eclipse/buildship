package org.eclipse.buildship.core.workspace.internal

import com.gradleware.tooling.toolingmodel.OmniEclipseLinkedResource

import org.eclipse.core.resources.IFolder
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path

import org.eclipse.buildship.core.preferences.PersistentModel
import org.eclipse.buildship.core.preferences.PersistentModelBuilder
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification

class LinkedResourcesUpdaterTest extends WorkspaceSpecification {

    def "Can define a linked resource"() {
        given:
        File externalDir = dir('another')
        IProject project = newProject('project-name')
        OmniEclipseLinkedResource linkedResource =  newFolderLinkedResource(externalDir.name, externalDir)
        PersistentModel persistentModel = emptyPersistentModel(project)
        PersistentModelBuilder modelUpdates = PersistentModel.builder(persistentModel)

        when:
        LinkedResourcesUpdater.update(project, [linkedResource], persistentModel, modelUpdates, new NullProgressMonitor())
        Collection<IFolder> linkedFolders = linkedFolders(project)

        then:
        linkedFolders.size() == 1
        linkedFolders[0].name == 'another'
        linkedFolders[0].exists()
        linkedFolders[0].location.toFile().equals(externalDir)
    }

    def "Can define a linked resource even if the resource does not exist"() {
        given:
            File externalDir = getDir('another')
            IProject project = newProject('project-name')
            OmniEclipseLinkedResource linkedResource =  newFolderLinkedResource(externalDir.name, externalDir)
            PersistentModel persistentModel = emptyPersistentModel(project)
            PersistentModelBuilder modelUpdates = PersistentModel.builder(persistentModel)

            when:
            LinkedResourcesUpdater.update(project, [linkedResource], persistentModel, modelUpdates, new NullProgressMonitor())
            Collection<IFolder> linkedFolders = linkedFolders(project)

            then:
            linkedFolders.size() == 1
            linkedFolders[0].name == 'another'
            linkedFolders[0].exists()
            linkedFolders[0].location.toFile().equals(externalDir)
    }

    def "Defining a linked resource is idempotent"() {
        given:
        File externalDir = dir('another')
        IProject project = newProject('project-name')
        OmniEclipseLinkedResource linkedResource =  newFolderLinkedResource(linkName, externalDir)
        PersistentModel persistentModel = emptyPersistentModel(project)
        PersistentModelBuilder modelUpdates = PersistentModel.builder(persistentModel)
        LinkedResourcesUpdater.update(project, [linkedResource], persistentModel, modelUpdates, new NullProgressMonitor())
        linkedResource = newFolderLinkedResource(linkName, externalDir)

        persistentModel = modelUpdates.build()
        modelUpdates = PersistentModel.builder(persistentModel)

        when:
        LinkedResourcesUpdater.update(project, [linkedResource], persistentModel, modelUpdates, new NullProgressMonitor())
        Collection<IFolder> linkedFolders = linkedFolders(project)

        then:
        linkedFolders.size() == 1
        linkedFolders[0].name == expectedFolderName
        linkedFolders[0].fullPath.toPortableString() == expectedFolderPath

        where:
        linkName  | expectedFolderName | expectedFolderPath
        'another' | 'another'          | '/project-name/another'
        'a/b/c'   | 'c'                | '/project-name/a/b/c'
    }

    def "Only local folder linked resources are set on the project" () {
        given:
        File externalDir = dir('another')
        File externalFile = file('file')
        IProject project = newProject('project-name')
        OmniEclipseLinkedResource localFolder =  newFolderLinkedResource(externalDir.name, externalDir)
        OmniEclipseLinkedResource localFile =  newFileLinkedResource(externalFile.name, externalFile)
        OmniEclipseLinkedResource virtualResource =  newVirtualLinkedResource()
        PersistentModel persistentModel = emptyPersistentModel(project)
        PersistentModelBuilder modelUpdates = PersistentModel.builder(persistentModel)

        when:
        LinkedResourcesUpdater.update(project, [localFile, localFolder, virtualResource], persistentModel, modelUpdates, new NullProgressMonitor())

        then:
        linkedFolders(project).size() == 1
    }

    def "If a linked resource name matches to an existing folder, then the folder is replaced" () {
        given:
        IProject project = newProject('project-name')
        project.getFolder('foldername').create(true, true, null)
        File externalDir = dir('foldername')
        OmniEclipseLinkedResource linkedResource =  newFolderLinkedResource(externalDir.name, externalDir)
        PersistentModel persistentModel = emptyPersistentModel(project)
        PersistentModelBuilder modelUpdates = PersistentModel.builder(persistentModel)

        when:
        LinkedResourcesUpdater.update(project, [linkedResource], persistentModel, modelUpdates, new NullProgressMonitor())
        Collection<IFolder> linkedFolders = linkedFolders(project)

        then:
        linkedFolders.size() == 1
        linkedFolders[0].name == 'foldername'
    }

    def "A linked resource is deleted if no longer part of the Gradle model"() {
        given:
        File externalDirA = dir('another1')
        File externalDirB = dir('another2')
        IProject project = newProject('project-name')
        OmniEclipseLinkedResource linkedResourceA =  newFolderLinkedResource(linkName, externalDirA)
        OmniEclipseLinkedResource linkedResourceB =  newFolderLinkedResource(externalDirB.name, externalDirB)
        PersistentModel persistentModel = emptyPersistentModel(project)
        PersistentModelBuilder modelUpdates = PersistentModel.builder(persistentModel)
        LinkedResourcesUpdater.update(project, [linkedResourceA], persistentModel, modelUpdates, new NullProgressMonitor())

        persistentModel = modelUpdates.build()
        modelUpdates = PersistentModel.builder(persistentModel)

        when:
        LinkedResourcesUpdater.update(project, [linkedResourceB], persistentModel, modelUpdates, new NullProgressMonitor())
        Collection<IFolder> linkedFolders = linkedFolders(project)

        then:
        !project.getFolder(linkName).exists()
        linkedFolders.size() == 1
        linkedFolders[0].name == 'another2'
        linkedFolders[0].exists()
        linkedFolders[0].location.toFile().equals(externalDirB)

        where:
        linkName << ['another', 'a/b/c']
    }

    def "Only linked resources are removed from the project"() {
        given:
        File externalDirA = dir('another1')
        File externalDirB = dir('another2')
        IProject project = newProject('project-name')
        OmniEclipseLinkedResource linkedResourceA =  newFolderLinkedResource(externalDirA.name, externalDirA)
        OmniEclipseLinkedResource linkedResourceB =  newFolderLinkedResource(externalDirB.name, externalDirB)
        PersistentModel persistentModel = emptyPersistentModel(project)
        PersistentModelBuilder modelUpdates = PersistentModel.builder(persistentModel)
        LinkedResourcesUpdater.update(project, [linkedResourceA], persistentModel, modelUpdates, new NullProgressMonitor())

        persistentModel = modelUpdates.build()
        modelUpdates = PersistentModel.builder(persistentModel)

        when:
        project.getFolder('another1').delete(false, new NullProgressMonitor())
        project.getFolder('another1').create(true, true, new NullProgressMonitor())
        LinkedResourcesUpdater.update(project, [linkedResourceB], persistentModel, modelUpdates, new NullProgressMonitor())
        Collection<IFolder> linkedFolders = linkedFolders(project)

        then:
        project.getFolder('another1').exists()
    }

    def "Model linked resources that were previously defined manually are transformed to model linked resources"() {
        given:
        File externalDir = dir('another')
        IProject project = newProject('project-name')
        IPath linkedFolderPath = new Path(externalDir.absolutePath)
        PersistentModel persistentModel = emptyPersistentModel(project)
        PersistentModelBuilder modelUpdates = PersistentModel.builder(persistentModel)
        IFolder manuallyDefinedLinkedFolder = project.getFolder(externalDir.name)
        manuallyDefinedLinkedFolder.createLink(linkedFolderPath, IResource.NONE, null);
        OmniEclipseLinkedResource linkedResource = newFolderLinkedResource(externalDir.name, externalDir)
        LinkedResourcesUpdater.update(project, [linkedResource], persistentModel, modelUpdates, new NullProgressMonitor())

        persistentModel = modelUpdates.build()
        modelUpdates = PersistentModel.builder(persistentModel)

        when:
        LinkedResourcesUpdater.update(project, [], persistentModel,modelUpdates, new NullProgressMonitor())

        then:
        !project.getFolder('another').isLinked()
    }

    def "Can create linked resources in the subfolders" () {
        given:
        File externalDir = dir('ext')
        IProject project = newProject('project-name')
        OmniEclipseLinkedResource linkedResource =  newFolderLinkedResource('links/link-to-ext', externalDir)
        PersistentModel persistentModel = emptyPersistentModel(project)
        PersistentModelBuilder modelUpdates = PersistentModel.builder(persistentModel)

        when:
        LinkedResourcesUpdater.update(project, [linkedResource], persistentModel, modelUpdates, new NullProgressMonitor())

        then:
        linkedFolders(project).size() == 1
        project.getFolder('links/link-to-ext').isLinked()
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

    private def linkedFolders(IProject project) {
        collectLinkedFolders(project.members() as List)
    }

    private def collectLinkedFolders(Collection resources, Collection result = []) {
        resources.each { resource ->
            if (resource instanceof IFolder) {
                if (resource.linked) {
                    result.add(resource)
                } else {
                    collectLinkedFolders(resource.members() as List, result)
                }
            }
        }
        result
    }

    private PersistentModel emptyPersistentModel(IProject project) {
        PersistentModel.builder(project)
            .buildDir(new Path("build"))
            .subprojectPaths([])
            .classpath([])
            .derivedResources([])
            .linkedResources([])
            .build()
    }
}
