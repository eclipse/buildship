package Buildship.Check30.BasicTestCoverage.Linux.buildTypes

import Buildship.Check30.Checkpoints.buildTypes.SanityCheck
import Buildship.EclipseBuildTemplate
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.FailureAction

object Eclipse412 : BuildType({
    id("Basic_Test_Coverage_Linux_Eclipse412_Java8_30")
    name = "Basic Test Coverage (Linux, Eclipse 2019-06, Java 8)"

    templates(EclipseBuildTemplate)

    params {
        param("eclipse.version", "412")
        param("compiler.location", "%linux.java8.oracle.64bit%/bin/javac")
        param("eclipse.test.java.home", "%linux.java8.oracle.64bit%")
        param("gradle.tasks", "clean eclipseTest")
        param("env.JAVA_HOME", "%linux.java8.oracle.64bit%")
    }

    dependencies {
        snapshot(SanityCheck) {
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL
        }
    }

    requirements {
        contains("teamcity.agent.jvm.os.name", "Linux", "RQ_650")
    }
})
