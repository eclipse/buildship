package org.eclipse.buildship.core.projectimport

import com.gradleware.tooling.toolingclient.GradleDistribution

import org.eclipse.core.runtime.IPath;

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.GradleProjectBuilder
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.gradle.GradleDistributionWrapper

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class ProjectImportJobTest extends Specification {

    @Rule
    TemporaryFolder tempFolder

    def cleanup() {
        CorePlugin.workspaceOperations().deleteAllProjects(null)
    }

    def "Project import job creates a new project in the workspace"(boolean projectDescriptorExists) {
        setup:
        def applyJavaPlugin = false
        File projectLocation = newProject(projectDescriptorExists, applyJavaPlugin)
        ProjectImportJob job = newProjectImportJob(projectLocation)

        when:
        job.schedule()
        job.join()

        then:
        CorePlugin.workspaceOperations().findProjectByName(projectLocation.name).present

        where:
        projectDescriptorExists << [false, true]
    }

    def "Project descriptors should be created iff they don't already exist"(boolean applyJavaPlugin, boolean projectDescriptorExists, String descriptorComment) {
        setup:
        File rootProject = newProject(projectDescriptorExists, applyJavaPlugin)
        ProjectImportJob job = newProjectImportJob(rootProject)

        when:
        job.schedule()
        job.join()

        then:
        new File(rootProject, '.project').exists()
        new File(rootProject, '.classpath').exists() == applyJavaPlugin
        CorePlugin.workspaceOperations().findProjectInFolder(rootProject, null).get().getComment() == descriptorComment

        where:
        applyJavaPlugin | projectDescriptorExists | descriptorComment
        false           | false                   | 'Project created by Buildship' // the comment from the generated descriptor
        false           | true                    | 'original'                     // the comment from the original descriptor
        true            | false                   | 'Project created by Buildship'
        true            | true                    | 'original'
    }

    def "Imported projects always have Gradle builder and nature"(boolean projectDescriptorExists) {
        setup:
        File rootProject = newProject(projectDescriptorExists, false)
        ProjectImportJob job = newProjectImportJob(rootProject)

        when:
        job.schedule()
        job.join()

        then:
        def project = CorePlugin.workspaceOperations().findProjectByName(rootProject.name).get()
        GradleProjectNature.INSTANCE.isPresentOn(project)
        project.description.buildSpec.find { it.getBuilderName().equals(GradleProjectBuilder.INSTANCE.ID) }

        where:
        projectDescriptorExists << [false, true]
    }

    def "Imported parent projects have filters to hide the content of the children"() {
        setup:
        File rootProject = newMultiProject()
        ProjectImportJob job = newProjectImportJob(rootProject)

        when:
        job.schedule()
        job.join()
        def project = CorePlugin.workspaceOperations().findProjectByName(rootProject.name).get()
        def filters = project.getFilters()

        then:
        filters.length == 1
        filters[0].fileInfoMatcherDescription.arguments.arguments == ['subproject']
    }

    def "Importing a project twice won't result in duplicate filters"() {
        setup:
        File rootProject = newMultiProject()
        ProjectImportJob job = newProjectImportJob(rootProject)

        when:
        job.schedule()
        job.join()
        CorePlugin.workspaceOperations().deleteAllProjects()
        job = newProjectImportJob(rootProject)
        job.schedule()
        job.join()

        def project = CorePlugin.workspaceOperations().findProjectByName(rootProject.name).get()
        def filters = project.getFilters()

        then:
        filters.length == 1
    }

    def newProject(boolean projectDescriptorExists, boolean applyJavaPlugin) {
        def root = tempFolder.newFolder('simple-project')
        def buildGradle = new File(root, 'build.gradle')
        def sourceFile = new File(root, 'src/main/java')
        sourceFile.mkdirs()
        buildGradle.text = applyJavaPlugin ? "apply plugin: 'java'" : " "
        if (projectDescriptorExists) {
            def dotProject = new File(root, '.project')
            dotProject.text = '''<?xml version="1.0" encoding="UTF-8"?><projectDescription><name>simple-project</name><comment>original</comment><projects>
               </projects><buildSpec></buildSpec><natures></natures></projectDescription>'''
            if (applyJavaPlugin) {
                def dotClasspath = new File(root, '.classpath')
                dotClasspath.text = '''<?xml version="1.0" encoding="UTF-8"?><classpath><classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>
                    <classpathentry kind="src" path="src/main/java"/><classpathentry kind="output" path="bin"/></classpath>'''
            }
        }
        root
    }

    def newMultiProject() {
        def rootProject = tempFolder.newFolder('multi-project')
        def rootBuildGradle = new File(rootProject, 'build.gradle')
        rootBuildGradle << ' '
        def rootSettingsGradle = new File(rootProject, 'settings.gradle')
        rootSettingsGradle << 'include "subproject"'
        def subProject = new File(rootProject, "subproject")
        subProject.mkdirs()
        def subBuildGradle = new File(subProject, 'build.gradle')
        subBuildGradle << ' '
        rootProject
    }

    def newProjectImportJob(File location) {
        ProjectImportConfiguration configuration = new ProjectImportConfiguration()
        configuration.gradleDistribution = GradleDistributionWrapper.from(GradleDistribution.fromBuild())
        configuration.projectDir = location
        new ProjectImportJob(configuration)
    }

}
