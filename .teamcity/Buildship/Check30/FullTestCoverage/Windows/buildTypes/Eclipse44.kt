package Buildship.Check30.FullTestCoverage.Windows.buildTypes

import Buildship.Check30.Checkpoints.buildTypes.BasicTestCoverage
import Buildship.EclipseBuildTemplate
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.FailureAction

object Eclipse44 : BuildType({
    id("Full_Test_Coverage_Windows_Eclipse44_Java8_30")
    name = "Full Test Coverage (Windows, Eclipse 4.4, Java 8)"

    templates(EclipseBuildTemplate)

    params {
        param("eclipse.version", "44")
        param("compiler.location", """%windows.java8.oracle.64bit%\bin\javac""")
        param("eclipse.test.java.home", "%windows.java8.oracle.64bit%")
        param("env.JAVA_HOME", "%windows.java8.oracle.64bit%")
        param("enable.oomph.plugin", "false")
    }

    dependencies {
        snapshot(BasicTestCoverage) {
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL
        }
    }

    requirements {
        contains("teamcity.agent.jvm.os.name", "Windows", "RQ_489")
    }
})
