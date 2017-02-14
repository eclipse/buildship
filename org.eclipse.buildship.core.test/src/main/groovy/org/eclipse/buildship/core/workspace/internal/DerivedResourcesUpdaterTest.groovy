package org.eclipse.buildship.core.workspace.internal

import com.gradleware.tooling.toolingmodel.OmniEclipseProject
import com.gradleware.tooling.toolingmodel.OmniGradleProject
import com.gradleware.tooling.toolingmodel.util.Maybe

import org.eclipse.core.resources.IFolder
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.Path
import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.preferences.PersistentModel
import org.eclipse.buildship.core.preferences.PersistentModelBuilder
import org.eclipse.buildship.core.preferences.internal.DefaultPersistentModel
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification

class DerivedResourcesUpdaterTest extends WorkspaceSpecification {
    IProject project
    IFolder buildFolder
    IFolder newBuildFolder
    IFolder dotGradleFolder

    void setup() {
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
        PersistentModel persistentModel = emptyPersistentModel(project)
        PersistentModelBuilder modelUpdates = PersistentModel.builder(persistentModel)

        when:
        DerivedResourcesUpdater.update(project, model(), persistentModel, modelUpdates, null)

        then:
        buildFolder.isDerived()
        dotGradleFolder.isDerived()
    }

    def "Derived resource markers are removed if they no longer exist in the Gradle model"() {
        setup:
        PersistentModel persistentModel = emptyPersistentModel(project)
        PersistentModelBuilder modelUpdates = PersistentModel.builder(persistentModel)
        DerivedResourcesUpdater.update(project, model('build'), persistentModel, modelUpdates, null)

        persistentModel = modelUpdates.build();
        modelUpdates = PersistentModel.builder(persistentModel)

        when:
        DerivedResourcesUpdater.update(project, model('build'), persistentModel, modelUpdates, null)
        DerivedResourcesUpdater.update(project, model('target'), persistentModel, modelUpdates, null)

        then:
        !buildFolder.isDerived()
        newBuildFolder.isDerived()
    }

    def "Manual derived markers are preserved"() {
        setup:
        def manual = project.getFolder('manual')
        manual.create(true, true, null)
        manual.setDerived(true, null)
        PersistentModel persistentModel = emptyPersistentModel(project)
        PersistentModelBuilder modelUpdates = PersistentModel.builder(persistentModel)

        when:
        DerivedResourcesUpdater.update(project, model(), persistentModel, modelUpdates, null)

        then:
        manual.isDerived()
    }

    def "Derived resource markers that were defined manually are transformed to model elements"() {
        setup:
        buildFolder.setDerived(true, null)
        PersistentModel persistentModel = emptyPersistentModel(project)
        PersistentModelBuilder modelUpdates = PersistentModel.builder(persistentModel)
        DerivedResourcesUpdater.update(project, model('build'), persistentModel, modelUpdates, null)

        persistentModel = modelUpdates.build();
        modelUpdates = PersistentModel.builder(persistentModel)

        when:
        DerivedResourcesUpdater.update(project, model('target'), persistentModel, modelUpdates, null)

        then:
        !buildFolder.isDerived()
    }

    private def model(String buildDir = 'build') {
        OmniEclipseProject eclipseProject = Mock(OmniEclipseProject)
        OmniGradleProject gradleProject = Mock(OmniGradleProject)
        gradleProject.buildDirectory >> Maybe.of(new File(project.location.toFile(), buildDir))
        eclipseProject.gradleProject >> gradleProject
        eclipseProject
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
