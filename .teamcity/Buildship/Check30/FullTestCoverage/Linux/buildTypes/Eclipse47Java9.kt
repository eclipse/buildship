package Buildship.Check30.FullTestCoverage.Linux.buildTypes

import Buildship.Check30.Checkpoints.buildTypes.BasicTestCoverage
import Buildship.EclipseBuildTemplate
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.FailureAction

object Eclipse47Java9 : BuildType({
    id("Full_Test_Coverage_Linux_Eclipse47_Java9_30")
    name = "Full Test Coverage (Linux, Eclipse 4.7, Java 9)"

    templates(EclipseBuildTemplate)

    params {
        param("eclipse.version", "47")
        param("compiler.location", "%linux.java8.oracle.64bit%/bin/javac")
        param("eclipse.test.java.home", "%linux.java9.oracle.64bit%")
        param("env.JAVA_HOME", "%linux.java9.oracle.64bit%")
    }

    dependencies {
        snapshot(BasicTestCoverage) {
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL
        }
    }

    requirements {
        contains("teamcity.agent.jvm.os.name", "Linux", "RQ_650")
    }
})
