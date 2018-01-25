package org.eclipse.buildship.core.workspace.internal

import org.gradle.api.JavaVersion
import spock.lang.IgnoreIf

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.launching.JavaRuntime

import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.util.gradle.GradleDistribution
import org.eclipse.buildship.core.workspace.GradleClasspathContainer


class ClasspathContainerOrderingTest extends ProjectSynchronizationSpecification {

    static IClasspathEntry JRE_CONTAINER = JavaRuntime.defaultJREContainerEntry
    static IClasspathEntry GRADLE_CONTAINER = JavaCore.newContainerEntry(GradleClasspathContainer.CONTAINER_PATH)
    static IClasspathEntry CUSTOM_CONTAINER = JavaCore.newContainerEntry(new Path('custom.container'))
    static IClasspathEntry SOURCE_FOLDER = JavaCore.newSourceEntry(new Path('/sample-project/src/main/java'))

    @IgnoreIf({ JavaVersion.current().isJava9Compatible() }) // https://github.com/eclipse/buildship/issues/601
    def "Without source folders and using old Gradle Version"() {
        setup:
        File location = dir('sample-project') {
            file('build.gradle') << "apply plugin: 'java'"
        }

        when:
        importAndWait(location, GradleDistribution.forVersion('2.14.1'))
        IProject project = findProject('sample-project')

        then:
        assertClasspatContent(project, JRE_CONTAINER, GRADLE_CONTAINER)

        when:
        synchronizeAndWait(project)

        then:
        assertClasspatContent(project, JRE_CONTAINER, GRADLE_CONTAINER)
    }

    @IgnoreIf({ JavaVersion.current().isJava9Compatible() }) // https://github.com/eclipse/buildship/issues/601
    def "With source folders and using old Gradle version"() {
        setup:
        File location = dir('sample-project') {
            file('build.gradle') << "apply plugin: 'java'"
            dir('src/main/java').mkdirs()
        }

        when:
        importAndWait(location, GradleDistribution.forVersion('2.14.1'))
        IProject project = findProject('sample-project')

        then:
        assertClasspatContent(project, SOURCE_FOLDER, JRE_CONTAINER, GRADLE_CONTAINER)

        when:
        synchronizeAndWait(project)

        then:
        assertClasspatContent(project, SOURCE_FOLDER, JRE_CONTAINER, GRADLE_CONTAINER)
    }

    @IgnoreIf({ !JavaVersion.current().isJava7Compatible() })
    def "Without source folders"() {
        setup:
        File location = dir('sample-project') {
            file('build.gradle') << "apply plugin: 'java'"
        }

        when:
        importAndWait(location)
        IProject project = findProject('sample-project')

        then:
        assertClasspatContent(project, JRE_CONTAINER, GRADLE_CONTAINER)

        when:
        synchronizeAndWait(project)

        then:
        assertClasspatContent(project, JRE_CONTAINER, GRADLE_CONTAINER)
    }

    @IgnoreIf({ !JavaVersion.current().isJava7Compatible() })
    def "With source folders"() {
        setup:
        File location = dir('sample-project') {
            file('build.gradle') << "apply plugin: 'java'"
            dir('src/main/java').mkdirs()
        }

        when:
        importAndWait(location)
        IProject project = findProject('sample-project')

        then:
        assertClasspatContent(project, SOURCE_FOLDER, JRE_CONTAINER, GRADLE_CONTAINER)

        when:
        synchronizeAndWait(project)

        then:
        assertClasspatContent(project, SOURCE_FOLDER, JRE_CONTAINER, GRADLE_CONTAINER)
    }

    @IgnoreIf({ !JavaVersion.current().isJava7Compatible() })
    def "With custom container"() {
        setup:
        File location = dir('sample-project') {
            file('build.gradle') << """
                apply plugin: 'java'
                apply plugin: 'eclipse'

                eclipse.classpath.containers 'custom.container'
            """
        }

        when:
        importAndWait(location)
        IProject project = findProject('sample-project')

        then:
        assertClasspatContent(project, JRE_CONTAINER, CUSTOM_CONTAINER, GRADLE_CONTAINER)

        when:
        synchronizeAndWait(project)

        then:
        assertClasspatContent(project, JRE_CONTAINER, CUSTOM_CONTAINER, GRADLE_CONTAINER)
    }

