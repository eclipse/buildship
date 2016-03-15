package org.eclipse.buildship.core.workspace.internal

import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IFolder
import org.eclipse.core.resources.IProject

import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification

class DerivedResourcesUpdaterTest extends WorkspaceSpecification {
    IProject project
    IFolder derivedFolder
    IFile derivedFile

    void setup() {
        project = newProject("sample")
        derivedFolder = project.getFolder("derivedFolder")
        derivedFolder.create(true, true, null)
        derivedFile = project.getFile("derivedFile")
        derivedFile.create(new ByteArrayInputStream(new byte[0]), true, null)
    }

    def "Derived resources can be marked on a project"() {
        when:
        DerivedResourcesUpdater.update(project, [derivedFile.name, derivedFolder.name], null)

        then:
        derivedFile.isDerived()
        derivedFolder.isDerived()
    }

    def "Derived resource markers are removed if they no longer exist in the Gradle model"() {
        when:
        DerivedResourcesUpdater.update(project, [derivedFile.name, derivedFolder.name], null)
        DerivedResourcesUpdater.update(project, [], null)

        then:
        !derivedFile.isDerived()
        !derivedFolder.isDerived()
    }

    def "Manual derived markers are preserved"() {
        given:
        def manual = project.getFolder("manual")
        manual.create(true, true, null)
        manual.setDerived(true, null)

        when:
        DerivedResourcesUpdater.clear(project, null)

        then:
        manual.isDerived()
    }

    def "Derived resource markers that were defined manually are transformed to model elements"() {
        given:
        def manual = project.getFolder("manual")
        manual.create(true,, true, null)
        manual.setDerived(true, null)

        when:
        DerivedResourcesUpdater.update(project, [manual.name], null)
        DerivedResourcesUpdater.clear(project, null)

        then:
        !manual.isDerived()
    }

}
