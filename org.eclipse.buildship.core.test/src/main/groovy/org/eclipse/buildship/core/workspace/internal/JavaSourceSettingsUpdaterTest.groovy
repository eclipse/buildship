package org.eclipse.buildship.core.workspace.internal

import com.gradleware.tooling.toolingmodel.OmniJavaRuntime
import com.gradleware.tooling.toolingmodel.OmniJavaSourceSettings
import com.gradleware.tooling.toolingmodel.OmniJavaVersion
import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.test.fixtures.EclipseProjects
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.launching.JavaRuntime
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

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
        JavaSourceSettingsUpdater.update(project, sourceSettings(sourceVersion, targetVersion), new NullProgressMonitor())

        then:
        project.getOption(JavaCore.COMPILER_COMPLIANCE, true) == sourceVersion
        project.getOption(JavaCore.COMPILER_SOURCE, true) == sourceVersion
        project.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true) == targetVersion

        where:
        sourceVersion | targetVersion
        '1.2'         | '1.2'
        '1.4'         | '1.5'
    }

    @SuppressWarnings("GroovyAccessibility")
    def "Invalid source setting results in runtime exception"() {
        given:
        IJavaProject project = EclipseProjects.newJavaProject('sample-project', tempFolder.newFolder())

        when:
        JavaSourceSettingsUpdater.update(project, sourceSettings(version, '1.3'), new NullProgressMonitor())

        then:
        thrown GradlePluginsRuntimeException

        when:
        JavaSourceSettingsUpdater.update(project, sourceSettings('1.4', version), new NullProgressMonitor())

        then:
        thrown GradlePluginsRuntimeException

        where:
        version << [null, '', '1.0.0', '7.8', 'string']
    }

    def "VM added to the project classpath if not exist"() {
        given:
        IJavaProject project = EclipseProjects.newJavaProject('sample-project', tempFolder.newFolder())
        def classpathWithoutVM = project.rawClasspath.findAll { !it.path.segment(0).equals(JavaRuntime.JRE_CONTAINER) }
        project.setRawClasspath(classpathWithoutVM as IClasspathEntry[], null)

        expect:
        !project.rawClasspath.find { it.path.segment(0).equals(JavaRuntime.JRE_CONTAINER) }

        when:
        JavaSourceSettingsUpdater.update(project, sourceSettings('1.6', '1.6'), new NullProgressMonitor())

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
        project.rawClasspath.find {
            it.path.toPortableString().equals('org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/custom')
        }

        when:
        JavaSourceSettingsUpdater.update(project, sourceSettings('1.6', '1.6'), new NullProgressMonitor())

        then:
        project.rawClasspath.find {
            it.path.toPortableString().startsWith('org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType')
        }
        !project.rawClasspath.find {
            it.path.toPortableString().equals('org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/custom')
        }
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

}
