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
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.SubProgressMonitor
import org.eclipse.core.runtime.jobs.IJobManager
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.test.fixtures.BuildshipTestSpecification
import org.eclipse.buildship.core.test.fixtures.EclipseProjects
import org.eclipse.buildship.core.test.fixtures.FileStructure
import org.eclipse.buildship.core.test.fixtures.GradleModel
import org.eclipse.buildship.core.test.fixtures.LegacyEclipseSpockTestHelper
import org.eclipse.buildship.core.workspace.GradleClasspathContainer

class WorkspaceProjectSynchronizationTest extends BuildshipTestSpecification {

    def "If workspace project exists at model location and closed then the project remins untouched"() {
        setup:
        IProject project = newClosedProject('sample-project')
        fileStructure().create {
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')
        File[] projectFiles = folder('sample-project').listFiles()
        Long[] modifiedTimes = folder('sample-project').listFiles().collect{ it.lastModified() }

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        !project.isOpen()
        projectFiles == folder('sample-project').listFiles()
        modifiedTimes == folder('sample-project').listFiles().collect{ it.lastModified() }
    }

    def "If workspace project exists at model location, then the Gradle nature is set"() {
        setup:
        IProject project = newOpenProject('sample-project')
        fileStructure().create {
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        project.hasNature(GradleProjectNature.ID)
    }

    def "If workspace project exists at model location, then the Gradle settings file is written"() {
        setup:
        IProject project = newOpenProject('sample-project')
        fileStructure().create {
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        file('sample-project/.settings/gradle.prefs').exists()
        file('sample-project/.settings/gradle.prefs').text.length() > 0
    }

    // TODO (donat) the documentation says the filters _should_ be set
    def "If workspace project exists at model location, then resource filters are not set"() {
        setup:
        IProject project = newOpenProject('sample-project')
        fileStructure().create {
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        expect:
        project.filters.length == 0

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        project.filters.length == 0
    }

    @Ignore // TODO (donat) test is failing. Should we really add the Java nature? Should we add the Gradle classpath container?
    def "If workspace project exists at model location and the model applies the java plug-in, then the java nature is set up"() {
        setup:
        IProject project = newOpenProject('sample-project')
        fileStructure().create {
            file 'sample-project/build.gradle', 'apply plugin: "java"'
            file 'sample-project/settings.gradle'
            folder 'sample-project/src/main/java'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        project.hasNature(JavaCore.NATURE_ID)
    }

    def "If .project file exists at the model location, then the project is added to the workspace"() {
        setup:
        IProject project = newOpenProject('sample-project')
        CorePlugin.workspaceOperations().deleteAllProjects(new NullProgressMonitor())
        fileStructure().create {
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        expect:
        CorePlugin.workspaceOperations().getAllProjects().isEmpty()
        file('sample-project/.project').exists()

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        CorePlugin.workspaceOperations().allProjects.size() == 1
        findProject('sample-project')
    }

    def "If .project file exists at the model location, then the Gradle nature is set"() {
        setup:
        IProject project = newOpenProject('sample-project')
        CorePlugin.workspaceOperations().deleteAllProjects(new NullProgressMonitor())
        fileStructure().create {
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        findProject('sample-project').hasNature(GradleProjectNature.ID)
    }

    def "If .project file exists at the model location, then the Gradle settings file is written"() {
        setup:
        IProject project = newOpenProject('sample-project')
        CorePlugin.workspaceOperations().deleteAllProjects(new NullProgressMonitor())
        fileStructure().create {
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        findProject('sample-project').getFile('.settings/gradle.prefs').exists()
    }

    def "If no workspace project or .project file exists, then the project is imported in the workspace"() {
        setup:
        fileStructure().create {
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        expect:
        CorePlugin.workspaceOperations().getAllProjects().isEmpty()

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        CorePlugin.workspaceOperations().allProjects.size() == 1
        findProject('sample-project')
    }

    def "If no workspace project or .project file exists, then the Gradle nature is set"() {
        setup:
        fileStructure().create {
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        CorePlugin.workspaceOperations().allProjects.size() == 1
        findProject('sample-project').hasNature(GradleProjectNature.ID)
    }


    def "If no workspace project or .project file exists, then the settings file is set"() {
        setup:
        fileStructure().create {
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        CorePlugin.workspaceOperations().allProjects.size() == 1
        findProject('sample-project').getFile('.settings/gradle.prefs').exists()
    }

    def "If no workspace project or .project file exists, then the resource filters are set"() {
        setup:
        fileStructure().create {
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        findProject('sample-project').filters.length > 0
    }

    def "If no workspace project or .project file exists, then the linked resources are set"() {
        setup:
        fileStructure().create {
            file 'sample-project/build.gradle', '''apply plugin: "java"
                                                   sourceSets { main { java { srcDir '../another-project/src' } } }'''
            file 'sample-project/settings.gradle'
            folder 'another-project/src'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        findProject('sample-project').getFolder('src').isLinked()
    }

    def "If no workspace project or .project file exists, then a Java project is set, in case the Gradle project applies the Java plug-in"() {
        setup:
        fileStructure().create {
            file 'sample-project/build.gradle', 'apply plugin: "java"'
            file 'sample-project/settings.gradle'
            folder 'sample-project/src/main/java'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        findProject('sample-project').hasNature(JavaCore.NATURE_ID)
    }

    // -- helper methods --

    private static def executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(GradleModel gradleModel) {
        // Note: executing the synchronizeGradleProjectWithWorkspaceProject() in a new job is necessary
        // as the jdt operations expect that all modifications are guarded by proper rules. For the sake
        // of this test class we simply use the workspace root as the job rule.
        Job job = new Job('') {
            protected IStatus run(IProgressMonitor monitor) {
                Job.jobManager.beginRule(LegacyEclipseSpockTestHelper.workspace.root, monitor)
                CorePlugin.workspaceGradleOperations().synchronizeGradleProjectWithWorkspaceProject(
                        gradleModel.eclipseProject('sample-project'),
                        gradleModel.build,
                        gradleModel.attributes,
                        [],
                        new NullProgressMonitor())
                Job.jobManager.endRule(LegacyEclipseSpockTestHelper.workspace.root)
                Status.OK_STATUS
            }
        }
        job.schedule()
        job.join()
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

    private IProject findProject(String name) {
        CorePlugin.workspaceOperations().findProjectByName(name).orNull()
    }

}
