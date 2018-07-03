package Buildship.Check30.Checkpoints.buildTypes

import Buildship.GitHubVcsRoot
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.CheckoutMode
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.finishBuildTrigger

object BasicTestCoverage : BuildType({
    id("Checkpoint_Basic_Test_Coverage_30")
    name = "Basic Test Coverage"
    description = "Runs basic integration tests"

    vcs {
        root(GitHubVcsRoot)
        checkoutMode = CheckoutMode.ON_AGENT
    }

    triggers {
        finishBuildTrigger {
            buildTypeExtId = "${SanityCheck.id}"
            successfulOnly = true
            branchFilter = """
                +:*
                -:teamcity-versioned-settings
            """.trimIndent()
        }
    }

    requirements {
        contains("teamcity.agent.jvm.os.name", "Linux")
    }

    dependencies {
        snapshot(SanityCheck, CheckpointUtils.DefaultFailureCondition)

        snapshot(Buildship.Check30.BasicTestCoverage.Linux.buildTypes.Eclipse43, CheckpointUtils.DefaultFailureCondition)
        snapshot(Buildship.Check30.BasicTestCoverage.Linux.buildTypes.Eclipse412, CheckpointUtils.DefaultFailureCondition)
        snapshot(Buildship.Check30.BasicTestCoverage.Windows.buildTypes.Eclipse43, CheckpointUtils.DefaultFailureCondition)
        snapshot(Buildship.Check30.BasicTestCoverage.Windows.buildTypes.Eclipse46, CheckpointUtils.DefaultFailureCondition)
    }
})
