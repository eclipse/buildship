package org.eclipse.buildship.core.projectimport

import spock.lang.Unroll;

import com.gradleware.tooling.toolingmodel.repository.ModelRepository;
import com.gradleware.tooling.toolingmodel.repository.ModelRepositoryProvider;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectBuilder;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.gradle.GradleDistributionWrapper;
import org.eclipse.buildship.core.gradle.GradleDistributionWrapper.DistributionType;
import org.eclipse.buildship.core.workspace.WorkspaceOperations;

import org.junit.rules.TemporaryFolder;
import org.junit.Rule;
import spock.lang.Specification

class ProjectImportJobTest extends Specification {

    @Rule
    TemporaryFolder tempFolder

    def cleanup() {
        CorePlugin.workspaceOperations().deleteAllProjects(null)
    }

    def "Project import job creates a new project in the workspace"(boolean projectDescriptorExists) {
        setup:
        File rootProject = newProject(false, projectDescriptorExists)
        ProjectImportJob job = newProjectImportJob(rootProject)

        when:
        job.schedule()
        job.join()

        then:
        CorePlugin.workspaceOperations().findProjectByName(rootProject.name).present

        where:
        projectDescriptorExists << [false, true]
    }

    def "Project descriptors should be created if and only if they don't exist"(boolean applyJavaPlugin, boolean projectDescriptorExists, String descriptorComment) {
        setup:
        File rootProject = newProject(applyJavaPlugin, projectDescriptorExists)
        ProjectImportJob job = newProjectImportJob(rootProject)

        when:
        job.schedule()
        job.join()

        then:
        new File(rootProject, '.project').exists()
        new File(rootProject, '.classpath').exists() == applyJavaPlugin
        CorePlugin.workspaceOperations().findEclipseProject(rootProject).get().getComment() == descriptorComment

        where:
        applyJavaPlugin | projectDescriptorExists | descriptorComment
        false           | false                   | ''         // the comment from the generated descriptor
        false           | true                    | 'original' // the comment from the original descriptor
        true            | false                   | ''
        true            | true                    | 'original'
    }

    def "Imported projects always have Gradle builder and nature"(boolean projectDescriptorExists) {
        setup:
        File rootProject = newProject(false, projectDescriptorExists)
        ProjectImportJob job = newProjectImportJob(rootProject)

        when:
        job.schedule()
        job.join()
        def project = CorePlugin.workspaceOperations().findProjectByName(rootProject.name).get()

        then:
        GradleProjectNature.INSTANCE.isPresentOn(project)
        project.description.buildSpec.find { it.getBuilderName().equals(GradleProjectBuilder.INSTANCE.ID) }

        where:
        projectDescriptorExists << [false, true]
    }

    def newProject(boolean applyJavaPlugin, boolean projectDescriptorExists) {
        def root = tempFolder.newFolder("simple-project")
        def buildGradle = new File(root, "build.gradle")
        def sourceFile = new File(root, 'src/main/java/')
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

    def newProjectImportJob(File location) {
        ProjectImportConfiguration configuration = new ProjectImportConfiguration()
        configuration.setGradleDistribution(GradleDistributionWrapper.from(DistributionType.WRAPPER, ""))
        configuration.setProjectDir(location)
        new ProjectImportJob(configuration)
    }
}
