package Buildship.Check30.CrossVersionCoverage

import Buildship.Check30.CrossVersionCoverage.buildTypes.Eclipse47Java9
import jetbrains.buildServer.configs.kotlin.v2019_2.Project

object Project : Project({
    id("CrossVersionCoverage_30")
    name = "Cross-version Integration Tests"
    description = "Runs tests with all supported Gradle versions"

    buildType(Eclipse47Java9)
})
