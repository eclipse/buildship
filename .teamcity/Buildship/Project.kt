package Buildship

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.Project
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.*

val individualBuildsForPhase1 = listOf(
    IndividualScenarioBuildType(ScenarioType.SANITY_CHECK, OS.LINUX, EclipseVersion.ECLIPSE4_13, Jdk.ORACLE_JDK_8)
)
val individualBuildsForPhase2 = listOf(
    IndividualScenarioBuildType(ScenarioType.BASIC_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_8, Jdk.ORACLE_JDK_8),
    IndividualScenarioBuildType(ScenarioType.BASIC_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_16, Jdk.ORACLE_JDK_8),
    IndividualScenarioBuildType(ScenarioType.BASIC_COVERAGE, OS.WINDOWS, EclipseVersion.ECLIPSE4_8, Jdk.ORACLE_JDK_8),
    IndividualScenarioBuildType(ScenarioType.BASIC_COVERAGE, OS.WINDOWS, EclipseVersion.ECLIPSE4_16, Jdk.ORACLE_JDK_8)
)
val individualBuildsForPhase3 = listOf(
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_8, Jdk.ORACLE_JDK_8),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_9, Jdk.ORACLE_JDK_8),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_10, Jdk.ORACLE_JDK_8),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_11, Jdk.ORACLE_JDK_8),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_12, Jdk.ORACLE_JDK_8),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_13, Jdk.ORACLE_JDK_8),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_14, Jdk.ORACLE_JDK_8),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_15, Jdk.ORACLE_JDK_8),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_16, Jdk.ORACLE_JDK_8),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.WINDOWS, EclipseVersion.ECLIPSE4_8, Jdk.ORACLE_JDK_8),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.WINDOWS, EclipseVersion.ECLIPSE4_16, Jdk.ORACLE_JDK_8)
)
val individualBuildsForPhase4 = listOf(
    IndividualScenarioBuildType(ScenarioType.CROSS_VERSION, OS.LINUX, EclipseVersion.ECLIPSE4_8, Jdk.ORACLE_JDK_9)
)

val tb1_1 = CheckpointBuildType("Sanity Check (Phase 1/1)", individualBuildsForPhase1, null)
val tb1_2 = CheckpointBuildType("Basic Test Coverage (Phase 1/2)", individualBuildsForPhase1, null)
val tb2_2 = CheckpointBuildType("Basic Test Coverage (Phase 2/2)", individualBuildsForPhase2, tb1_2)
val tb3_1 = CheckpointBuildType("Full Test Coverage (Phase 1/3)", individualBuildsForPhase1, null, Trigger.GIT)
val tb3_2 = CheckpointBuildType("Full Test Coverage (Phase 2/3)", individualBuildsForPhase2, tb3_1)
val tb3_3 = CheckpointBuildType("Full Test Coverage (Phase 3/3)", individualBuildsForPhase3, tb3_2)
val tb4_1 = CheckpointBuildType("Cross-Version Test Coverage (Phase 1/4)", individualBuildsForPhase1, null, Trigger.DAILY)
val tb4_2 = CheckpointBuildType("Cross-Version Test Coverage (Phase 2/4)", individualBuildsForPhase2, tb4_1)
val tb4_3 = CheckpointBuildType("Cross-Version Test Coverage (Phase 3/4)", individualBuildsForPhase3, tb4_2)
val tb4_4 = CheckpointBuildType("Cross-Version Test Coverage (Phase 4/4)", individualBuildsForPhase4, tb4_3)

val snapshotPromotion = PromotionBuildType("snapshot", tb4_4, Trigger.DAILY_MASTER)
val milestonePromotion = PromotionBuildType("milestone", tb4_4)
val releasePromotion = PromotionBuildType("release", tb4_4)

