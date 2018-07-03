package Buildship.Check30.FullTestCoverage

import jetbrains.buildServer.configs.kotlin.v2019_2.Project

object Project : Project({
    id("Full_Test_Coverage_30")
    name = "Full Test Coverage"
    description = "Configurations that provide full test coverage"

    subProject(Buildship.Check30.FullTestCoverage.Linux.Project)
    subProject(Buildship.Check30.FullTestCoverage.Windows.Project)
})
