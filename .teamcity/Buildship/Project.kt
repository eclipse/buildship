package Buildship

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.Project
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.*

val ib1_1 = IndividualScenarioBuildType(type = "SanityCheck", os = "Linux", eclipseVersion = "4.13", javaVersion = "8")
val ib2_1 = IndividualScenarioBuildType(type = "Basic", os = "Linux", eclipseVersion = "4.3", javaVersion = "8")
val ib2_2 = IndividualScenarioBuildType(type = "Basic", os = "Linux", eclipseVersion = "4.12", javaVersion = "8")
val ib2_3 = IndividualScenarioBuildType(type = "Basic", os = "Windows", eclipseVersion = "4.3", javaVersion = "8")
val ib2_4 = IndividualScenarioBuildType(type = "Basic", os = "Windows", eclipseVersion = "4.6", javaVersion = "8")
val ib3_1 = IndividualScenarioBuildType(type = "Full", os = "Linux", eclipseVersion = "4.4", javaVersion = "8")
val ib3_2 = IndividualScenarioBuildType(type = "Full", os = "Linux", eclipseVersion = "4.5", javaVersion = "8")
val ib3_3 = IndividualScenarioBuildType(type = "Full", os = "Linux", eclipseVersion = "4.6", javaVersion = "8")
val ib3_4 = IndividualScenarioBuildType(type = "Full", os = "Linux", eclipseVersion = "4.7", javaVersion = "8")
val ib3_5 = IndividualScenarioBuildType(type = "Full", os = "Linux", eclipseVersion = "4.7", javaVersion = "9")
val ib3_6 = IndividualScenarioBuildType(type = "Full", os = "Linux", eclipseVersion = "4.8", javaVersion = "8")
val ib3_7 = IndividualScenarioBuildType(type = "Full", os = "Linux", eclipseVersion = "4.9", javaVersion = "8")
val ib3_8 = IndividualScenarioBuildType(type = "Full", os = "Linux", eclipseVersion = "4.10", javaVersion = "8")
val ib3_9 = IndividualScenarioBuildType(type = "Full", os = "Linux", eclipseVersion = "4.11", javaVersion = "8")
val ib3_10 = IndividualScenarioBuildType(type = "Full", os = "Linux", eclipseVersion = "4.12", javaVersion = "8")
val ib3_11 = IndividualScenarioBuildType(type = "Full", os = "Linux", eclipseVersion = "4.13", javaVersion = "8")
val ib3_12 = IndividualScenarioBuildType(type = "Full", os = "Linux", eclipseVersion = "4.14", javaVersion = "8")
val ib3_13 = IndividualScenarioBuildType(type = "Full", os = "Linux", eclipseVersion = "4.15", javaVersion = "8")
val ib3_14 = IndividualScenarioBuildType(type = "Full", os = "Linux", eclipseVersion = "4.16", javaVersion = "8")
val ib3_15 = IndividualScenarioBuildType(type = "Full", os = "Windows", eclipseVersion = "4.4", javaVersion = "8")
val ib3_16 = IndividualScenarioBuildType(type = "Full", os = "Windows", eclipseVersion = "4.7", javaVersion = "8")
val ib4_1 = IndividualScenarioBuildType(type = "CrossVersion", os = "Linux", eclipseVersion = "4.7", javaVersion = "9")

val individualBuildsForPhase1 = listOf(ib1_1)
val individualBuildsForPhase2 = listOf(ib2_1, ib2_2, ib2_3, ib2_4)
val individualBuildsForPhase3 = listOf(ib3_1, ib3_2, ib3_3, ib3_4, ib3_5, ib3_6, ib3_7, ib3_8, ib3_9, ib3_10, ib3_11,  ib3_12, ib3_13, ib3_14, ib3_15, ib3_16)
val individualBuildsForPhase4 = listOf(ib4_1)

