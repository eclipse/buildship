package Buildship.Check30.Checkpoints

import Buildship.Check30.Checkpoints.buildTypes.BasicTestCoverage
import Buildship.Check30.Checkpoints.buildTypes.Final
import Buildship.Check30.Checkpoints.buildTypes.FullTestCoverage
import Buildship.Check30.Checkpoints.buildTypes.SanityCheck
import jetbrains.buildServer.configs.kotlin.v2019_2.Project

object Project : Project({
    id("Checkpoints_30")
    name = "Checkpoints"
    description = "Configurations of the stages to getting a distribution that passes QA"

    buildType(SanityCheck)
    buildType(BasicTestCoverage)
    buildType(FullTestCoverage)
    buildType(Final)

    buildTypesOrder = arrayListOf(SanityCheck, BasicTestCoverage, FullTestCoverage, Final)
})
