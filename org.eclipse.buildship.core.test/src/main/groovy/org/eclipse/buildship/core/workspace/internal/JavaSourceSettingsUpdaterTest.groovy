package org.eclipse.buildship.core.workspace.internal

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import com.gradleware.tooling.toolingmodel.OmniJavaLanguageLevel
import com.gradleware.tooling.toolingmodel.OmniJavaSourceSettings
import com.gradleware.tooling.toolingmodel.OmniJavaVersion
import com.gradleware.tooling.toolingmodel.util.Maybe

import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.test.fixtures.EclipseProjects

class JavaSourceSettingsUpdaterTest extends Specification {

    @Rule
    TemporaryFolder tempFolder

    def cleanup() {
        CorePlugin.workspaceOperations().deleteAllProjects(new NullProgressMonitor())
    }

    def "If no source settings available then the compiler level remains untouched"() {
        given:
        IJavaProject project = Mock()

        when:
        JavaSourceSettingsUpdater.update(project, sourceSettings, new NullProgressMonitor())

        then:
        0 * project.setOption(*_)

        where:
        sourceSettings << [Maybe.of(null), Maybe.absent()]
    }

    def "Can set valid source settings"() {
        given:
        IJavaProject project = EclipseProjects.newJavaProject('sample-project', tempFolder.newFolder())

        when:
        JavaSourceSettingsUpdater.update(project, sourceSettings(version), new NullProgressMonitor())

        then:
        project.getOption(JavaCore.COMPILER_SOURCE, true) == version
        project.getOption(JavaCore.COMPILER_COMPLIANCE, true) == version
        project.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true) == version

        where:
        version << ['1.1', '1.2', '1.3', '1.4', '1.5', '1.6']
    }

    def "Setting invalid source settings falls back to highest version available in Eclipse"() {
        given:
        IJavaProject project = EclipseProjects.newJavaProject('sample-project', tempFolder.newFolder())

        when:
        JavaSourceSettingsUpdater.update(project, sourceSettings(version), new NullProgressMonitor())

        then:
        project.getOption(JavaCore.COMPILER_SOURCE, true) == highestAvailableVersionInRuntime
        project.getOption(JavaCore.COMPILER_COMPLIANCE, true) == highestAvailableVersionInRuntime
        project.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true) == highestAvailableVersionInRuntime

        where:
        version << [null, '', '1.0.0', '7.8', 'string']
    }

    private def sourceSettings(String sourceVersion) {
        OmniJavaVersion version = Mock()
        version.name >> sourceVersion
        OmniJavaLanguageLevel sourceLanguageLevel = Mock()
        sourceLanguageLevel.version >> version
        OmniJavaSourceSettings settings = Mock()
        settings.sourceLanguageLevel >> sourceLanguageLevel
        Maybe.of(settings)
    }

    private def getHighestAvailableVersionInRuntime() {
        new ArrayList(JavaSourceSettingsUpdater.availableJavaVersions).sort().last()
    }
}