val tb1_1 = TriggerBuildType("Sanity Check (Trigger)", individualBuildsForPhase1, null, "git")
val tb1_2 = TriggerBuildType("Basic Test Coverage (Trigger, Phase 1/2)", individualBuildsForPhase1, null)
val tb2_2 = TriggerBuildType("Basic Test Coverage (Phase 2/2)", individualBuildsForPhase2, tb1_2)
val tb3_1 = TriggerBuildType("Full Test Coverage (Trigger, Phase 1/3)", individualBuildsForPhase1, null)
val tb3_2 = TriggerBuildType("Full Test Coverage (Phase 2/3)", individualBuildsForPhase2, tb3_1)
val tb3_3 = TriggerBuildType("Full Test Coverage (Phase 3/3)", individualBuildsForPhase3, tb3_2)
val tb4_1 = TriggerBuildType("Cross-Version Coverage (Trigger, Phase 1/4)", individualBuildsForPhase1, null, "daily")
val tb4_2 = TriggerBuildType("Cross-Version Test Coverage (Phase 2/4)", individualBuildsForPhase2, tb4_1)
val tb4_3 = TriggerBuildType("Cross-Version Test Coverage (Phase 3/4)", individualBuildsForPhase3, tb4_2)
val tb4_4 = TriggerBuildType("Cross-Version Test Coverage (Phase 4/4)", individualBuildsForPhase4, tb4_3)

val snapshotPromotion = PromotionBuildType("snapshot", tb4_4, "daily")
val milestonePromotion = PromotionBuildType("milestone", tb4_4)
val releasePromotion = PromotionBuildType("release", tb4_4)

class IndividualScenarioBuildType(type: String, os: String, eclipseVersion: String, javaVersion: String) : BuildType({
    createId("Individual", "${type}_Test_Coverage_${os}_Eclipse${eclipseVersion.replace(".", "_")}_Java${javaVersion}")

    artifactRules = """
        org.eclipse.buildship.site/build/repository/** => .teamcity/update-site
        org.eclipse.buildship.core.test/build/eclipseTest/workspace/.metadata/.log => .teamcity/test/org.eclipse.buildship.core.test
        org.eclipse.buildship.ui.test/build/eclipseTest/workspace/.metadata/.log => .teamcity/test/org.eclipse.buildship.ui.test
    """.trimIndent()

    params {
        val sep = if (os == "Windows") "\\" else "/"
        param("eclipse.release.type", "snapshot")
        param("build.invoker", "ci")
        param("eclipsetest.mirrors", "jcenter:https://dev12.gradle.org/artifactory/jcenter")
        param("eclipse.version", eclipseVersion.replace(".", ""))
        param("compiler.location", """%${os.toLowerCase()}.java${javaVersion}.oracle.64bit%${sep}bin${sep}javac""")
        param("eclipse.test.java.home", "%${os.toLowerCase()}.java${javaVersion}.oracle.64bit%")
        param("env.JAVA_HOME", "%${os.toLowerCase()}.java${javaVersion}.oracle.64bit%")
        param("enable.oomph.plugin", "false")
        when(type) {
            "Basic"        -> param("gradle.tasks", "clean eclipseTest")
            "Full"         -> param("gradle.tasks", "clean build")
            "SanityCheck"  -> param("gradle.tasks", "assemble checkstyleMain")
            "CrossVersion" -> param("gradle.tasks", "clean crossVersionEclipseTest")
            else -> throw RuntimeException("Unrecognized type: $type")
        }
    }

    triggers {
        retryBuild {
            delaySeconds = 0
            attempts = 2
        }
    }

    steps {
        gradle {
            name = "RUNNER_21"
            id = "RUNNER_21"
            tasks = "%gradle.tasks%"
            buildFile = ""
            gradleParams = "-Peclipse.version=%eclipse.version% -Pcompiler.location='%compiler.location%' -Pbuild.invoker=%build.invoker% -Prelease.type=%eclipse.release.type% -Peclipse.test.java.home='%eclipse.test.java.home%' --info --stacktrace -Declipse.p2.mirror=false -Dscan -Pmirrors=%eclipsetest.mirrors% -Penable.oomph.plugin=%enable.oomph.plugin% \"-Dgradle.cache.remote.url=%gradle.cache.remote.url%\" \"-Dgradle.cache.remote.username=%gradle.cache.remote.username%\" \"-Dgradle.cache.remote.password=%gradle.cache.remote.password%\""
            jvmArgs = "-XX:MaxPermSize=256m"
            param("org.jfrog.artifactory.selectedDeployableServer.defaultModuleVersionConfiguration", "GLOBAL")
        }
    }

    vcs {
        root(GitHubVcsRoot)
        checkoutMode = CheckoutMode.ON_AGENT
    }

    addCredentialsLeakFailureCondition()

    cleanup {
        all(days = 5)
        history(days = 5)
        artifacts(days = 5)
        preventDependencyCleanup = false
    }

    requirements {
        contains("teamcity.agent.jvm.os.name", os)
    }
})


