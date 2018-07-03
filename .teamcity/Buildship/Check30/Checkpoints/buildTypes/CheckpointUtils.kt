package Buildship.Check30.Checkpoints.buildTypes

import jetbrains.buildServer.configs.kotlin.v2019_2.FailureAction
import jetbrains.buildServer.configs.kotlin.v2019_2.SnapshotDependency

object CheckpointUtils {
    val DefaultFailureCondition:  SnapshotDependency.() -> Unit = {
        onDependencyCancel = FailureAction.ADD_PROBLEM
        onDependencyFailure = FailureAction.FAIL_TO_START
    }
}