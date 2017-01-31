package org.eclipse.buildship.ui.view.launch

import com.gradleware.tooling.toolingclient.GradleDistribution

import org.eclipse.debug.core.ILaunchConfiguration

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes
import org.eclipse.buildship.ui.test.fixtures.ProjectSynchronizationSpecification

class GradleLaunchConfigurationTest extends ProjectSynchronizationSpecification {

    def "Launch configuration can be loaded and validated after the project is deleted"() {
        setup:
        def project = dir("root") {
            file 'build.gradle'
            file 'settings.gradle'
        }
        importAndWait(project)
        def attributes = GradleRunConfigurationAttributes.with([], '${workspace_loc:/root}', GradleDistribution.fromBuild(), null, [], [], false, false, true)
        ILaunchConfiguration configuration = CorePlugin.gradleLaunchConfigurationManager().getOrCreateRunConfiguration(attributes)

        when:
        deleteAllProjects(true)
        attributes = GradleRunConfigurationAttributes.from(configuration)
        attributes.getWorkingDirExpression()

        then:
        notThrown(Exception)

        when:
        attributes.getWorkingDir()

        then:
        thrown(Exception)
    }

}
