package org.eclipse.buildship.core.internal.launch

import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.*

import spock.lang.Issue

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IProjectDescription
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.debug.core.DebugPlugin
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.debug.core.ILaunchConfigurationListener
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy
import org.eclipse.jdt.core.IJavaProject

import org.eclipse.buildship.core.internal.configuration.GradleProjectNature
import org.eclipse.buildship.core.internal.launch.impl.SupportedLaunchConfigType
import org.eclipse.buildship.core.internal.test.fixtures.WorkspaceSpecification

class MavenGradleClasspathProviderUpdaterTest extends WorkspaceSpecification {

    @Issue('https://github.com/eclipse/buildship/issues/617')
    def "Classpath provider can be updated when two run configuration listener racing for it"() {
        setup:
        ILaunchConfigurationListener listener = new DummyMavenClasspathProviderUpdater()
        DebugPlugin.default.launchManager.addLaunchConfigurationListener(listener)

        when:
        IJavaProject project = newJavaProject('ext-run-config-manager-test')
        ILaunchConfiguration configuration = createJdtLaunchConfigFor(project)
        addGradleNature(project)

        then:
        notThrown(Throwable)

        cleanup:
        DebugPlugin.default.launchManager.removeLaunchConfigurationListener(listener)
    }

    private ILaunchConfiguration createJdtLaunchConfigFor(IJavaProject javaProject, Map<String, String> attributes = [:]) {
        IProject project = javaProject.project
        ILaunchConfigurationWorkingCopy launchConfig = createLaunchConfig(SupportedLaunchConfigType.JDT_JAVA_APPLICATION.id)
        launchConfig.setAttribute(ATTR_PROJECT_NAME, project.name)
        attributes.each { String k, String v -> launchConfig.setAttribute(k, v) }
        launchConfig.doSave()
    }

    private void addGradleNature(IJavaProject javaProject) {
        IProjectDescription projectDescription = javaProject.project.description
        projectDescription.setNatureIds((projectDescription.natureIds + GradleProjectNature.ID) as String[])
        javaProject.project.setDescription(projectDescription, new NullProgressMonitor())
    }

    class DummyMavenClasspathProviderUpdater implements ILaunchConfigurationListener {

        private final String PROVIDER_ID = 'org.eclipse.m2e.launchconfig.classpathProvider'

        @Override
        public void launchConfigurationChanged(ILaunchConfiguration configuration) {
            if (configuration instanceof ILaunchConfigurationWorkingCopy) {
                updateLaunchConfiguration((ILaunchConfigurationWorkingCopy) configuration);
            } else {
                ILaunchConfigurationWorkingCopy workingCopy = configuration.getWorkingCopy();
                updateLaunchConfiguration(workingCopy);
            }
        }

        private void updateLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy) {
            String provider = workingCopy.getAttribute(ATTR_CLASSPATH_PROVIDER, '')
            if (!provider.equals(PROVIDER_ID)) {
                workingCopy.setAttribute(ATTR_CLASSPATH_PROVIDER, PROVIDER_ID)
                workingCopy.doSave()
            }
        }

        @Override
        public void launchConfigurationAdded(ILaunchConfiguration configuration) {
        }

        @Override
        public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
        }
    }
}
