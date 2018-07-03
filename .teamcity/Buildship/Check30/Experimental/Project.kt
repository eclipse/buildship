package Buildship.Check30.Experimental

import Buildship.Check30.Experimental.buildTypes.Eclipse47KotlinSupport
import jetbrains.buildServer.configs.kotlin.v2019_2.Project

object Project : Project({
    id("Experimental_30")
    name = "Experimental"

    buildType(Eclipse47KotlinSupport)
})
