package org.eclipse.buildship.core.workspace.internal

import org.eclipse.core.resources.IFolder
import org.eclipse.core.resources.IProject

import org.eclipse.buildship.core.omnimodel.OmniEclipseProject
import org.eclipse.buildship.core.omnimodel.OmniGradleProject
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.util.gradle.Maybe

class GradleFolderUpdaterTest extends WorkspaceSpecification {
    IProject project
    IFolder buildFolder
    IFolder newBuildFolder
    IFolder dotGradleFolder

    def setup() {
        project = newProject('sample')
        buildFolder = project.getFolder('build')
        buildFolder.create(true, true, null)
        newBuildFolder = project.getFolder('target')
        newBuildFolder.create(true, true, null)
        dotGradleFolder = project.getFolder('.gradle')
        dotGradleFolder.create(true, true, null)
    }

    def "Derived resources can be marked on a project"() {
        given:
        PersistentModelBuilder persistentModel = persistentModelBuilder(project)

        when:
        GradleFolderUpdater.update(project, model(), persistentModel, null)

        then:
        buildFolder.isDerived()
        dotGradleFolder.isDerived()
    }

    def "Derived resource markers are removed if they no longer exist in the Gradle model"() {
        setup:
        PersistentModelBuilder persistentModel = persistentModelBuilder(project)
        GradleFolderUpdater.update(project, model('build'), persistentModel, null)

        persistentModel =  persistentModelBuilder(persistentModel.build())

        when:
        GradleFolderUpdater.update(project, model('build'), persistentModel, null)
        GradleFolderUpdater.update(project, model('target'), persistentModel, null)

        then:
        !buildFolder.isDerived()
        newBuildFolder.isDerived()
    }

    def "Manual derived markers are preserved"() {
        setup:
        def manual = project.getFolder('manual')
        manual.create(true, true, null)
        manual.setDerived(true, null)
        PersistentModelBuilder persistentModel = persistentModelBuilder(project)

        when:
        GradleFolderUpdater.update(project, model(), persistentModel, null)

        then:
        manual.isDerived()
    }

    def "Derived resource markers that were defined manually are transformed to model elements"() {
        setup:
        buildFolder.setDerived(true, null)
        PersistentModelBuilder persistentModel = persistentModelBuilder(project)
        GradleFolderUpdater.update(project, model('build'), persistentModel, null)

        persistentModel = persistentModelBuilder(persistentModel.build())

        when:
        GradleFolderUpdater.update(project, model('target'), persistentModel, null)

        then:
        !buildFolder.isDerived()
    }

    private def model(String buildDir = 'build') {
        OmniEclipseProject eclipseProject = Mock(OmniEclipseProject)
        OmniGradleProject gradleProject = Mock(OmniGradleProject)
        gradleProject.buildDirectory >> Maybe.of(new File(project.location.toFile(), buildDir))
        eclipseProject.gradleProject >> gradleProject
        eclipseProject.projectDirectory >> project.location.toFile()
        eclipseProject.all >> [eclipseProject]
        eclipseProject
    }
}
