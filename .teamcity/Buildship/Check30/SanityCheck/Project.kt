package Buildship.Check30.SanityCheck

import Buildship.Check30.SanityCheck.buildTypes.Eclipse413
import jetbrains.buildServer.configs.kotlin.v2019_2.Project

object Project : Project({
    id("SanityCheck")
    name = "Sanity check"
    description = "Compiles the project with against and the latest Eclipse version and warms up the cache"

    buildType(Eclipse413)
})