class PromotionBuildType(typeName: String,  dependency: BuildType, trigger: String = "none") : BuildType({
    createId("Promotion", typeName.capitalize())

    artifactRules = "org.eclipse.buildship.site/build/repository/** => .teamcity/update-site"

    params {
        when(typeName) {
            "milestone" -> text("Confirm", "NO", label = "Do you want to proceed with the milestone?", description = "Confirm to publish a new milestone.", display = ParameterDisplay.PROMPT,regex = "YES", validationMessage = "Confirm by writing YES in order to proceed.")
            "release" -> password("github.token", "", label = "GitHub token", description = "Please specify your GitHub auth token to proceed with the release", display = ParameterDisplay.PROMPT)
        }
        param("env.JAVA_HOME", "%linux.java8.oracle.64bit%")
        param("eclipse.release.type", typeName)
        param("build.invoker", "ci")
    }

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
        snapshot(dependency, DefaultFailureCondition)
    }

    steps {
        for (eclipseVersion in EclipseVersion.values()) {
            gradle {
                name = "Build and upload update site for Eclipse ${eclipseVersion.codeName} (${eclipseVersion.versionNumber})"
                tasks = "clean build uploadUpdateSite"
                buildFile = ""
                gradleParams = """
                    --exclude-task eclipseTest
                    -Peclipse.version=${eclipseVersion.updateSiteVersion} -Pcompiler.location='%linux.java8.oracle.64bit%/bin/javac' -Pbuild.invoker=%build.invoker% -Prelease.type=%eclipse.release.type% -PECLIPSE_ORG_FTP_HOST=build.eclipse.org -PECLIPSE_ORG_FTP_USER=%eclipse.downloadServer.username% -PECLIPSE_ORG_FTP_PASSWORD=%eclipse.downloadServer.password% -PECLIPSE_ORG_FTP_UPDATE_SITES_PATH=/home/data/httpd/download.eclipse.org/buildship/updates -PECLIPSE_ORG_TEMP_PATH=/home/data/httpd/download.eclipse.org/buildship/temp -PECLIPSE_ORG_MIRROR_PATH=/buildship/updates
                    --stacktrace -Declipse.p2.mirror=false
                    -Penable.oomph.plugin=false
                    "-Dgradle.cache.remote.url=%gradle.cache.remote.url%"
                    "-Dgradle.cache.remote.username=%gradle.cache.remote.username%"
                    "-Dgradle.cache.remote.password=%gradle.cache.remote.password%"
                """.trimIndent()
                param("org.jfrog.artifactory.selectedDeployableServer.defaultModuleVersionConfiguration", "GLOBAL")
            }
        }

        if (typeName == "release") {
            gradle {
                name = "Tag revision and increment version number"
                tasks = "tag incrementVersion"
                buildFile = ""
                gradleParams = """
                    --exclude-task eclipseTest
                    -Peclipse.version=45 -Pcompiler.location='%linux.java8.oracle.64bit%/bin/javac' -Pbuild.invoker=%build.invoker% -Prelease.type=%eclipse.release.type% -PECLIPSE_ORG_FTP_HOST=build.eclipse.org -PECLIPSE_ORG_FTP_USER=%eclipse.downloadServer.username% -PECLIPSE_ORG_FTP_PASSWORD=%eclipse.downloadServer.password% -PECLIPSE_ORG_FTP_UPDATE_SITES_PATH=/home/data/httpd/download.eclipse.org/buildship/updates -PECLIPSE_ORG_TEMP_PATH=/home/data/httpd/download.eclipse.org/buildship/temp -PECLIPSE_ORG_MIRROR_PATH=/buildship/updates -PgithubAccessKey=%github.token%
                    --stacktrace
                    "-Dgradle.cache.remote.url=%gradle.cache.remote.url%"
                    "-Dgradle.cache.remote.username=%gradle.cache.remote.username%"
                    "-Dgradle.cache.remote.password=%gradle.cache.remote.password%"
                """.trimIndent()
            }
        }
    }

    if (trigger == "daily") {
        triggers {
            schedule {
                schedulingPolicy = daily {
                    hour = 23
                }
                branchFilter = """
                +:*
                -:teamcity-versioned-settings
            """.trimIndent()
                triggerRules = """
                -:docs/**
                -:README.MD
            """.trimIndent()
                triggerBuild = always()
                enforceCleanCheckout = true
                param("revisionRule", "lastFinished")
                param("dayOfWeek", "Sunday")
            }
        }
    }

})

