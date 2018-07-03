package Buildship.Check30.FullTestCoverage.Windows

import Buildship.Check30.FullTestCoverage.Windows.buildTypes.Eclipse44
import Buildship.Check30.FullTestCoverage.Windows.buildTypes.Eclipse47
import jetbrains.buildServer.configs.kotlin.v2019_2.Project

object Project : Project({
    id("Full_Test_Coverage_Windows_30")
    name = "Full Test Coverage - Windows"

    buildType(Eclipse44)
    buildType(Eclipse47)
})
