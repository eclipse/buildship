package org.eclipse.buildship.core.internal.workspace.impl

import org.gradle.api.JavaVersion
import org.gradle.tooling.model.eclipse.AccessRule
import org.gradle.tooling.model.eclipse.ClasspathAttribute
import org.gradle.tooling.model.eclipse.EclipseClasspathContainer
import org.gradle.tooling.model.eclipse.EclipseJavaSourceSettings
import org.gradle.tooling.model.eclipse.EclipseProject
import org.gradle.tooling.model.java.InstalledJdk

import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.IAccessRule
import org.eclipse.jdt.core.IClasspathAttribute
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.launching.JavaRuntime

import org.eclipse.buildship.core.internal.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.internal.util.gradle.CompatEclipseProject
import org.eclipse.buildship.core.internal.util.gradle.ModelUtils
import org.eclipse.buildship.core.internal.workspace.GradleClasspathContainer

class ClasspathContainerUpdaterTest extends WorkspaceSpecification {

    static IPath CUSTOM_MODEL_CONTAINER = new Path('model.classpath.container')
    static IPath CUSTOM_USER_CONTAINER = new Path('user.classpath.container')
    static IPath GRADLE_CLASSPATH_CONTAINER = GradleClasspathContainer.CONTAINER_PATH
    static IPath DEFAULT_JRE_CONTAINER = JavaRuntime.newDefaultJREContainerPath()
    static IPath DEFAULT_JAVA_8_CONTAINER = new Path('org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.8')
    static IPath CUSTOM_JRE_CONTAINER = new Path('org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.launching.macosx.MacOSXType/Java SE 6 [1.6.0_65-b14-462]')

    def "Can set classpath containers"() {
        setup:
        IJavaProject project = newJavaProject('project-with-classpath-container')

        expect:
        !findContainer(project, CUSTOM_MODEL_CONTAINER)

        when:
        executeContainerUpdate(project, container(CUSTOM_MODEL_CONTAINER))
        IClasspathEntry container = findContainer(project, CUSTOM_MODEL_CONTAINER)

        then:
        container.path == CUSTOM_MODEL_CONTAINER
        !container.isExported()
        container.accessRules.length == 0
        container.extraAttributes.length == 0
    }

    def "Removes classpath containers if they no longer exist in the Gradle model"() {
        setup:
        IJavaProject project = newJavaProject('project-with-classpath-container')
        executeContainerUpdate(project, container(CUSTOM_MODEL_CONTAINER))

        expect:
        findContainer(project, CUSTOM_MODEL_CONTAINER)

        when:
        executeContainerUpdate(project)

        then:
        !findContainer(project, CUSTOM_MODEL_CONTAINER)
    }

    def "Overwrites manually added classpath containers if model contains container information"() {
        setup:
        IJavaProject project = newJavaProject('project-with-classpath-container')
        IAccessRule[] rules = [JavaCore.newAccessRule(new Path('accessiblePattern'), IAccessRule.K_ACCESSIBLE)]
        IClasspathAttribute[] attributes = [JavaCore.newClasspathAttribute('attributeKey', 'attributeValue')]
        IClasspathEntry[] classpath = project.rawClasspath + JavaCore.newContainerEntry(CUSTOM_USER_CONTAINER, rules, attributes, true)
        project.setRawClasspath(classpath, new NullProgressMonitor())

        when:
        executeContainerUpdate(project)

        then:
        !findContainer(project, CUSTOM_USER_CONTAINER)
    }

