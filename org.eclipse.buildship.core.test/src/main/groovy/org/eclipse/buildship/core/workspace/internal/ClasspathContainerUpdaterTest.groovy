package org.eclipse.buildship.core.workspace.internal

import com.google.common.base.Optional

import com.gradleware.tooling.toolingmodel.OmniAccessRule
import com.gradleware.tooling.toolingmodel.OmniClasspathAttribute
import com.gradleware.tooling.toolingmodel.OmniEclipseClasspathContainer

import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.IAccessRule
import org.eclipse.jdt.core.IClasspathAttribute
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.launching.JavaRuntime

import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.workspace.GradleClasspathContainer

class ClasspathContainerUpdaterTest extends WorkspaceSpecification {

    static IPath CUSTOM_MODEL_CONTAINER = new Path('model.classpath.container')
    static IPath CUSTOM_USER_CONTAINER = new Path('user.classpath.container')
    static IPath STANDARD_JRE_CONTAINER = new Path('org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.8')
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

    def "Preserves manually added classpath containers"() {
        setup:
        IJavaProject project = newJavaProject('project-with-classpath-container')
        IAccessRule[] rules = [JavaCore.newAccessRule(new Path('accessiblePattern'), IAccessRule.K_ACCESSIBLE)]
        IClasspathAttribute[] attributes = [JavaCore.newClasspathAttribute('attributeKey', 'attributeValue')]
        IClasspathEntry[] classpath = project.rawClasspath + JavaCore.newContainerEntry(CUSTOM_USER_CONTAINER, rules, attributes, true)
        project.setRawClasspath(classpath, new NullProgressMonitor())

        when:
        executeContainerUpdate(project)

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

    def "Classpath containers that were previously defined manually are transformed to model elements"() {
        setup:
        IJavaProject project = newJavaProject('project-with-classpath-container')
        IClasspathEntry[] classpath = project.rawClasspath + JavaCore.newContainerEntry(CUSTOM_USER_CONTAINER, false)
        project.setRawClasspath(classpath, new NullProgressMonitor())

        when:
        executeContainerUpdate(project, container(CUSTOM_USER_CONTAINER))

        then:
        findContainer(project, CUSTOM_USER_CONTAINER)

        when:
        executeContainerUpdate(project)

        then:
        !findContainer(project, CUSTOM_USER_CONTAINER)
    }

    def "Adds Gradle classpath container by default"() {
        setup:
        IJavaProject project = newJavaProject('project-with-classpath-container')
        executeContainerUpdate(project)

        expect:
        findContainer(project, GradleClasspathContainer.CONTAINER_PATH)
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
        path << [ CUSTOM_MODEL_CONTAINER, GradleClasspathContainer.CONTAINER_PATH ]
    }

    def "Keeps user-defined JRE entries if model doesn't contain JRE"() {
        setup:
        IJavaProject project = newJavaProject('sample-project')
        def updatedClasspath = project.rawClasspath.findAll { !it.path.segment(0).equals(JavaRuntime.JRE_CONTAINER) }
        updatedClasspath += JavaCore.newContainerEntry(STANDARD_JRE_CONTAINER)
        updatedClasspath += JavaCore.newContainerEntry(CUSTOM_JRE_CONTAINER)
        project.setRawClasspath(updatedClasspath as IClasspathEntry[], null)

        expect:
        findContainer(project, STANDARD_JRE_CONTAINER)
        findContainer(project, CUSTOM_JRE_CONTAINER)

        when:
        executeContainerUpdate(project)

        then:
        findContainer(project, STANDARD_JRE_CONTAINER)
        findContainer(project, CUSTOM_JRE_CONTAINER)
    }

    def "Removes user-defined JRE entries if model contains JRE"() {
        setup:
        IJavaProject project = newJavaProject('project-with-classpath-container')
        def updatedClasspath = project.rawClasspath.findAll { !it.path.segment(0).equals(JavaRuntime.JRE_CONTAINER) }
        updatedClasspath += JavaCore.newContainerEntry(STANDARD_JRE_CONTAINER)
        updatedClasspath += JavaCore.newContainerEntry(CUSTOM_JRE_CONTAINER)
        project.setRawClasspath(updatedClasspath as IClasspathEntry[], null)

        expect:
        findContainer(project, STANDARD_JRE_CONTAINER)
        findContainer(project, CUSTOM_JRE_CONTAINER)

        when:
        def defaultJreContainerPath = JavaRuntime.newDefaultJREContainerPath()
        executeContainerUpdate(project, container(defaultJreContainerPath))

        then:
        findContainer(project, defaultJreContainerPath)
        !findContainer(project, STANDARD_JRE_CONTAINER)
        !findContainer(project, CUSTOM_JRE_CONTAINER)
    }

    private def executeContainerUpdate(IJavaProject project, OmniEclipseClasspathContainer... containers) {
        ClasspathContainerUpdater.update(project, Optional.of(Arrays.asList(containers)), new NullProgressMonitor())
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
