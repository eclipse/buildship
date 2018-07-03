package Buildship.Check30.BasicTestCoverage.Windows.buildTypes

import Buildship.Check30.Checkpoints.buildTypes.SanityCheck
import Buildship.EclipseBuildTemplate
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.FailureAction

object Eclipse46 : BuildType({
    id("Basic_Test_Coverage_Windows_Eclipse46_java8_30")
    name = "Basic Test Coverage (Windows, Eclipse 4.6, Java 8)"

    templates(EclipseBuildTemplate)

    description = "Building the Eclipse plugin against Eclipse 4.6 without running the tests"

    params {
        param("eclipse.version", "46")
        param("compiler.location", """%windows.java8.oracle.64bit%\bin\javac""")
        param("eclipse.test.java.home", "%windows.java8.oracle.64bit%")
        param("gradle.tasks", "clean eclipseTest")
        param("env.JAVA_HOME", "%windows.java8.oracle.64bit%")
    }

    dependencies {
        snapshot(SanityCheck) {
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL
        }
    }

    requirements {
        contains("teamcity.agent.jvm.os.name", "Windows", "RQ_501")
    }
})