    def "Preserves manually added classpath containers if model contains no container information"() {
        setup:
        IJavaProject project = newJavaProject('project-with-classpath-container')
        IAccessRule[] rules = [JavaCore.newAccessRule(new Path('accessiblePattern'), IAccessRule.K_ACCESSIBLE)]
        IClasspathAttribute[] attributes = [JavaCore.newClasspathAttribute('attributeKey', 'attributeValue')]
        IClasspathEntry[] classpath = project.rawClasspath + JavaCore.newContainerEntry(CUSTOM_USER_CONTAINER, rules, attributes, true)
        project.setRawClasspath(classpath, new NullProgressMonitor())

        when:
        executeContainerUpdateWithOldGradle(project)

        then:
        IClasspathEntry container = findContainer(project, CUSTOM_USER_CONTAINER)
        container != null
        container.exported == true
        container.accessRules.length == 1
        container.accessRules[0].pattern.toPortableString() == 'accessiblePattern'
        container.accessRules[0].kind == IAccessRule.K_ACCESSIBLE
        container.extraAttributes.length == 1
        container.extraAttributes[0].name == 'attributeKey'
        container.extraAttributes[0].value == 'attributeValue'
    }

    def "Adds Gradle classpath container by default"() {
        setup:
        IJavaProject project = newJavaProject('project-with-classpath-container')
        executeContainerUpdate(project)

        expect:
        findContainer(project, GRADLE_CLASSPATH_CONTAINER)
    }

    def "Respects custom classpath container features"() {
        setup:
        IJavaProject project = newJavaProject('project-with-classpath-container')

        ClasspathAttribute attribute = Mock(ClasspathAttribute)
        attribute.getName() >> 'customname'
        attribute.getValue() >> 'customvalue'

        AccessRule rule = Mock(AccessRule)
        rule.getKind() >> IAccessRule.K_DISCOURAGED
        rule.getPattern() >> 'custompattern'

        executeContainerUpdate(project, container(path, true, [attribute], [rule]))
        IClasspathEntry entry = findContainer(project, path)

        expect:
        entry.getPath() == path
        entry.isExported()
        entry.extraAttributes.length == 1
        entry.extraAttributes[0].name == 'customname'
        entry.extraAttributes[0].value == 'customvalue'
        entry.accessRules.length == 1
        entry.accessRules[0].kind == IAccessRule.K_DISCOURAGED
        entry.accessRules[0].pattern.toPortableString() == 'custompattern'

        where:
        path << [ CUSTOM_MODEL_CONTAINER, GRADLE_CLASSPATH_CONTAINER ]
    }

    def "Updates JRE entry based on source level if containers are not supported"() {
        setup:
        IJavaProject project = newJavaProject('sample-project')
        def updatedClasspath = project.rawClasspath.findAll { !it.path.segment(0).equals(JavaRuntime.JRE_CONTAINER) }
        updatedClasspath += JavaCore.newContainerEntry(DEFAULT_JAVA_8_CONTAINER)
        updatedClasspath += JavaCore.newContainerEntry(CUSTOM_JRE_CONTAINER)
        project.setRawClasspath(updatedClasspath as IClasspathEntry[], null)

        expect:
        findContainer(project, DEFAULT_JAVA_8_CONTAINER)
        findContainer(project, CUSTOM_JRE_CONTAINER)

        when:
        executeContainerUpdateWithOldGradle(project)

        then:
        findContainer(project, new Path('org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.6'))
        !findContainer(project, DEFAULT_JAVA_8_CONTAINER)
        !findContainer(project, CUSTOM_JRE_CONTAINER)
    }

    def "Removes user-defined JRE entries if model doesn't contain JRE"() {
        setup:
        IJavaProject project = newJavaProject('sample-project')
        def updatedClasspath = project.rawClasspath.findAll { !it.path.segment(0).equals(JavaRuntime.JRE_CONTAINER) }
        updatedClasspath += JavaCore.newContainerEntry(DEFAULT_JAVA_8_CONTAINER)
        updatedClasspath += JavaCore.newContainerEntry(CUSTOM_JRE_CONTAINER)
        project.setRawClasspath(updatedClasspath as IClasspathEntry[], null)

        expect:
        findContainer(project, DEFAULT_JAVA_8_CONTAINER)
        findContainer(project, CUSTOM_JRE_CONTAINER)

        when:
        executeContainerUpdate(project)

        then:
        !findContainer(project, DEFAULT_JAVA_8_CONTAINER)
        !findContainer(project, CUSTOM_JRE_CONTAINER)
    }

