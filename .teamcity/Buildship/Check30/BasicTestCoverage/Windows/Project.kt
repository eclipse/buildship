package Buildship.Check30.BasicTestCoverage.Windows

import Buildship.Check30.BasicTestCoverage.Windows.buildTypes.Eclipse43
import Buildship.Check30.BasicTestCoverage.Windows.buildTypes.Eclipse46

import jetbrains.buildServer.configs.kotlin.v2019_2.Project

object Project : Project({
    id("Basic_Test_Coverage_Windows_30")
    name = "Basic Test Coverage - Windows"

    buildType(Eclipse43)
    buildType(Eclipse46)

    buildTypesOrder = arrayListOf(Eclipse43, Eclipse46)
})