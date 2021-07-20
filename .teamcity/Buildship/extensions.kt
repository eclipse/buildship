package Buildship

import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.FailureAction
import jetbrains.buildServer.configs.kotlin.v2019_2.Project
import jetbrains.buildServer.configs.kotlin.v2019_2.SnapshotDependency
import jetbrains.buildServer.configs.kotlin.v2019_2.failureConditions.BuildFailureOnText
import jetbrains.buildServer.configs.kotlin.v2019_2.failureConditions.failOnText
import jetbrains.buildServer.configs.kotlin.v2019_2.ui.add

fun BuildType.addCredentialsLeakFailureCondition() {
    failureConditions {
        add {
            failOnText {
                conditionType = BuildFailureOnText.ConditionType.CONTAINS
                pattern = "%unmaskedFakeCredentials%"
                failureMessage = "This build might be leaking credentials"
                reverse = false
                stopBuildOnFailure = true
            }
        }
    }
}

fun BuildType.createId(type: String, name: String)  {
    this.name = name
    id("${type}_${name.replace(" ", "_").replace("-", "_").replace("(", "").replace(")", "").replace("/", "").replace(",", "").replace(".", "_")}")
}

fun Project.buildTypesWithOrder(buildTypes: List<BuildType>) {
    buildTypes.forEach { bt ->
        buildType(bt)
    }
    buildTypesOrder = buildTypes
}

fun Project.subprojectsWithOrder(subprojects: List<Project>) {
    subprojects.forEach { sb ->
        subProject(sb)
    }
    subProjectsOrder = subprojects
}

fun Project.createId(name: String)  {
    this.name = name
    id("Buildship_${name.replace(" ", "_")}")
}

val DefaultFailureCondition : SnapshotDependency.() -> Unit
    get() =  {
        onDependencyCancel = FailureAction.ADD_PROBLEM
        onDependencyFailure = FailureAction.FAIL_TO_START
    }

val FailWhenDependenciesFail : SnapshotDependency.() -> Unit
    get() =  {
        onDependencyCancel = FailureAction.ADD_PROBLEM
        onDependencyFailure = FailureAction.ADD_PROBLEM
    }