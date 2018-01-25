package org.eclipse.buildship.core.workspace.internal

import com.google.common.base.Optional

import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.jobs.IJobChangeEvent
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.core.runtime.jobs.JobChangeAdapter
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.launching.JavaRuntime

import org.eclipse.buildship.core.omnimodel.OmniEclipseProject
import org.eclipse.buildship.core.omnimodel.OmniJavaRuntime
import org.eclipse.buildship.core.omnimodel.OmniJavaSourceSettings
import org.eclipse.buildship.core.omnimodel.OmniJavaVersion
import org.eclipse.buildship.core.test.fixtures.LegacyEclipseSpockTestHelper
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification

class JavaSourceSettingsUpdaterTest extends WorkspaceSpecification {

    def "Can set valid source settings"() {
        given:
        IJavaProject project = newJavaProject('sample-project')

        when:
        JavaSourceSettingsUpdater.update(project, modelProject(sourceVersion, targetVersion), new NullProgressMonitor())

        then:
        project.getOption(JavaCore.COMPILER_COMPLIANCE, true) == sourceVersion
        project.getOption(JavaCore.COMPILER_SOURCE, true) == sourceVersion
        project.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true) == targetVersion

        where:
        sourceVersion | targetVersion
        '1.2'         | '1.2'
        '1.4'         | '1.5'
    }

    def "Invalid source settings are written as-is"() {
        given:
        IJavaProject project = newJavaProject('sample-project')

        when:
        JavaSourceSettingsUpdater.update(project, modelProject(version, version), new NullProgressMonitor())

        then:
        project.getOption(JavaCore.COMPILER_COMPLIANCE, true) == version
        project.getOption(JavaCore.COMPILER_SOURCE, true) == version
        project.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true) == version

        where:
        version << ['', '1.0.0', '7.8', 'string', '1.5']
    }



    def "If Tooling API supports classpath containers then VMs are left unchanged"() {
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
        JavaSourceSettingsUpdater.update(project, modelProject('1.6', '1.6', Optional.of([])), new NullProgressMonitor())

        then:
        project.rawClasspath.find {
            it.path.toPortableString().equals('org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/custom')
        }
    }

    def "A project is rebuilt if the source settings have changed"() {
        setup:
        IJavaProject javaProject = newJavaProject('sample-project')
        def buildScheduledListener = new BuildJobScheduledByJavaSourceSettingsUpdater()
        Job.jobManager.addJobChangeListener(buildScheduledListener)

        when:
        JavaSourceSettingsUpdater.update(javaProject, modelProject('1.4', '1.4'), new NullProgressMonitor())

        then:
        buildScheduledListener.isBuildScheduled()

        cleanup:
        Job.jobManager.removeJobChangeListener(buildScheduledListener)
    }

    def "A project is not rebuilt if source settings have not changed"() {
        setup:
        IJavaProject javaProject = newJavaProject('sample-project')
        JavaSourceSettingsUpdater.update(javaProject, modelProject('1.4', '1.4'), new NullProgressMonitor())
        def buildScheduledListener = new BuildJobScheduledByJavaSourceSettingsUpdater()
        Job.jobManager.addJobChangeListener(buildScheduledListener)

        when:
        JavaSourceSettingsUpdater.update(javaProject, modelProject('1.4', '1.4'), new NullProgressMonitor())

        then:
        !buildScheduledListener.isBuildScheduled()

        cleanup:
        Job.jobManager.removeJobChangeListener(buildScheduledListener)
    }

    def "A project is not rebuilt if the 'Build Automatically' setting is disabled"() {
        setup:
        IJavaProject javaProject = newJavaProject('sample-project')
        def buildScheduledListener = new BuildJobScheduledByJavaSourceSettingsUpdater()
        Job.jobManager.addJobChangeListener(buildScheduledListener)
        def description = LegacyEclipseSpockTestHelper.workspace.description
        def wasAutoBuilding = description.autoBuilding
        description.autoBuilding = false
        LegacyEclipseSpockTestHelper.workspace.description = description

        when:
        JavaSourceSettingsUpdater.update(javaProject, modelProject('1.4', '1.4'), new NullProgressMonitor())

        then:
        !buildScheduledListener.isBuildScheduled()

        cleanup:
        description.autoBuilding = wasAutoBuilding
        LegacyEclipseSpockTestHelper.workspace.description = description
        Job.jobManager.removeJobChangeListener(buildScheduledListener)
    }

    private OmniEclipseProject modelProject(String sourceVersion, String targetVersion, Optional classpathContainers = Optional.absent()) {
        OmniEclipseProject project = Mock(OmniEclipseProject)
        project.getJavaSourceSettings() >> Optional.of(sourceSettings(sourceVersion, targetVersion))
        project.getClasspathContainers() >> classpathContainers
        project
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

    static class BuildJobScheduledByJavaSourceSettingsUpdater extends JobChangeAdapter {

        boolean buildScheduled = false

        @Override
        public void scheduled(IJobChangeEvent event) {
            if (event.job.class.name.startsWith(JavaSourceSettingsUpdater.class.name)) {
                buildScheduled = true
            }
        }

        boolean isRebuildScheduled() {
            return buildScheduled
        }
    }

}
