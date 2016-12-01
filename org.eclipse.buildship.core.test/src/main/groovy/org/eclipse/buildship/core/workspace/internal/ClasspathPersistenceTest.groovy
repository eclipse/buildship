package org.eclipse.buildship.core.workspace.internal

import com.google.common.base.Optional

import org.eclipse.core.resources.IProject
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.workspace.GradleBuild
import org.eclipse.buildship.core.workspace.GradleWorkspaceManager

class ClasspathPersistenceTest extends ProjectSynchronizationSpecification {

    def "the classpath container is persisted"() {
        setup:
        def projectDir = dir('sample-project') {
            file 'build.gradle',  '''apply plugin: "java"
               repositories { jcenter() }
               dependencies { compile "org.springframework:spring-beans:1.2.8"}
            '''
        }
        importAndWait(projectDir)

        GradleBuild gradleBuild = Mock(GradleBuild)
        GradleWorkspaceManager workspaceManager = Mock(GradleWorkspaceManager)
        _ * workspaceManager.getGradleBuild(_) >> Optional.of(gradleBuild)
        registerService(GradleWorkspaceManager, workspaceManager)

        IJavaProject javaProject = JavaCore.create(findProject("sample-project"))
        IProject project = javaProject.project

        expect:
        javaProject.getResolvedClasspath(false).find { it.path.toPortableString().endsWith('spring-beans-1.2.8.jar') }

        when:
        reimportWithoutSynchronization(project)

        then:
        0 * gradleBuild._
        javaProject.getResolvedClasspath(false).find { it.path.toPortableString().endsWith('spring-beans-1.2.8.jar') }
    }

    def "The container initializer does not import new subprojects"() {
        setup:
        def projectDir = dir('sample-project') {
            file 'build.gradle',  'apply plugin: "java"'
        }

        importAndWait(projectDir)

        fileTree(projectDir) {
            dir 'sub'
            file 'settings.gradle', 'include "sub"'
        }

        expect:
        CorePlugin.instance.stateLocation.append("project-preferences").append("sample-project").toFile().delete()

        when:
        reimportWithoutSynchronization(findProject("sample-project"))

        then:
        workspace.root.projects.length == 1
    }

    def "If the cache is still present, the container is kept for broken projects"() {
        setup:
        File projectDir = dir('sample-project') {
            file 'build.gradle',  """apply plugin: "java"
               repositories { jcenter() }
               dependencies { compile "org.springframework:spring-beans:1.2.8"}
            """
        }

        importAndWait(projectDir)
        IProject project = findProject("sample-project")
        IJavaProject javaProject = JavaCore.create(project)

        new File(projectDir, ".settings/org.eclipse.buildship.core.prefs").delete()

        when:
        reimportWithoutSynchronization(project)

        then:
        javaProject.getResolvedClasspath(false).find { it.path.toPortableString().endsWith('spring-beans-1.2.8.jar') }
    }

    def "If the cache is missing, the container is cleared for broken projects"() {
        setup:
        File projectDir = dir('sample-project') {
            file 'build.gradle',  """apply plugin: "java"
               repositories { jcenter() }
               dependencies { compile "org.springframework:spring-beans:1.2.8"}
            """
        }

        importAndWait(projectDir)
        IProject project = findProject("sample-project")
        IJavaProject javaProject = JavaCore.create(project)

        expect:
        new File(projectDir, ".settings/org.eclipse.buildship.core.prefs").delete()
        CorePlugin.instance.stateLocation.append("project-preferences").append("sample-project").toFile().delete()

        when:
        reimportWithoutSynchronization(project)

        then:
        !JavaCore.create(findProject("sample-project")).getResolvedClasspath(false).find { it.path.toPortableString().endsWith('spring-beans-1.2.8.jar') }
    }

    private reimportWithoutSynchronization(IProject project) {
        def descriptor = project.description
        def classpath = CorePlugin.projectPluginStatePreferenceStore().loadProjectPrefs(project).getValue('classpath', null)
        project.delete(false, true, null)
        project.create(descriptor, null)
        def preferences = CorePlugin.projectPluginStatePreferenceStore().loadProjectPrefs(project)
        preferences.setValue('classpath', classpath)
        preferences.flush()
        project.open(null)
        waitForGradleJobsToFinish()
    }

}
