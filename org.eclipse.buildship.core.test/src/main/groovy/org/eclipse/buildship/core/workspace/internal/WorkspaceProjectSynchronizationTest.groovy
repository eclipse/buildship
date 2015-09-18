package org.eclipse.buildship.core.workspace.internal

import groovy.lang.Closure
import org.gradle.tooling.model.eclipse.EclipseProject
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Ignore
import spock.lang.IgnoreIf
import spock.lang.Specification

import com.google.common.base.Optional

import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild
import com.gradleware.tooling.toolingmodel.OmniEclipseProject
import com.gradleware.tooling.toolingmodel.OmniGradleProject
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes
import com.gradleware.tooling.toolingmodel.util.Maybe

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IProjectDescription
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.SubProgressMonitor
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.test.fixtures.BuildshipTestSpecification
import org.eclipse.buildship.core.test.fixtures.EclipseProjects
import org.eclipse.buildship.core.test.fixtures.FileStructure
import org.eclipse.buildship.core.test.fixtures.GradleModel
import org.eclipse.buildship.core.test.fixtures.LegacyEclipseSpockTestHelper

class WorkspaceProjectSynchronizationTest extends BuildshipTestSpecification {

    def "If workspace project exists at model location and closed then the project remins untouched"() {
        setup:
        IProject project = EclipseProjects.newClosedProject('sample-project', folder('sample-project'))

        fileStructure().create {
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')
        File[] projectFiles = folder('sample-project').listFiles()
        Long[] modifiedTimes = folder('sample-project').listFiles().collect{ it.lastModified() }

        when:
        executeSynchronizeGradleProjectWithWorkspaceProject(gradleModel)

        then:
        !project.isOpen()
        projectFiles == folder('sample-project').listFiles()
        modifiedTimes == folder('sample-project').listFiles().collect{ it.lastModified() }
    }

    def "If workspace project exists at model location, then the Gradle nature is set"() {
        setup:
        IProject project = EclipseProjects.newProject('sample-project', folder('sample-project'))

        fileStructure().create {
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProject(gradleModel)

        then:
        project.hasNature(GradleProjectNature.ID)
    }

    def "If workspace project exists at model location, then the Gradle settings file is written"() {
        setup:
        IProject project = EclipseProjects.newProject('sample-project', folder('sample-project'))

        fileStructure().create {
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProject(gradleModel)

        then:
        file('sample-project/.settings/gradle.prefs').exists()
        file('sample-project/.settings/gradle.prefs').text.length() > 0
    }

    @Ignore // TODO (donat) the documentation says we re-add the resource filters, but the tests finds otherwise
    def "If workspace project exists at model location, then resource filters are set"() {
        setup:
        IProject project = EclipseProjects.newProject('sample-project', folder('sample-project'))

        fileStructure().create {
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        expect:
        project.filters.length == 0

        when:
        executeSynchronizeGradleProjectWithWorkspaceProject(gradleModel)

        then:
        project.filters.length > 0
    }

    @Ignore // TODO (donat) should we add the Java nature? Should we add the Gradle classpath container?
    def "If workspace project exists at model location and the model applies the java plug-in, then the java nature is set up"() {
        setup:
        IProject project = EclipseProjects.newProject('sample-project', folder('sample-project'))

        fileStructure().create {
            file 'sample-project/build.gradle', 'apply plugin: "java"'
            file 'sample-project/settings.gradle'
            folder 'sample-project/src/main/java'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProject(gradleModel)

        then:
        project.hasNature(JavaCore.NATURE_ID)
    }

    // -- helper methods --

    private static def executeSynchronizeGradleProjectWithWorkspaceProject(GradleModel gradleModel) {
        CorePlugin.workspaceGradleOperations().synchronizeGradleProjectWithWorkspaceProject(
            gradleModel.eclipseProject('sample-project'),
            gradleModel.build,
            gradleModel.attributes,
            [],
            new NullProgressMonitor())
    }

    private IProject newClosedProject(String name) {
        EclipseProjects.newClosedProject(name, folder(name))
    }

    private IProject newOpenProject(String name) {
        EclipseProjects.newProject(name, folder(name))
    }

    private FileStructure fileStructure() {
        new FileStructure(externalTestFolder){}
    }

    private GradleModel loadGradleModel(String location) {
        GradleModel.fromProject(folder(location))
    }

}
