package Buildship.Check30.Checkpoints.buildTypes

import Buildship.GitHubVcsRoot
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.CheckoutMode
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.VcsTrigger
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs

object SanityCheck : BuildType({
    id("Checkpoint_Sanity_Check")
    name = "Sanity Check"

    vcs {
        root(GitHubVcsRoot)
        checkoutMode = CheckoutMode.ON_AGENT
    }

    triggers {
        vcs {
            quietPeriodMode = VcsTrigger.QuietPeriodMode.USE_DEFAULT
            triggerRules = """
                +:**
                -:**.md
            """.trimIndent()
            branchFilter = """
                +:*
                -:teamcity-versioned-settings
            """.trimIndent()
            perCheckinTriggering = true
            groupCheckinsByCommitter = true
            enableQueueOptimization = false
        }
    }

    requirements {
        contains("teamcity.agent.jvm.os.name", "Linux")
    }

    dependencies {
        snapshot(Buildship.Check30.SanityCheck.buildTypes.Eclipse413, CheckpointUtils.DefaultFailureCondition)
    }
})
