package org.eclipse.buildship.core.internal.preferences

import spock.lang.Issue

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.internal.util.gradle.GradleVersion
import org.eclipse.buildship.core.internal.util.gradle.GradleVersionTest

class DefaultModelPersistenceTest extends WorkspaceSpecification {

    IProject project

    def setup() {
        project = newProject('sample-project')
    }

    def "Absent model is returned if no models were saved for the project"() {
        when:
        PersistentModel model = CorePlugin.modelPersistence().loadModel(project)

        then:
        !model.present
    }

    def "Absent model throws runtime exceptions from all getters"() {
        setup:
        PersistentModel model = CorePlugin.modelPersistence().loadModel(project)

        when:
        model."$method"()

        then:
        thrown IllegalStateException

        where:
        method << [ 'getBuildDir', 'getSubprojectPaths', 'getClasspath', 'getDerivedResources', 'getLinkedResources' ]
    }

    def "Can store and load a model"() {
        setup:
        def buildDir = new Path('buildDir')
        def buildScriptPath = new Path('build.gradle')
        def subProjectPaths = [new Path('subproject')]
        def classpath = [JavaCore.newProjectEntry(new Path('/project-path'))]
        def derivedResources = [new Path('derived')]
        def linkedResources = [project.getFolder('linked')]
        def managedNatures = ['org.eclipse.pde.UpdateSiteNature']
        def command = project.description.newCommand()
        command.setBuilderName('custom-command')
        def managedBuilders = [command]
        def hasAutoBuildTasks = true
        def gradleVersion = GradleVersion.version('5.6')

        PersistentModel model = new DefaultPersistentModel(project, buildDir, buildScriptPath, subProjectPaths, classpath, derivedResources, linkedResources, managedNatures, managedBuilders, hasAutoBuildTasks, gradleVersion)

        when:
        CorePlugin.modelPersistence().saveModel(model)
        model = CorePlugin.modelPersistence().loadModel(project)

        then:
        model.present
        model.project == project
        model.buildDir == buildDir
        model.subprojectPaths == subProjectPaths
        model.classpath == classpath
        model.derivedResources == derivedResources
        model.linkedResources == linkedResources
    }

    def "Can delete a model"() {
        setup:
        def buildDir = new Path('buildDir')
        def buildScriptPath = new Path('build.gradle')
        def subProjectPaths = [new Path('subproject')]
        def classpath = [JavaCore.newProjectEntry(new Path('/project-path'))]
        def derivedResources = [new Path('derived')]
        def linkedResources = [project.getFolder('linked')]
        def managedNatures = ['org.eclipse.pde.UpdateSiteNature']
        def command = project.description.newCommand()
        command.setBuilderName('custom-command')
        def managedBuilders = [command]
        def hasAutoBuildTasks = false
        def gradleVersion = GradleVersion.version('5.6')

        PersistentModel model = new DefaultPersistentModel(project, buildDir, buildScriptPath, subProjectPaths, classpath, derivedResources, linkedResources, managedNatures, managedBuilders, hasAutoBuildTasks, gradleVersion)
        CorePlugin.modelPersistence().saveModel(model)

        when:
        CorePlugin.modelPersistence().deleteModel(project)
        model = CorePlugin.modelPersistence().loadModel(project)

        then:
        !model.present
    }

    def "Model is still accessible if the referenced project is renamed"() {
        setup:
        def buildDir = new Path('buildDir')
        def buildScriptPath = new Path('build.gradle')
        def subProjectPaths = [new Path('subproject')]
        def classpath = [JavaCore.newProjectEntry(new Path('/project-path'))]
        def derivedResources = [new Path('derived')]
        def linkedResources = [project.getFolder('linked')]
        def managedNatures = ['org.eclipse.pde.UpdateSiteNature']
        def command = project.description.newCommand()
        command.setBuilderName('custom-command')
        def managedBuilders = [command]
        def hasAutoBuildTasks = true
        def gradleVersion = GradleVersion.version('5.6')

        PersistentModel model = new DefaultPersistentModel(project, buildDir, buildScriptPath, subProjectPaths, classpath, derivedResources, linkedResources, managedNatures, managedBuilders, hasAutoBuildTasks, gradleVersion)
        CorePlugin.modelPersistence().saveModel(model)

        when:
        project = CorePlugin.workspaceOperations().renameProject(project, 'new-project-name', new NullProgressMonitor())
        model = CorePlugin.modelPersistence().loadModel(project)

        then:
        model.present
        model.buildDir == buildDir
        model.subprojectPaths == subProjectPaths
        model.classpath == classpath
        model.derivedResources == derivedResources
        model.linkedResources == linkedResources
    }

    @Issue('https://github.com/eclipse/buildship/issues/404')
    def "Cached absent model is not persisted"() {
        setup:
        DefaultModelPersistence persistence = CorePlugin.modelPersistence()
        persistence.loadModel(project)

        when:
        persistence.persistAllProjectPrefs()

        then:
        notThrown RuntimeException
    }
}
