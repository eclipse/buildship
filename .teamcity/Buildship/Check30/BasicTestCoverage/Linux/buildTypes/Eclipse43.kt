package Buildship.Check30.BasicTestCoverage.Linux.buildTypes

import Buildship.Check30.Checkpoints.buildTypes.SanityCheck
import Buildship.EclipseBuildTemplate
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.FailureAction

object Eclipse43 : BuildType({
    id("Basic_Test_Coverage_Linux_Eclipse43_java8_30")
    name = "Basic Test Coverage (Linux, Eclipse 4.3, Java 8)"

    templates(EclipseBuildTemplate)

    params {
        param("eclipse.version", "43")
        param("compiler.location", "%linux.java8.oracle.64bit%/bin/javac")
        param("eclipse.test.java.home", "%linux.java8.oracle.64bit%")
        param("gradle.tasks", "clean eclipseTest")
        param("env.JAVA_HOME", "%linux.java8.oracle.64bit%")
        param("enable.oomph.plugin", "false")
    }

    dependencies {
        snapshot(SanityCheck) {
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL
        }
    }

    requirements {
        contains("teamcity.agent.jvm.os.name", "Linux", "RQ_171")
    }
})
