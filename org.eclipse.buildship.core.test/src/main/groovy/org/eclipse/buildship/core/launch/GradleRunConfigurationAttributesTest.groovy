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

package org.eclipse.buildship.core.launch

import org.eclipse.debug.core.DebugPlugin
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper.DistributionType

import java.lang.reflect.Field;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.transform.Canonical
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.FirstParam;
import spock.lang.Shared;
import spock.lang.Specification;

class GradleRunConfigurationAttributesTest extends Specification {

    @Shared def Attributes defaults = new Attributes (
        tasks : ['clean'],
        workingDir : "/home/user/workspace",
        gradleDistr : GradleDistributionWrapper.from(DistributionType.WRAPPER, null).toGradleDistribution(),
        gradleHome : "/.gradle",
        javaHome : "/.java",
        arguments : ["-q"],
        jvmArguments : ["-ea"],
        showExecutionView :  true,
        showConsoleView : true
    )

    def "Can create a new valid instance"() {
        when:
        def configuration = defaults.toConfiguration()

        then:
        // not null
        configuration != null
        // check non-calculated values
        configuration.getTasks() == defaults.tasks
        configuration.getWorkingDirExpression() == defaults.workingDir
        configuration.getGradleDistribution() == defaults.gradleDistr
        configuration.getGradleUserHomeExpression() == defaults.gradleHome
        configuration.getJavaHomeExpression() == defaults.javaHome
        configuration.getJvmArguments() == defaults.jvmArguments
        configuration.getArguments() == defaults.arguments
        configuration.isShowExecutionView() == defaults.showExecutionView
        configuration.isShowConsoleView() == defaults.showConsoleView
        // check calculated value
        configuration.getArgumentExpressions() == defaults.arguments
        configuration.getJvmArgumentExpressions() == defaults.jvmArguments
        configuration.getWorkingDir().getAbsolutePath() == new File(defaults.workingDir).getAbsolutePath()
        configuration.getGradleUserHome().getAbsolutePath() == new File(defaults.gradleHome).getAbsolutePath()
        configuration.getJavaHome().getAbsolutePath() == new File(defaults.javaHome).getAbsolutePath()
    }

    def "Can create a new valid instance with valid null arguments"(Attributes attributes) {
        when:
        def configuration = attributes.toConfiguration()

        then:
        configuration != null
        attributes.gradleHome != null || configuration.getGradleUserHome() == null
        attributes.javaHome != null || configuration.getJavaHome() == null

        where:
        attributes << [
            defaults.copy { gradleHome = null },
            defaults.copy { javaHome = null },
            defaults.copy { gradleHome = null; javaHome = null }
        ]
    }

    def "Creation fails when null argument passed"(Attributes attributes) {
        when:
        attributes.toConfiguration()

        then:
        thrown(RuntimeException)

        where:
        attributes << [
            defaults.copy { tasks = null },
            defaults.copy { workingDir = null },
            defaults.copy { jvmArguments = null},
            defaults.copy { arguments = null}
        ]
    }

    def "Expressions can be resolved in the parameters"() {
        when:
        def Attributes attributes = defaults.copy {
            workingDir = '${workspace_loc}/working_dir'
            gradleHome = '${workspace_loc}/gradle_user_home'
            javaHome = '${workspace_loc}/java_home'
        }
        def configuration = attributes.toConfiguration()

        then:
        configuration.getWorkingDir().getPath().endsWith("working_dir")
        !(configuration.getWorkingDir().getPath().contains('$'))
        configuration.getGradleUserHome().getPath().endsWith("gradle_user_home")
        !(configuration.getGradleUserHome().getPath().contains('$'))
        configuration.getJavaHome().getPath().endsWith("java_home")
        !(configuration.getJavaHome().getPath().contains('$'))
    }

    def "Unresolvable expressions in Java home results in runtime exception"() {
        setup:
        def Attributes attributes = defaults.copy {
            javaHome = '${nonexistingvariable}/java_home'
        }
        def configuration = attributes.toConfiguration()

        when:
        configuration.getJavaHome()

        then:
        thrown(GradlePluginsRuntimeException)

    }

    def "Unresolvable expressions in Gradle user home results in runtime exception"() {
        setup:
        def Attributes attributes = defaults.copy {
            gradleHome = '${nonexistingvariable}/gradle_user_home'
        }
        def configuration = attributes.toConfiguration()

        when:
        configuration.getGradleUserHome()

        then:
        thrown(GradlePluginsRuntimeException)

    }

