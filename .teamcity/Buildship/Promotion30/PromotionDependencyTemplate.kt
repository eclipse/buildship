package Buildship.Promotion30

import Buildship.Check30.Checkpoints.buildTypes.Final
import jetbrains.buildServer.configs.kotlin.v2019_2.FailureAction
import jetbrains.buildServer.configs.kotlin.v2019_2.Template

object PromotionDependencyTemplate : Template({
    name = "Promotion Dependency Template"

    dependencies {
        snapshot(Final) {
            onDependencyFailure = FailureAction.FAIL_TO_START
            onDependencyCancel = FailureAction.CANCEL
        }
        snapshot(Buildship.Check30.CrossVersionCoverage.buildTypes.Eclipse47Java9, Buildship.Check30.Checkpoints.buildTypes.CheckpointUtils.DefaultFailureCondition)
    }
})
