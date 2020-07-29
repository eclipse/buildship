package Buildship.Check30.SanityCheck.buildTypes

import Buildship.EclipseBuildTemplate
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.retryBuild

object Eclipse413 : BuildType({
    id("Sanity_Check_Linux_Eclipse413_Java8_30")
    name = "Sanity Check (Linux, Eclipse 2019-09, Java 8)"

    templates(EclipseBuildTemplate)

    params {
        param("eclipse.version", "413")
        param("compiler.location", "%linux.java8.oracle.64bit%/bin/javac")
        param("eclipse.test.java.home", "%linux.java8.oracle.64bit%")
        param("gradle.tasks", "assemble checkstyleMain")
        param("env.JAVA_HOME", "%linux.java8.oracle.64bit%")
    }

    requirements {
        contains("teamcity.agent.jvm.os.name", "Linux", "RQ_650")
    }
})