    def "Unresolvable expressions in working directory results in runtime exception"() {
        setup:
        def Attributes attributes = defaults.copy {
            workingDir = '${nonexistingvariable}/working_dir'
        }
        def configuration = attributes.toConfiguration()

        when:
        configuration.getWorkingDir()

        then:
        thrown(GradlePluginsRuntimeException)
    }

    def "Unresolvable expressions in arguments results in runtime exception"() {
        setup:
        def Attributes attributes = defaults.copy {
            arguments = ['${nonexistingvariable}/arguments']
        }
        def configuration = attributes.toConfiguration()

        when:
        configuration.getArguments()

        then:
        thrown(GradlePluginsRuntimeException)
    }

    def "Unresolvable expressions in jvm arguments results in runtime exception"() {
        setup:
        def Attributes attributes = defaults.copy {
            jvmArguments = ['${nonexistingvariable}/jvmarguments']
        }
        def configuration = attributes.toConfiguration()

        when:
        configuration.getJvmArguments()

        then:
        thrown(GradlePluginsRuntimeException)
    }

    def "All configuration can be saved to Eclipse settings"() {
        setup:
        def launchManager = DebugPlugin.getDefault().getLaunchManager();
        def type = launchManager.getLaunchConfigurationType(GradleRunConfigurationDelegate.ID);
        def eclipseConfig = type.newInstance(null, "launch-config-name")

        when:
        assert eclipseConfig.getAttributes().isEmpty()
        def gradleConfig = defaults.toConfiguration()
        gradleConfig.apply(eclipseConfig)

        then:
        eclipseConfig.getAttributes().size() == defaults.size()
    }

    def "All valid configuration settings can be stored and retrieved"(Attributes attributes) {
        setup:
        def launchManager = DebugPlugin.getDefault().getLaunchManager();
        def type = launchManager.getLaunchConfigurationType(GradleRunConfigurationDelegate.ID);
        def eclipseConfig = type.newInstance(null, "launch-config-name")

        when:
        def gradleConfig1 = attributes.toConfiguration()
        gradleConfig1.apply(eclipseConfig)
        def gradleConfig2 = GradleRunConfigurationAttributes.from(eclipseConfig)

        then:
        gradleConfig1.getTasks() == gradleConfig2.getTasks()
        gradleConfig1.getWorkingDirExpression() == gradleConfig2.getWorkingDirExpression()
        gradleConfig1.getGradleDistribution() == gradleConfig2.getGradleDistribution()
        gradleConfig1.getGradleUserHomeExpression() == gradleConfig2.getGradleUserHomeExpression()
        gradleConfig1.getJavaHomeExpression() == gradleConfig2.getJavaHomeExpression()
        gradleConfig1.getJvmArguments() == gradleConfig2.getJvmArguments()
        gradleConfig1.getArguments() == gradleConfig2.getArguments()
        gradleConfig1.isShowExecutionView() == gradleConfig2.isShowExecutionView()

        where:
        attributes << [
            defaults,
            defaults.copy { gradleHome = null },
            defaults.copy { javaHome = null },
            defaults.copy { gradleHome = null; javaHome = null }
        ]
    }

    def "Saved Configuration attributes has same unique attributes"() {
        setup:
        def launchManager = DebugPlugin.getDefault().getLaunchManager();
        def type = launchManager.getLaunchConfigurationType(GradleRunConfigurationDelegate.ID);
        def eclipseConfig = type.newInstance(null, "launch-config-name")

        when:
        def gradleConfig = defaults.toConfiguration()
        gradleConfig.apply(eclipseConfig)

        then:
        gradleConfig.hasSameUniqueAttributes(eclipseConfig)
    }

    static class Attributes implements Cloneable {
        def tasks
        def workingDir
        def gradleDistr
        def gradleHome
        def javaHome
        def arguments
        def jvmArguments
        def showExecutionView
        def showConsoleView

        def GradleRunConfigurationAttributes toConfiguration() {
            GradleRunConfigurationAttributes.with(tasks, workingDir, gradleDistr, gradleHome, javaHome, jvmArguments, arguments, showExecutionView, showConsoleView)
        }

        def Attributes copy(@DelegatesTo(value = Attributes, strategy=Closure.DELEGATE_FIRST) Closure closure) {
            def clone = clone()
            def Closure clonedClosure =  closure.clone()
            clonedClosure.setResolveStrategy(Closure.DELEGATE_FIRST)
            clonedClosure.setDelegate(clone)
            clonedClosure.call(clone)
            return clone
        }

        def int size() {
            Attributes.declaredFields.findAll { it.synthetic == false }.size
        }
    }

}
