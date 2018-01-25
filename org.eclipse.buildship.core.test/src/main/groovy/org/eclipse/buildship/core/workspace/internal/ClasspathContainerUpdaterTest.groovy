package org.eclipse.buildship.core.workspace.internal

import com.google.common.base.Optional

import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.IAccessRule
import org.eclipse.jdt.core.IClasspathAttribute
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.launching.JavaRuntime

import org.eclipse.buildship.core.omnimodel.OmniAccessRule
import org.eclipse.buildship.core.omnimodel.OmniClasspathAttribute
import org.eclipse.buildship.core.omnimodel.OmniEclipseClasspathContainer
import org.eclipse.buildship.core.omnimodel.OmniJavaRuntime
import org.eclipse.buildship.core.omnimodel.OmniJavaSourceSettings
import org.eclipse.buildship.core.omnimodel.OmniJavaVersion
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.workspace.GradleClasspathContainer

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

        OmniClasspathAttribute attribute = Mock(OmniClasspathAttribute)
        attribute.getName() >> 'customname'
        attribute.getValue() >> 'customvalue'

        OmniAccessRule rule = Mock(OmniAccessRule)
        rule.getKind() >> IAccessRule.K_DISCOURAGED
        rule.getPattern() >> 'custompattern'

        executeContainerUpdate(project, container(path, true, Optional.of(Arrays.asList(attribute)), Optional.of(Arrays.asList(rule))))
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
        def containers = containerPaths.collect { container(it) } as OmniEclipseClasspathContainer[]
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
        ClasspathContainerUpdater.update(project, Optional.absent(), sourceSettings('1.6', '1.6'), new NullProgressMonitor())
    }

    private def executeContainerUpdate(IJavaProject project, OmniEclipseClasspathContainer... containers) {
        ClasspathContainerUpdater.update(project, Optional.of(Arrays.asList(containers)), sourceSettings('1.6', '1.6'), new NullProgressMonitor())
    }

    private OmniJavaSourceSettings sourceSettings(String sourceVersion, String targetVersion) {
        OmniJavaRuntime rt = Mock(OmniJavaRuntime)
        rt.homeDirectory >> new File(System.getProperty('java.home'))
        rt.javaVersion >> Mock(OmniJavaVersion)

        OmniJavaVersion target = Mock(OmniJavaVersion)
        target.name >> targetVersion

        OmniJavaVersion source = Mock(OmniJavaVersion)
        source.name >> sourceVersion

        OmniJavaSourceSettings settings = Mock(OmniJavaSourceSettings)
        settings.targetRuntime >> rt
        settings.targetBytecodeLevel >> target
        settings.sourceLanguageLevel >> source

        settings
    }

    private def container(IPath path, boolean exported = false, attributes = Optional.of([]), rules = Optional.of([])) {
        OmniEclipseClasspathContainer container = Mock(OmniEclipseClasspathContainer)
        container.getPath() >> path.toPortableString()
        container.exported >> exported
        container.getClasspathAttributes() >> attributes
        container.getAccessRules() >> rules
        container
    }

    private def findContainer(IJavaProject project, IPath path) {
        project.rawClasspath.find { it.entryKind == IClasspathEntry.CPE_CONTAINER && it.path == path }
    }
}
