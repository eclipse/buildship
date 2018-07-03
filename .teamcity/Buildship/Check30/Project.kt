package Buildship.Check30

import jetbrains.buildServer.configs.kotlin.v2019_2.Project

object Project : Project({
    id("Check")
    name = "Check"
    description = "Test coverage"
    subProjectsOrder = arrayListOf(Buildship.Check30.Checkpoints.Project,
                                   Buildship.Check30.BasicTestCoverage.Project,
                                   Buildship.Check30.FullTestCoverage.Project,
                                   Buildship.Check30.CrossVersionCoverage.Project,
                                   Buildship.Check30.Experimental.Project)

    subProject(Buildship.Check30.Checkpoints.Project)
    subProject(Buildship.Check30.SanityCheck.Project)
    subProject(Buildship.Check30.BasicTestCoverage.Project)
    subProject(Buildship.Check30.FullTestCoverage.Project)
    subProject(Buildship.Check30.CrossVersionCoverage.Project)
    subProject(Buildship.Check30.Experimental.Project)
})
