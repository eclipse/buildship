package Buildship.Check30.BasicTestCoverage.Linux

import jetbrains.buildServer.configs.kotlin.v2019_2.Project

object Project : Project({
    id("Basic_Test_Coverage_Linux_30")
    name = "Basic Test Coverage - Linux"
    description = "Configurations that provide basic test coverage"

    buildType(Buildship.Check30.BasicTestCoverage.Linux.buildTypes.Eclipse43)
    buildType(Buildship.Check30.BasicTestCoverage.Linux.buildTypes.Eclipse412)
})
