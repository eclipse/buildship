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

package com.gradleware.tooling.eclipse.core.launch

import java.util.List;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;

import com.gradleware.tooling.eclipse.core.GradlePluginsRuntimeException;
import com.gradleware.tooling.eclipse.core.gradle.GradleDistributionWrapper;
import com.gradleware.tooling.eclipse.core.gradle.GradleDistributionWrapper.DistributionType;
import com.gradleware.tooling.toolingclient.GradleDistribution;

import spock.lang.Shared;
import spock.lang.Specification;


class GradleRunConfigurationAttributesTest extends Specification {

    // list ov valid attributes
    @Shared def atr = [
        'tasks' : ['clean'],
        'workingDir' : "/home/user/workspace",
        'gradleDistr' : GradleDistributionWrapper.from(DistributionType.WRAPPER, null).toGradleDistribution(),
        'gradleHome' : "/.gradle",
        'javaHome' : "/.java",
        'arguments' : ["-q"],
        'jvmArguments' : ["-ea"],
        'visualize' :  true
    ]


    def "Can create a new valid instance"() {
        when:
        def configuration = GradleRunConfigurationAttributes.with(atr.tasks, atr.workingDir, atr.gradleDistr, atr.gradleHome, atr.javaHome, atr.jvmArguments, atr.arguments, atr.visualize)

        then:
        // not null
        configuration != null
        // check non-calculated values
        configuration.getTasks() == atr.tasks
        configuration.getWorkingDirExpression() == atr.workingDir
        configuration.getGradleDistribution() == atr.gradleDistr
        configuration.getGradleUserHomeExpression() == atr.gradleHome
        configuration.getJavaHomeExpression() == atr.javaHome
        configuration.getJvmArguments() == atr.jvmArguments
        configuration.getArguments() == atr.arguments
        configuration.isVisualizeTestProgress() == atr.visualize
        // check calculated value
        configuration.getArgumentExpressions() == atr.arguments
        configuration.getJvmArgumentExpressions() == atr.jvmArguments
        configuration.getWorkingDir().getAbsolutePath() == new File(atr.workingDir).getAbsolutePath()
        configuration.getGradleUserHome().getAbsolutePath() == new File(atr.gradleHome).getAbsolutePath()
        configuration.getJavaHome().getAbsolutePath() == new File(atr.javaHome).getAbsolutePath()
    }



    def "Can create a new valid instance with valid null arguments"() {
        when:
        def configuration = GradleRunConfigurationAttributes.with(tasks, workingDir, gradleDistr, gradleHome, javaHome, jvmArguments, arguments, visualize)

        then:
        configuration != null
        gradleHome != null || configuration.getGradleUserHome() == null
        javaHome != null || configuration.getJavaHome() == null


        where:
        tasks     | workingDir     | gradleDistr     | gradleHome     | javaHome     | jvmArguments     | arguments     | visualize
        atr.tasks | atr.workingDir | atr.gradleDistr | null           | atr.javaHome | atr.jvmArguments | atr.arguments | atr.visualize
        atr.tasks | atr.workingDir | atr.gradleDistr | atr.gradleHome | null         | atr.jvmArguments | atr.arguments | atr.visualize
        atr.tasks | atr.workingDir | atr.gradleDistr | null           | null         | atr.jvmArguments | atr.arguments | atr.visualize
    }

    def "Creation fails when null argument passed"() {
        when:
        GradleRunConfigurationAttributes.with(tasks, workingDir, gradleDistr, gradleHome, javaHome, jvmArguments, arguments, visualize)

        then:
        thrown(RuntimeException)

        where:
        tasks     | workingDir     | gradleDistr     | gradleHome     | javaHome     | jvmArguments     | arguments     | visualize
        null      | atr.workingDir | atr.gradleDistr | atr.gradleHome | atr.javaHome | atr.jvmArguments | atr.arguments | atr.visualize
        atr.tasks | null           | atr.gradleDistr | atr.gradleHome | atr.javaHome | atr.jvmArguments | atr.arguments | atr.visualize
        atr.tasks | atr.workingDir | atr.gradleDistr | atr.gradleHome | atr.javaHome | null             | atr.arguments | atr.visualize
        atr.tasks | atr.workingDir | atr.gradleDistr | atr.gradleHome | atr.javaHome | atr.jvmArguments | null          | atr.visualize
    }

