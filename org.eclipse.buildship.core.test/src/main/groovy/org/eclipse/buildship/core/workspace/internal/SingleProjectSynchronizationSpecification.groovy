package org.eclipse.buildship.core.workspace.internal

import org.gradle.api.JavaVersion
import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.configuration.internal.ProjectConfigurationPersistence
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.test.fixtures.EclipseProjects
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification;
import org.eclipse.buildship.core.workspace.ExistingDescriptorHandler
import org.eclipse.buildship.core.workspace.GradleClasspathContainer
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResourceFilterDescription
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.JavaModelException;

abstract class SingleProjectSynchronizationSpecification extends ProjectSynchronizationSpecification {

    protected abstract void prepareProject(String name)

    protected abstract void prepareJavaProject(String name)

    def "The Gradle nature is set"() {
        setup:
        prepareProject('sample-project')
        def projectDir = dir('sample-project') {
            file 'settings.gradle'
        }

        when:
        synchronizeAndWait(projectDir)

        expect:

        then:
        def project = findProject('sample-project')
        project.hasNature(GradleProjectNature.ID)
    }

    def "Natures and build commands are set"() {
        setup:
        prepareProject('sample-project')
        def projectDir = dir('sample-project') {
            file 'build.gradle', """
                apply plugin: 'eclipse'
                eclipse {
                    project {
                        natures << "org.eclipse.pde.UpdateSiteNature"
                        buildCommand 'customBuildCommand', buildCommandKey: "buildCommandValue"
                    }
                }
            """
        }

        when:
        synchronizeAndWait(projectDir)

        then:
        def project = findProject('sample-project')
        project.description.natureIds.find{ it == 'org.eclipse.pde.UpdateSiteNature' }
        project.description.buildSpec.find{ it.builderName == 'customBuildCommand' }.arguments == ['buildCommandKey' : "buildCommandValue"]
    }

    def "The Gradle settings file is written"() {
        setup:
        prepareProject('sample-project')
        def projectDir = dir('sample-project') {
            file 'settings.gradle'
        }

        when:
        synchronizeAndWait(projectDir)

        then:
        def project = findProject('sample-project')
        new ProjectConfigurationPersistence().readProjectConfiguration(project)
    }

    def "Resource filters are set"() {
        setup:
        prepareProject('sample-project')
        def projectDir = dir('sample-project') {
            file 'settings.gradle'
        }

        when:
        synchronizeAndWait(projectDir)

        then:
        def project = findProject('sample-project')
        project.filters.length == 2
        project.filters[0].type == IResourceFilterDescription.EXCLUDE_ALL.or(IResourceFilterDescription.FOLDERS).or(IResourceFilterDescription.INHERITABLE)
        project.filters[1].type == IResourceFilterDescription.EXCLUDE_ALL.or(IResourceFilterDescription.FOLDERS).or(IResourceFilterDescription.INHERITABLE)
        project.filters[0].fileInfoMatcherDescription.id == 'org.eclipse.ui.ide.multiFilter'
        project.filters[1].fileInfoMatcherDescription.id == 'org.eclipse.ui.ide.multiFilter'
        (project.filters[0].fileInfoMatcherDescription.arguments as String).endsWith("build")
        (project.filters[1].fileInfoMatcherDescription.arguments as String).endsWith(".gradle")
    }

    def "Linked resources are set"() {
        setup:
        prepareProject('sample-project')
        def projectDir = dir('sample-project') {
            file 'build.gradle',
            '''
                apply plugin: "java"
                sourceSets { main { java { srcDir '../another-project/src' } } }
            '''
            dir '../another-project/src'
        }

        when:
        synchronizeAndWait(projectDir)

        then:
        def project = findProject('sample-project')
        project.getFolder('src').isLinked()
    }

    def "Source settings are updated"() {
        setup:
        prepareJavaProject('sample-project')
        def projectDir = dir('sample-project') {
            file 'build.gradle', """
                apply plugin: 'java'
                sourceCompatibility = 1.2
                targetCompatibility = 1.3
            """
            dir 'src/main/java'
        }

        when:
        synchronizeAndWait(projectDir)

        then:
        def javaProject = JavaCore.create(findProject('sample-project'))
        javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true) == JavaVersion.current().toString()
        javaProject.getOption(JavaCore.COMPILER_SOURCE, true) == JavaCore.VERSION_1_2
        javaProject.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true) == JavaCore.VERSION_1_3
    }

    def "Source folders are updated"() {
        setup:
        prepareJavaProject('sample-project')
        def projectDir = dir('sample-project') {
            file 'build.gradle', 'apply plugin: "java"'
            dir 'src/main/java'
        }

        when:
        synchronizeAndWait(projectDir)

        then:
        def javaProject = JavaCore.create(findProject('sample-project'))
        javaProject.rawClasspath.find{
            it.entryKind == IClasspathEntry.CPE_SOURCE &&
            it.path.toPortableString() == '/sample-project/src/main/java' &&
            it.extraAttributes.length == 1 &&
            it.extraAttributes[0].name == "FROM_GRADLE_MODEL"
        }
    }

    def "If the project applies the java plugin, then it's converted to a Java project"() {
        setup:
        prepareProject('sample-project')
        def projectDir = dir('sample-project') {
            file 'build.gradle', 'apply plugin: "java"'
            dir 'src/main/java'
        }

        when:
        synchronizeAndWait(projectDir)

        then:
        def project = findProject("sample-project")
        project.hasNature(JavaCore.NATURE_ID)
    }

    def "If the project applies the Java plugin, then the Gradle classpath container is added"() {
        setup:
        prepareProject("sample-project")
        def projectDir = dir('sample-project') {
            file 'build.gradle', 'apply plugin: "java"'
            dir 'src/main/java'
        }

        when:
        synchronizeAndWait(projectDir)

        then:
        def project = findProject('sample-project')
        JavaCore.create(project).rawClasspath.find{
            it.entryKind == IClasspathEntry.CPE_CONTAINER &&
            it.path.toPortableString() == GradleClasspathContainer.CONTAINER_ID
        }
    }
}