    def "Removes user-defined JRE entries if model contains JRE"() {
        setup:
        IJavaProject project = newJavaProject('project-with-classpath-container')
        def updatedClasspath = project.rawClasspath.findAll { !it.path.segment(0).equals(JavaRuntime.JRE_CONTAINER) }
        updatedClasspath += JavaCore.newContainerEntry(DEFAULT_JAVA_8_CONTAINER)
        updatedClasspath += JavaCore.newContainerEntry(CUSTOM_JRE_CONTAINER)
        project.setRawClasspath(updatedClasspath as IClasspathEntry[], null)

        expect:
        findContainer(project, DEFAULT_JAVA_8_CONTAINER)
        findContainer(project, CUSTOM_JRE_CONTAINER)

        when:
        executeContainerUpdate(project, container(DEFAULT_JRE_CONTAINER))

        then:
        findContainer(project, DEFAULT_JRE_CONTAINER)
        !findContainer(project, DEFAULT_JAVA_8_CONTAINER)
        !findContainer(project, CUSTOM_JRE_CONTAINER)
    }

    def "Model containers should come after the source folders"() {
        setup:
        IJavaProject project = newJavaProject('project-with-classpath-container')

        when:
        executeContainerUpdate(project, container(CUSTOM_MODEL_CONTAINER))
        int sourceIndex = project.rawClasspath.findIndexOf { it.entryKind == IClasspathEntry.CPE_SOURCE }
        int containerIndex = project.rawClasspath.findIndexOf { it.path == CUSTOM_MODEL_CONTAINER }

        then:
        sourceIndex + 1 == containerIndex
    }

    def "Model containers should be at beginning of classpath if no source folders exist"() {
        setup:
        IJavaProject project = newJavaProject('project-with-classpath-container')
        project.setRawClasspath(project.rawClasspath - project.rawClasspath.findAll { it.entryKind == IClasspathEntry.CPE_SOURCE } as IClasspathEntry[], null)

        when:
        executeContainerUpdate(project, container(CUSTOM_MODEL_CONTAINER))

        then:
        project.rawClasspath.findIndexOf { it.path == CUSTOM_MODEL_CONTAINER } == 0
    }

    def "JRE container is set between source folders and Gradle classpath container"() {
        setup:
        IJavaProject project = newJavaProject('project-with-classpath-container')
        project.setRawClasspath(project.rawClasspath + JavaCore.newContainerEntry(GRADLE_CLASSPATH_CONTAINER) as IClasspathEntry[], null)

        when:
        executeContainerUpdate(project, container(DEFAULT_JRE_CONTAINER))

        then:
        project.rawClasspath.length == 3
        project.rawClasspath[0].entryKind == IClasspathEntry.CPE_SOURCE
        project.rawClasspath[1].path == DEFAULT_JRE_CONTAINER
        project.rawClasspath[2].path == GRADLE_CLASSPATH_CONTAINER
    }

    def "Container ordering respected on the classpath"(List<IPath> containerPaths) {
        setup:
        IJavaProject project = newJavaProject('project-with-classpath-container')
        project.setRawClasspath(project.rawClasspath + JavaCore.newContainerEntry(CUSTOM_USER_CONTAINER) as IClasspathEntry[], null)

        when:
        def containers = containerPaths.collect { container(it) } as EclipseClasspathContainer[]
        executeContainerUpdate(project, containers)
        def containerIndexes = containerPaths.collect { path -> project.rawClasspath.findIndexOf { it.path == path } ?: -1 }

        then:
        containerIndexes.every { it >= 0 }
        containerIndexes == containerIndexes.toSorted()

        where:
        containerPaths << [
            [ DEFAULT_JAVA_8_CONTAINER, GRADLE_CLASSPATH_CONTAINER ],
            [ GRADLE_CLASSPATH_CONTAINER, DEFAULT_JAVA_8_CONTAINER ],
            [ CUSTOM_USER_CONTAINER, DEFAULT_JAVA_8_CONTAINER, CUSTOM_MODEL_CONTAINER ],
            [ DEFAULT_JAVA_8_CONTAINER, CUSTOM_USER_CONTAINER, CUSTOM_MODEL_CONTAINER ],
            [ CUSTOM_USER_CONTAINER, CUSTOM_MODEL_CONTAINER, DEFAULT_JAVA_8_CONTAINER ],
        ]
    }