class IndividualScenarioBuildType(type: ScenarioType, os: OS, eclipseVersion: EclipseVersion, jdk: Jdk) : BuildType({
    createId("Individual", "${type.name.toLowerCase()}_Test_Coverage_${os.name.toLowerCase()}_Eclipse${eclipseVersion.versionNumber}_Java${jdk.majorVersion}")
    addCredentialsLeakFailureCondition()

    artifactRules = """
        org.eclipse.buildship.site/build/repository/** => .teamcity/update-site
        org.eclipse.buildship.core.test/build/eclipseTest/workspace/.metadata/.log => .teamcity/test/org.eclipse.buildship.core.test
        org.eclipse.buildship.ui.test/build/eclipseTest/workspace/.metadata/.log => .teamcity/test/org.eclipse.buildship.ui.test
    """.trimIndent()

    params {
        param("eclipse.release.type", "snapshot")
        param("build.invoker", "ci")
        param("eclipse.version", eclipseVersion.updateSiteVersion)
        param("compiler.location", jdk.getJavaCompilerPath(os))
        param("eclipse.test.java.home", jdk.getJavaHomePath(os))
        param("env.JAVA_HOME", jdk.getJavaHomePath(os))
        param("enable.oomph.plugin", "false")
        param("gradle.tasks", type.gradleTasks)
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
            gradleParams = "-Peclipse.version=%eclipse.version% -Pcompiler.location='%compiler.location%' -Pbuild.invoker=%build.invoker% -Prelease.type=%eclipse.release.type% -Peclipse.test.java.home='%eclipse.test.java.home%' --info --stacktrace -Declipse.p2.mirror=false -Dscan -Penable.oomph.plugin=%enable.oomph.plugin% \"-Dgradle.cache.remote.url=%gradle.cache.remote.url%\" \"-Dgradle.cache.remote.username=%gradle.cache.remote.username%\" \"-Dgradle.cache.remote.password=%gradle.cache.remote.password%\""
            jvmArgs = "-XX:MaxPermSize=256m"
            param("org.jfrog.artifactory.selectedDeployableServer.defaultModuleVersionConfiguration", "GLOBAL")
        }
    }

    vcs {
        root(GitHubVcsRoot)
        checkoutMode = CheckoutMode.ON_AGENT
    }

    cleanup {
        all(days = 5)
        history(days = 5)
        artifacts(days = 5)
        preventDependencyCleanup = false
    }

    requirements {
        contains("teamcity.agent.jvm.os.name", os.name.toLowerCase().capitalize())
    }
})

class PromotionBuildType(typeName: String,  dependency: BuildType, trigger: Trigger = Trigger.NONE) : BuildType({
    createId("Promotion", typeName.capitalize())
    artifactRules = "org.eclipse.buildship.site/build/repository/** => .teamcity/update-site"
    trigger.applyOn(this)
    addCredentialsLeakFailureCondition()

    params {
        when(typeName) {
            "milestone" -> text("Confirm", "NO", label = "Do you want to proceed with the milestone?", description = "Confirm to publish a new milestone.", display = ParameterDisplay.PROMPT,regex = "YES", validationMessage = "Confirm by writing YES in order to proceed.")
            "release" -> password("github.token", "", label = "GitHub token", description = "Please specify your GitHub auth token to proceed with the release", display = ParameterDisplay.PROMPT)
        }
        param("env.JAVA_HOME", "%linux.java8.oracle.64bit%")
        param("eclipse.release.type", typeName)
        param("build.invoker", "ci")
    }

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
})

class CheckpointBuildType(triggerName: String, scenarios: List<IndividualScenarioBuildType>, previousCheckpoint: CheckpointBuildType?, trigger: Trigger = Trigger.NONE) : BuildType({
    createId("Checkpoint", triggerName)
    trigger.applyOn(this)

    if (previousCheckpoint != null) {
        triggers {
            finishBuildTrigger {
                buildType = previousCheckpoint.id!!.value
                successfulOnly = true
                branchFilter = "+:*"
            }
        }

        dependencies {
             snapshot(previousCheckpoint, DefaultFailureCondition)
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
    subprojectsWithOrder(listOf(IndividualBuilds, Checkpoints, Promotions))

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
})

object IndividualBuilds : Project({
    createId("Individual Coverage Scenarios")
    buildTypesWithOrder(individualBuildsForPhase1 + individualBuildsForPhase2 + individualBuildsForPhase3 + individualBuildsForPhase4)
})

object Checkpoints : Project({
    createId("Checkpoints")
    buildTypesWithOrder(listOf(
        tb1_1,
        tb1_2, tb2_2,
        tb3_1, tb3_2, tb3_3,
        tb4_1, tb4_2, tb4_3, tb4_4))
})

object Promotions : Project({
    createId("Promotions")
    buildTypesWithOrder(listOf(snapshotPromotion, milestonePromotion, releasePromotion))
})

