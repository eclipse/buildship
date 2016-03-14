package org.eclipse.buildship.core.workspace.internal

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Issue;

import com.google.common.collect.ImmutableList

import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.GradleProjectBuilder
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.test.fixtures.LegacyEclipseSpockTestHelper
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification;
import org.eclipse.buildship.core.test.fixtures.TestEnvironment
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper
import org.eclipse.buildship.core.util.progress.AsyncHandler
import org.eclipse.buildship.core.util.variable.ExpressionUtils
import org.eclipse.buildship.core.workspace.WorkspaceGradleOperations;;

class ImportingProjectInDefaultLocation extends ProjectSynchronizationSpecification {

    def "Can import project located in default location"() {
        when:
        synchronizeAndWait(newSampleProject())

        then:
        workspace.root.projects.length == 1
    }

    @Issue("https://bugs.eclipse.org/bugs/show_bug.cgi?id=472223")
    def "Can import project located in workspace folder and with custom root name"() {
        setup:
        File rootProject = fileTree(newSampleProject()) {
            file 'settings.gradle', "rootProject.name = 'my-project-name-is-different-than-the-folder'"
        }

        when:
        synchronizeAndWait(rootProject)

        then : "The project is imported and stays in the same folder"
        workspace.root.projects.length == 1
        def project = workspace.root.projects[0]
        project.location.toFile() == rootProject
    }

    @Issue("https://bugs.eclipse.org/bugs/show_bug.cgi?id=476921")
    def "Can depend on project located in workspace folder and with custom root name"() {
        setup:
        File rootProject = fileTree(newSampleProject()) {
            file 'settings.gradle', """
                rootProject.name = 'my-project-name-is-different-than-the-folder'
                include 'sub'
            """
            file 'build.gradle', "apply plugin: 'java'"
            dir('sub') {
                file 'build.gradle', """
                    apply plugin: 'java'
                    dependencies {
                        compile rootProject
                    }
                """
            }
        }

        when:
        synchronizeAndWait(rootProject)

        then :
        IProject sub = findProject("sub")
        IJavaProject javaProject = JavaCore.create(sub)
        javaProject.getResolvedClasspath(true).find {
            it.path.toString() == "/sample"
        }
    }

    def File newSampleProject() {
        workspaceDir('sample')
    }

}
