package org.eclipse.buildship.core.workspace.internal

import org.gradle.api.JavaVersion
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import com.gradleware.tooling.toolingmodel.OmniJavaRuntime
import com.gradleware.tooling.toolingmodel.OmniJavaSourceSettings
import com.gradleware.tooling.toolingmodel.OmniJavaVersion
import com.gradleware.tooling.toolingmodel.util.Maybe

import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.internal.core.JavaModelManager
import org.eclipse.jdt.launching.JavaRuntime

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.test.fixtures.EclipseProjects

@SuppressWarnings("restriction")
class JavaSourceSettingsUpdaterTest extends Specification {

    @Rule
    TemporaryFolder tempFolder

    def cleanup() {
        CorePlugin.workspaceOperations().deleteAllProjects(new NullProgressMonitor())
    }

    def "Can set valid source settings"() {
        given:
        IJavaProject project = EclipseProjects.newJavaProject('sample-project', tempFolder.newFolder())

        when:
        JavaSourceSettingsUpdater.update(project, sourceSettings(runtimeVersion, targetVersion, sourceVersion), new NullProgressMonitor())

        then:
        project.getOption(JavaCore.COMPILER_COMPLIANCE, true) == runtimeVersion
        project.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true) == targetVersion
        project.getOption(JavaCore.COMPILER_SOURCE, true) == sourceVersion

        where:
        runtimeVersion | targetVersion | sourceVersion
        '1.2'          | '1.2'         | '1.2'
        '1.6'          | '1.5'         | '1.4'
    }

    def "Invalid compilance setting replaced with highest available Java version"() {
        given:
        IJavaProject project = EclipseProjects.newJavaProject('sample-project', tempFolder.newFolder())

        when:
        JavaSourceSettingsUpdater.update(project, sourceSettings(runtimeVersion, '1.3', '1.3'), new NullProgressMonitor())

        then:
        project.getOption(JavaCore.COMPILER_COMPLIANCE, true) == JavaSourceSettingsUpdater.availableJavaVersions[-1]

        where:
        runtimeVersion << [ null, '', '1.0.0', '7.8', 'string' ]
    }

    def "Invalid target Java version replaced with current compilance settings"() {
        given:
        IJavaProject project = EclipseProjects.newJavaProject('sample-project', tempFolder.newFolder())

        when:
        JavaSourceSettingsUpdater.update(project, sourceSettings('1.4', targetVersion, '1.6'), new NullProgressMonitor())

        then:
        project.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true) == '1.4'

        where:
        targetVersion << [ null, '', '1.0.0', '7.8', 'string', '1.5' ]
    }

    def "Invalid source Java version replaced with current target Java version"() {
        given:
        IJavaProject project = EclipseProjects.newJavaProject('sample-project', tempFolder.newFolder())

        when:
        JavaSourceSettingsUpdater.update(project, sourceSettings('1.6', '1.4', sourceVersion), new NullProgressMonitor())

        then:
        project.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true) == '1.4'

        where:
        sourceVersion << [ null, '', '1.0.0', '7.8', 'string', '1.5' ]
    }

    def "VM added to the project classpath if not exist"() {
        given:
        IJavaProject project = EclipseProjects.newJavaProject('sample-project', tempFolder.newFolder())
        def classpathWithoutVM = project.rawClasspath.findAll { !it.path.segment(0).equals(JavaRuntime.JRE_CONTAINER) }
        project.setRawClasspath(classpathWithoutVM as IClasspathEntry[], null)

        expect:
        !project.rawClasspath.find { it.path.segment(0).equals(JavaRuntime.JRE_CONTAINER) }

        when:
        JavaSourceSettingsUpdater.update(project, sourceSettings('1.6', '1.6', '1.6'), new NullProgressMonitor())

        then:
        project.rawClasspath.find { it.path.segment(0).equals(JavaRuntime.JRE_CONTAINER) }
    }

    def "Existing VM on the project classpath updated"() {
        given:
        IJavaProject project = EclipseProjects.newJavaProject('sample-project', tempFolder.newFolder())
        def updatedClasspath = project.rawClasspath.findAll { !it.path.segment(0).equals(JavaRuntime.JRE_CONTAINER) }
        updatedClasspath += JavaCore.newContainerEntry(new Path('org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/custom'))
        project.setRawClasspath(updatedClasspath as IClasspathEntry[], null)

        expect:
        project.rawClasspath.find { it.path.toPortableString().equals('org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/custom') }

        when:
        JavaSourceSettingsUpdater.update(project, sourceSettings('1.6', '1.6', '1.6'), new NullProgressMonitor())

        then:
        project.rawClasspath.find { it.path.toPortableString().startsWith('org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType') }
        !project.rawClasspath.find { it.path.toPortableString().equals('org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/custom') }
    }

    private OmniJavaSourceSettings sourceSettings(String runtimeVersion, String targetVersion, String sourceVersion) {
        OmniJavaVersion runtime = Mock()
        runtime.name >> runtimeVersion

        OmniJavaRuntime rt = Mock()
        rt.homeDirectory >> new File(System.getProperty('java.home'))
        rt.javaVersion >> runtime

        OmniJavaVersion target = Mock()
        target.name >> targetVersion

        OmniJavaVersion source = Mock()
        source.name >> sourceVersion

        OmniJavaSourceSettings settings = Mock()
        settings.targetRuntime >> rt
        settings.targetBytecodeLevel >> target
        settings.sourceLanguageLevel >> source

        settings
    }

}