    def "VM added to the project classpath if not exist"() {
        given:
        IJavaProject project = newJavaProject('sample-project')
        def classpathWithoutVM = project.rawClasspath.findAll { !it.path.segment(0).equals(JavaRuntime.JRE_CONTAINER) }
        project.setRawClasspath(classpathWithoutVM as IClasspathEntry[], null)

        expect:
        !project.rawClasspath.find { it.path.segment(0).equals(JavaRuntime.JRE_CONTAINER) }

        when:
        executeContainerUpdateWithOldGradle(project)

        then:
        project.rawClasspath.find { it.path.segment(0).equals(JavaRuntime.JRE_CONTAINER) }
    }

    def "Existing VM on the project classpath updated"() {
        given:
        IJavaProject project = newJavaProject('sample-project')
        def updatedClasspath = project.rawClasspath.findAll { !it.path.segment(0).equals(JavaRuntime.JRE_CONTAINER) }
        updatedClasspath += JavaCore.newContainerEntry(new Path('org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/custom'))
        project.setRawClasspath(updatedClasspath as IClasspathEntry[], null)

        expect:
        project.rawClasspath.find {
            it.path.toPortableString().equals('org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/custom')
        }

        when:
        executeContainerUpdateWithOldGradle(project)

        then:
        project.rawClasspath.find {
            it.path.toPortableString().startsWith('org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType')
        }
        !project.rawClasspath.find {
            it.path.toPortableString().equals('org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/custom')
        }
    }

    private def executeContainerUpdateWithOldGradle(IJavaProject project) {
        EclipseProject eclipseProject = Mock(EclipseProject)
        eclipseProject.getClasspathContainers() >> CompatEclipseProject.UNSUPPORTED_CONTAINERS
        eclipseProject.getJavaSourceSettings() >> sourceSettings('1.6', '1.6')
        ClasspathContainerUpdater.update(project, eclipseProject, new NullProgressMonitor())
    }

    private def executeContainerUpdate(IJavaProject project, EclipseClasspathContainer... containers) {
        EclipseProject eclipseProject = Mock(EclipseProject)
        eclipseProject.getClasspathContainers() >> ModelUtils.asDomainObjectSet(containers as List)
        eclipseProject.getJavaSourceSettings() >> sourceSettings('1.6', '1.6')
        ClasspathContainerUpdater.update(project, eclipseProject, new NullProgressMonitor())
    }

    private EclipseJavaSourceSettings sourceSettings(String sourceVersion, String targetVersion) {
        InstalledJdk jdk = Mock(InstalledJdk)
        jdk.javaHome >> new File(System.getProperty('java.home'))
        jdk.javaVersion >> JavaVersion.current()

        EclipseJavaSourceSettings settings = Mock(EclipseJavaSourceSettings)
        settings.jdk >> jdk
        settings.sourceLanguageLevel >> JavaVersion.toVersion(sourceVersion)
        settings.targetBytecodeVersion >> JavaVersion.toVersion(targetVersion)

        settings
    }

    private def container(IPath path, boolean exported = false, attributes = [], rules = []) {
        EclipseClasspathContainer container = Mock(EclipseClasspathContainer)
        container.getPath() >> path.toPortableString()
        container.exported >> exported

        container.getClasspathAttributes() >> ModelUtils.asDomainObjectSet(attributes)
        container.getAccessRules() >> ModelUtils.asDomainObjectSet(rules)
        container
    }

    private def findContainer(IJavaProject project, IPath path) {
        project.rawClasspath.find { it.entryKind == IClasspathEntry.CPE_CONTAINER && it.path == path }
    }
}
