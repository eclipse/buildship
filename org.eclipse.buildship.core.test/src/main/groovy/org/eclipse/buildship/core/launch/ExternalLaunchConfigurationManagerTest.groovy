package org.eclipse.buildship.core.launch

import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.*

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IProjectDescription
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.debug.core.DebugPlugin
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.debug.core.ILaunchConfigurationType
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy
import org.eclipse.debug.core.ILaunchManager
import org.eclipse.jdt.core.IJavaProject

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.launch.internal.DefaultExternalLaunchConfigurationManager
import org.eclipse.buildship.core.launch.internal.GradleClasspathProvider
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification

class ExternalLaunchConfigurationManagerTest extends WorkspaceSpecification {

    ExternalLaunchConfigurationManager manager

    def setup() {
        manager = new DefaultExternalLaunchConfigurationManager()
        CorePlugin.instance.launchConfigListener.unregister()
    }

    def cleanup() {
        CorePlugin.instance.launchConfigListener = CorePlugin.instance.launchConfigListener.createAndRegister()
    }

    def "Gradle classpath provider is updated for Gradle projects"() {
        setup:
        IJavaProject project = newJavaProject('ext-run-config-manager-test')
        ILaunchConfiguration configuration = createJdtLaunchConfigFor(project)
        addGradleNature(project)

        when:
        manager.updateClasspathProvider(configuration)

        then:
        hasGradleClasspathProvider(configuration)
    }

    def "Gradle classpath provider is removed for non-Gradle projects"() {
        setup:
        IJavaProject project = newJavaProject('ext-run-config-manager-test')
        ILaunchConfiguration configuration = createJdtLaunchConfigFor(project, [(ATTR_CLASSPATH_PROVIDER) : GradleClasspathProvider.ID])

        when:
        manager.updateClasspathProvider(configuration)

        then:
        !hasGradleClasspathProvider(configuration)
    }

    def "Provider remains unchanged if correctly configured"() {
        setup:
        IJavaProject project = newJavaProject('ext-run-config-manager-test')
        ILaunchConfiguration configuration = createJdtLaunchConfigFor(project)

        when:
        manager.updateClasspathProvider(configuration)

        then:
        !hasGradleClasspathProvider(configuration)

        when:
        addGradleNature(project)
        configuration = createJdtLaunchConfigFor(project, [(ATTR_CLASSPATH_PROVIDER) : GradleClasspathProvider.ID])

        then:
        hasGradleClasspathProvider(configuration)
    }

    def "Previous classpath provider is restored"() {
        setup:
        IJavaProject project = newJavaProject('ext-run-config-manager-test')
        addGradleNature(project)
        ILaunchConfiguration withDefaultClasspathProvider = createJdtLaunchConfigFor(project)
        ILaunchConfiguration withCustomClasspathProvider = createJdtLaunchConfigFor(project, [(ATTR_CLASSPATH_PROVIDER) : 'custom'])

        when:
        manager.updateClasspathProvider(withDefaultClasspathProvider)
        manager.updateClasspathProvider(withCustomClasspathProvider)
        removeGradleNature(project)
        manager.updateClasspathProvider(withDefaultClasspathProvider)
        manager.updateClasspathProvider(withCustomClasspathProvider)

        then:
        getClasspathProvider(withDefaultClasspathProvider) == null
        getClasspathProvider(withCustomClasspathProvider) == 'custom'
    }

    def "Can update all launch configuration at once for a target project"() {
        setup:
        IJavaProject projectA = newJavaProject('ext-run-config-manager-test-a')
        IJavaProject projectB = newJavaProject('ext-run-config-manager-test-b')
        ILaunchConfiguration configurationA = createJdtLaunchConfigFor(projectA)
        ILaunchConfiguration configurationB = createJdtLaunchConfigFor(projectB)
        addGradleNature(projectA)
        addGradleNature(projectB)

        when:
        manager.updateClasspathProviders(projectA.project)

        then:
        hasGradleClasspathProvider(configurationA)
        !hasGradleClasspathProvider(configurationB)
    }

    private ILaunchConfiguration createJdtLaunchConfigFor(IJavaProject javaProject, Map<String, String> attributes = [:]) {
        IProject project = javaProject.project
        ILaunchConfigurationWorkingCopy launchConfig = createLaunchConfig(DefaultExternalLaunchConfigurationManager.LAUNCH_CONFIG_TYPE_JAVA_LAUNCH)
        launchConfig.setAttribute(ATTR_PROJECT_NAME, project.name)
        attributes.forEach { k, v -> launchConfig.setAttribute(k, v) }
        launchConfig.doSave()
    }

    private void addGradleNature(IJavaProject javaProject) {
        IProjectDescription projectDescription = javaProject.project.description
        projectDescription.setNatureIds((projectDescription.natureIds + GradleProjectNature.ID) as String[])
        javaProject.project.setDescription(projectDescription, new NullProgressMonitor())
    }

    private void removeGradleNature(IJavaProject javaProject) {
        IProjectDescription projectDescription = javaProject.project.description
        projectDescription.setNatureIds((projectDescription.natureIds.findAll { it != GradleProjectNature.ID }) as String[])
        javaProject.project.setDescription(projectDescription, new NullProgressMonitor())
    }

    private boolean hasGradleClasspathProvider(ILaunchConfiguration configuration) {
        getClasspathProvider(configuration) == GradleClasspathProvider.ID
    }

    private String getClasspathProvider(ILaunchConfiguration configuration) {
        configuration.getAttribute(ATTR_CLASSPATH_PROVIDER, (String)null)
    }
}
