/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.internal.launch

import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status
import org.eclipse.debug.core.DebugPlugin
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.debug.core.ILaunchConfigurationType
import org.eclipse.debug.core.ILaunchManager

import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException
import org.eclipse.buildship.core.internal.launch.impl.DefaultGradleLaunchConfigurationManager
import org.eclipse.buildship.core.internal.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.internal.util.gradle.GradleDistribution

class GradleLaunchConfigurationManagerTest extends WorkspaceSpecification {
    GradleRunConfigurationAttributes validAttribute = createValidAttributes()
    GradleLaunchConfigurationManager manager = new DefaultGradleLaunchConfigurationManager()

    def "Storing a new attribute produces a new launch configuration instance"() {
        when:
        ILaunchConfiguration config = manager.getOrCreateRunConfiguration(validAttribute)

        then:
        config != null
        config.getName().contains("clean")
        config.getAttributes().values().size() >= 8
    }

    def "Can't create a run configuration from null object"() {
        when:
        def config = manager.getOrCreateRunConfiguration(null)

        then:
        thrown(NullPointerException)
    }

    def "Creating run configuration twice from same object finds creates only one element"() {
        // persistence contains only one config. objects same name are the same
    }

    def "Two different attributes create two run configurations"() {
        when:
        def config1 = manager.getOrCreateRunConfiguration(validAttribute)
        def config2 = manager.getOrCreateRunConfiguration(validAttribute)

        then:
        DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations().size() == 1
        config1 == config2
    }

    def "Can't save attribute if launch manager is not able to retrieve configurations"() {
        setup:
        ILaunchManager launchManager = Mock(ILaunchManager)
        ILaunchConfigurationType type = Mock(ILaunchConfigurationType)
        def configurationManager = new DefaultGradleLaunchConfigurationManager(launchManager)
        launchManager.getLaunchConfigurations() >> {}
        launchManager.getLaunchConfigurationType(_) >> { return type }
        launchManager.getLaunchConfigurations(_) >> { throw new CoreException(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, "error")) }

        when:
        configurationManager.getOrCreateRunConfiguration(validAttribute)

        then:
        thrown(GradlePluginsRuntimeException)
    }

    def "If can't save attributes then a runtime exception is thrown"() {
        setup:
        ILaunchManager launchManager = Mock(ILaunchManager)
        ILaunchConfigurationType type = Mock(ILaunchConfigurationType)
        def configurationManager = new DefaultGradleLaunchConfigurationManager(launchManager)
        launchManager.getLaunchConfigurations() >> {}
        launchManager.getLaunchConfigurationType(_) >> { return type }
        launchManager.getLaunchConfigurations(_) >> { [] }
        type.newInstance(_,_) >> { throw new CoreException(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, "error")) }

        when:
        configurationManager.getOrCreateRunConfiguration(validAttribute)

        then:
        thrown(GradlePluginsRuntimeException)
    }

    private GradleRunConfigurationAttributes createValidAttributes() {
        new GradleRunConfigurationAttributes(['clean'],
            '/home/user/workspace/project',
            GradleDistribution.forVersion('2.3').serializeToString(),
            null,
            '/.java',
            ['-ea'],
            ['-q'],
            true,
            true,
            true,
            true,
            true)
    }
}