class TriggerBuildType(triggerName: String, scenarios: List<IndividualScenarioBuildType>, prev: TriggerBuildType?, trigger: String = "none") : BuildType({
    createId("Trigger", triggerName)

    if (trigger == "git") {
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
    } else if (trigger == "daily") {
        triggers {
            schedule {
                schedulingPolicy = daily {
                    hour = 4
                    timezone = "Europe/Budapest"
                }
                branchFilter = """
                    +:*
                    -:teamcity-versioned-settings
                """.trimIndent()
                triggerBuild = always()
                param("revisionRule", "lastFinished")
                param("dayOfWeek", "Sunday")
            }
        }
    }

    if (prev != null) {
        triggers {
            finishBuildTrigger {
                buildType = prev.id!!.value
                successfulOnly = true
                branchFilter = "+:*"
            }
        }

        dependencies {
             snapshot(prev, DefaultFailureCondition)
        }
    }


    dependencies {
        scenarios.forEach {
            snapshot(it, DefaultFailureCondition)
        }
    }
})

object Project : Project({
    description = "Eclipse plugins for Gradle http://eclipse.org/buildship"

    vcsRoot(GitHubVcsRoot)

    params {
        param("env.JAVA_TOOL_OPTIONS", "-Dfile.encoding=UTF8")
        password("eclipse.downloadServer.password", "credentialsJSON:e600b1ef-46bb-4ced-b45b-795da6964956", label = "Password", display = ParameterDisplay.HIDDEN)
        password("eclipse.downloadServer.username", "credentialsJSON:23f3947f-45b2-46b8-83f6-9341c9b914f6", label = "Username", display = ParameterDisplay.HIDDEN)
        // Do not allow UI changes in the TeamCity configuration
        param("teamcity.ui.settings.readOnly", "true")
    }

    cleanup {
        all(days = 5)
        history(days = 5)
        artifacts(days = 5)
        preventDependencyCleanup = false
    }

    subProject(BuildshipBuilds)
    subProject(BuildshipTriggers)
    subProject(BuildshipPromotions)
})

object BuildshipBuilds : Project({
    name = "Individual coverage scenarios"
    id("Buildship_individual_coverage_scenarios")

    // TODO both EclipseBuildTemplate and the other template should be here
    buildTypesWithOrder(individualBuildsForPhase1 + individualBuildsForPhase2 + individualBuildsForPhase3 + individualBuildsForPhase4)
})

object BuildshipTriggers : Project({
    name = "Triggers"
    id("Buildship_triggers")
    buildTypesWithOrder(listOf(
        tb1_1,
        tb1_2, tb2_2,
        tb3_1, tb3_2, tb3_3,
        tb4_1, tb4_2, tb4_3, tb4_4))
})

object BuildshipPromotions : Project({
    name = "Promotions"
    id("Buildship_promotions")

    buildTypesWithOrder(listOf(snapshotPromotion, milestonePromotion, releasePromotion))
})

private fun Project.buildTypesWithOrder(buildTypes: List<BuildType>) {
    buildTypes.forEach { bt ->
        buildType(bt)
    }
    buildTypesOrder = buildTypes
}

private fun BuildType.createId(type: String, name: String)  {
    this.name = name
    id("${type}_${name.replace(" ", "_").replace("-", "_").replace("(", "").replace(")", "").replace("/", "").replace(",", "")}")
}

private val DefaultFailureCondition : SnapshotDependency.() -> Unit
    get() =  {
        onDependencyCancel = FailureAction.ADD_PROBLEM
        onDependencyFailure = FailureAction.FAIL_TO_START
    }

enum class EclipseVersion(val codeName: String, val versionNumber: String) {
    Eclipse_4_3("Kepler", "4.3"),
    Eclipse_4_4("Luna", "4.4"),
    Eclipse_4_5("Mars", "4.5"),
    Eclipse_4_6("Neon", "4.6"),
    Eclipse_4_7("Oxygen", "4.7"),
    Eclipse_4_8("Photon", "4.8"),
    Eclipse_4_9("2018-09", "4.9"),
    Eclipse_4_10("2018-12", "4.10"),
    Eclipse_4_11("2019-03", "4.11"),
    Eclipse_4_12("2019-06", "4.12"),
    Eclipse_4_13("2019-09", "4.13"),
    Eclipse_4_14("2019-12", "4.14"),
    Eclipse_4_15("2020-03", "4.15"),
    Eclipse_4_16("2020-06", "4.16");

    val updateSiteVersion: String
        get() = versionNumber.replace(".", "")
}