    def "Expressions can be resolved in the parameters"() {
        when:
        def configuration = GradleRunConfigurationAttributes.with(atr.tasks, '${workspace_loc}/working_dir', atr.gradleDistr, '${workspace_loc}/gradle_user_home', '${workspace_loc}/java_home', atr.jvmArguments, atr.arguments, atr.visualize)

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
        def configuration = GradleRunConfigurationAttributes.with(atr.tasks, atr.workingDir, atr.gradleDistr, atr.gradleHome, '${nonexistingvariable}/java_home', atr.jvmArguments, atr.arguments, atr.visualize)

        when:
        configuration.getJavaHome()

        then:
        thrown(GradlePluginsRuntimeException)

    }

    def "Unresolvable expressions in Gradle user home results in runtime exception"() {
        setup:
        def configuration = GradleRunConfigurationAttributes.with(atr.tasks, atr.workingDir, atr.gradleDistr, '${nonexistingvariable}/gradle_user_home', atr.javaHome, atr.jvmArguments, atr.arguments, atr.visualize)

        when:
        configuration.getGradleUserHome()

        then:
        thrown(GradlePluginsRuntimeException)

    }

    def "Unresolvable expressions in working directory results in runtime exception"() {
        setup:
        def configuration = GradleRunConfigurationAttributes.with(atr.tasks, '${nonexistingvariable}/working_dir', atr.gradleDistr, atr.gradleHome, atr.javaHome, atr.jvmArguments, atr.arguments, atr.visualize)

        when:
        configuration.getWorkingDir()

        then:
        thrown(GradlePluginsRuntimeException)
    }

    def "Unresolvable expressions in arguments results in runtime exception"() {
        setup:
        def configuration = GradleRunConfigurationAttributes.with(atr.tasks, atr.workingDir, atr.gradleDistr, atr.gradleHome, atr.javaHome, atr.jvmArguments,  ['${nonexistingvariable}/arguments'], atr.visualize)

        when:
        configuration.getArguments()

        then:
        thrown(GradlePluginsRuntimeException)
    }

    def "Unresolvable expressions in jvm arguments results in runtime exception"() {
        setup:
        def configuration = GradleRunConfigurationAttributes.with(atr.tasks, atr.workingDir, atr.gradleDistr, atr.gradleHome, atr.javaHome,  ['${nonexistingvariable}/jvmarguments'],  atr.arguments, atr.visualize)

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
        def gradleConfig = GradleRunConfigurationAttributes.with(atr.tasks, atr.workingDir, atr.gradleDistr, atr.gradleHome, atr.javaHome, atr.jvmArguments, atr.arguments, atr.visualize)
        gradleConfig.apply(eclipseConfig)

        then:
        eclipseConfig.getAttributes().size() == atr.size()
    }

    def "All valid configuration settings can be stored and retrieved"() {
        setup:
        def launchManager = DebugPlugin.getDefault().getLaunchManager();
        def type = launchManager.getLaunchConfigurationType(GradleRunConfigurationDelegate.ID);
        def eclipseConfig = type.newInstance(null, "launch-config-name")

        when:
        def gradleConfig1 = GradleRunConfigurationAttributes.with(tasks, workingDir, gradleDistr, gradleHome, javaHome, jvmArguments, arguments, visualize)
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
        gradleConfig1.isVisualizeTestProgress() == gradleConfig2.isVisualizeTestProgress()

        where:
        tasks     | workingDir     | gradleDistr     | gradleHome     | javaHome     | jvmArguments     | arguments     | visualize
        atr.tasks | atr.workingDir | atr.gradleDistr | atr.gradleHome | atr.javaHome | atr.jvmArguments | atr.arguments | atr.visualize
        atr.tasks | atr.workingDir | atr.gradleDistr | null           | atr.javaHome | atr.jvmArguments | atr.arguments | atr.visualize
        atr.tasks | atr.workingDir | atr.gradleDistr | atr.gradleHome | null         | atr.jvmArguments | atr.arguments | atr.visualize
        atr.tasks | atr.workingDir | atr.gradleDistr | null           | null         | atr.jvmArguments | atr.arguments | atr.visualize
    }


    def "Saved Configuration attributes has same unique attributes"() {
        setup:
        def launchManager = DebugPlugin.getDefault().getLaunchManager();
        def type = launchManager.getLaunchConfigurationType(GradleRunConfigurationDelegate.ID);
        def eclipseConfig = type.newInstance(null, "launch-config-name")

        when:
        def gradleConfig = GradleRunConfigurationAttributes.with(atr.tasks, atr.workingDir, atr.gradleDistr, atr.gradleHome, atr.javaHome, atr.jvmArguments, atr.arguments, atr.visualize)
        gradleConfig.apply(eclipseConfig)

        then:
        gradleConfig.hasSameUniqueAttributes(eclipseConfig)
    }

}
