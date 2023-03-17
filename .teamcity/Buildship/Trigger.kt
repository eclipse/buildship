package Buildship

import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.VcsTrigger
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.schedule
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs

enum class Trigger(val func: (BuildType) -> Unit) {
    NONE({ buildType -> }),
    GIT({ buildType ->
        buildType.triggers {
            vcs {
                quietPeriodMode = VcsTrigger.QuietPeriodMode.USE_DEFAULT
                triggerRules = """
                    -:**.md
                """.trimIndent()
                branchFilter = """
                    -:teamcity-versioned-settings
                """.trimIndent()
                perCheckinTriggering = true
                groupCheckinsByCommitter = true
                enableQueueOptimization = false
            }
        }
    }),
    DAILY({ buildType ->
        buildType.triggers {
            schedule {
                schedulingPolicy = daily {
                    hour = 4
                    timezone = "Europe/Budapest"
                }
                branchFilter = """
                    +:master
                """.trimIndent()
                triggerBuild = always()
                param("revisionRule", "lastFinished")
                param("dayOfWeek", "Sunday")
            }
        }
    }),

    DAILY_MASTER({ buildType ->
        buildType.triggers {
            schedule {
                schedulingPolicy = daily {
                    hour = 4
                    timezone = "Europe/Budapest"
                }
                branchFilter = """
                    +:master
                    -:teamcity-versioned-settings
                """.trimIndent()
                triggerBuild = always()
                param("revisionRule", "lastFinished")
                param("dayOfWeek", "Sunday")
            }
        }
    });

    fun applyOn(buildType: BuildType) {
        func.invoke(buildType)
    }
}