    @IgnoreIf({ !JavaVersion.current().isJava7Compatible() })
    def "With custom container order"() {
        setup:
        File location = dir('sample-project') {
            file('build.gradle') << """
                apply plugin: 'java'
                apply plugin: 'eclipse'

                eclipse.classpath.containers 'org.eclipse.buildship.core.gradleclasspathcontainer', 'custom.container'
            """
        }

        when:
        importAndWait(location)
        IProject project = findProject('sample-project')

        then:
        assertClasspatContent(project, JRE_CONTAINER, GRADLE_CONTAINER, CUSTOM_CONTAINER)

        when:
        synchronizeAndWait(project)

        then:
        assertClasspatContent(project, JRE_CONTAINER, GRADLE_CONTAINER, CUSTOM_CONTAINER)
    }

    @IgnoreIf({ !JavaVersion.current().isJava7Compatible() })
    def "Jre removed from model" () {
        setup:
        File location = dir('sample-project') {
            file('build.gradle') << """
                apply plugin: 'java'
                apply plugin: 'eclipse'

                eclipse.classpath.file.whenMerged {
                    entries.removeAll { it.path.startsWith('org.eclipse.jdt.launching') }
                }
            """
        }

        when:
        importAndWait(location)
        IProject project = findProject('sample-project')

        then:
        assertClasspatContent(project, GRADLE_CONTAINER)

        when:
        synchronizeAndWait(project)

        then:
        assertClasspatContent(project, GRADLE_CONTAINER)
    }

    @IgnoreIf({ !JavaVersion.current().isJava7Compatible() })
    def "Jre removed from project"() {
        setup:
        File location = dir('sample-project') {
            file('build.gradle') << "apply plugin: 'java'"
        }

        when:
        importAndWait(location)
        IProject project = findProject('sample-project')

        then:
        assertClasspatContent(project, JRE_CONTAINER, GRADLE_CONTAINER)

        when:
        removeJreFromClasspath(project)
        synchronizeAndWait(project)

        then:
        assertClasspatContent(project, JRE_CONTAINER, GRADLE_CONTAINER)
    }

    @IgnoreIf({ !JavaVersion.current().isJava7Compatible() })
    def "Jre removed from the model and from project" () {
        setup:
        File location = dir('sample-project') {
            file('build.gradle') << "apply plugin: 'java'"
        }

        when:
        importAndWait(location)
        IProject project = findProject('sample-project')

        then:
        assertClasspatContent(project, JRE_CONTAINER, GRADLE_CONTAINER)

        when:
        removeJreFromClasspath(project)
        file('sample-project/build.gradle') << """
            apply plugin: 'eclipse'

            eclipse.classpath.file.whenMerged {
                entries.removeAll { it.path.startsWith('org.eclipse.jdt.launching') }
            }
        """

        synchronizeAndWait(project)

        then:
        assertClasspatContent(project, GRADLE_CONTAINER)
    }

    @IgnoreIf({ JavaVersion.current().isJava9Compatible() }) // https://github.com/eclipse/buildship/issues/601
    def "Jre removed from project and using old Gradle version"() {
        setup:
        File location = dir('sample-project') {
            file('build.gradle') << "apply plugin: 'java'"
        }

        when:
        importAndWait(location, GradleDistribution.forVersion('2.14.1'))
        IProject project = findProject('sample-project')

        then:
        assertClasspatContent(project, JRE_CONTAINER, GRADLE_CONTAINER)

        when:
        removeJreFromClasspath(project)
        synchronizeAndWait(project)

        then:
        assertClasspatContent(project, JRE_CONTAINER, GRADLE_CONTAINER)
    }

    private void assertClasspatContent(IProject project, IClasspathEntry... expectedEntries) {
        def actualEntries = JavaCore.create(project).rawClasspath
        assert expectedEntries.length == actualEntries.length
        for (i in 0..<expectedEntries.length) {
            assert expectedEntries[i].entryKind == actualEntries[i].entryKind
            if (expectedEntries[i] == JRE_CONTAINER) {
                assert JRE_CONTAINER.path.isPrefixOf(actualEntries[i].path)
            } else {
                assert expectedEntries[i].path == actualEntries[i].path
            }
        }
    }

    private void removeJreFromClasspath(IProject project) {
        IJavaProject javaProject = JavaCore.create(project)
        IClasspathEntry[] entries = javaProject.rawClasspath.findAll { !JRE_CONTAINER.path.isPrefixOf(it.path) }
        javaProject.setRawClasspath(entries, null)
    }
}
