package Buildship.Promotion30

import Buildship.Check30.Checkpoints.buildTypes.Final
import Buildship.GitHubVcsRoot
import Buildship.addCredentialsLeakFailureCondition
import jetbrains.buildServer.configs.kotlin.v2019_2.CheckoutMode
import jetbrains.buildServer.configs.kotlin.v2019_2.FailureAction
import jetbrains.buildServer.configs.kotlin.v2019_2.Template

object Promotion30Template : Template({
    name = "Promotion30 Template"

    artifactRules = "org.eclipse.buildship.site/build/repository/** => update-site"

    addCredentialsLeakFailureCondition()

    // The artifact upload requires uses ssh which requires manual confirmation. to work around that, we use the same
    // machine for the upload.
    // TODO We should separate the update site generation and the artifact upload into two separate steps.
    requirements {
        contains("teamcity.agent.jvm.os.name", "Linux")
        contains("teamcity.agent.name", "dev")
    }

    vcs {
        root(GitHubVcsRoot)

        checkoutMode = CheckoutMode.ON_AGENT
        cleanCheckout = true
        showDependenciesChanges = true
    }

    failureConditions {
        errorMessage = true
    }

    dependencies {
        snapshot(Final) {
            onDependencyFailure = FailureAction.FAIL_TO_START
            onDependencyCancel = FailureAction.CANCEL
        }
        snapshot(Buildship.Check30.CrossVersionCoverage.buildTypes.Eclipse47Java9, Buildship.Check30.Checkpoints.buildTypes.CheckpointUtils.DefaultFailureCondition)
    }
})