package Buildship

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.Project
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.*

val individualBuildsForPhase1 = listOf(
    IndividualScenarioBuildType(ScenarioType.SANITY_CHECK, OS.LINUX, EclipseVersion.ECLIPSE4_13, eclipseRuntimeJdk = Jdk.ORACLE_JDK_8) // TODO use latest Eclipse version for sanity check coverage
)
val individualBuildsForPhase2 = listOf(
    IndividualScenarioBuildType(ScenarioType.BASIC_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_8, eclipseRuntimeJdk = Jdk.ORACLE_JDK_8),
    IndividualScenarioBuildType(ScenarioType.BASIC_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_20, eclipseRuntimeJdk = Jdk.OPEN_JDK_11),
    IndividualScenarioBuildType(ScenarioType.BASIC_COVERAGE, OS.WINDOWS, EclipseVersion.ECLIPSE4_8, eclipseRuntimeJdk = Jdk.ORACLE_JDK_8),
    IndividualScenarioBuildType(ScenarioType.BASIC_COVERAGE, OS.WINDOWS, EclipseVersion.ECLIPSE4_23, eclipseRuntimeJdk = Jdk.OPEN_JDK_11)
)
val individualBuildsForPhase3 = listOf(
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_8, eclipseRuntimeJdk = Jdk.ORACLE_JDK_8),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_9, eclipseRuntimeJdk = Jdk.ORACLE_JDK_8),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_10, eclipseRuntimeJdk = Jdk.ORACLE_JDK_8),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_11, eclipseRuntimeJdk = Jdk.ORACLE_JDK_8),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_12, eclipseRuntimeJdk = Jdk.ORACLE_JDK_8),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_13, eclipseRuntimeJdk = Jdk.ORACLE_JDK_8),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_14, eclipseRuntimeJdk = Jdk.ORACLE_JDK_8),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_15, eclipseRuntimeJdk = Jdk.ORACLE_JDK_8),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_16, eclipseRuntimeJdk = Jdk.ORACLE_JDK_8),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_17, eclipseRuntimeJdk = Jdk.OPEN_JDK_11),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_18, eclipseRuntimeJdk = Jdk.OPEN_JDK_11),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_19, eclipseRuntimeJdk = Jdk.OPEN_JDK_11),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_20, eclipseRuntimeJdk = Jdk.OPEN_JDK_11),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_21, eclipseRuntimeJdk = Jdk.OPEN_JDK_11),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_22, eclipseRuntimeJdk = Jdk.OPEN_JDK_11),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_23, eclipseRuntimeJdk = Jdk.OPEN_JDK_11),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_24, eclipseRuntimeJdk = Jdk.OPEN_JDK_11),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_25, eclipseRuntimeJdk = Jdk.OPEN_JDK_17),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_26, eclipseRuntimeJdk = Jdk.OPEN_JDK_17),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.LINUX, EclipseVersion.ECLIPSE4_27, eclipseRuntimeJdk = Jdk.OPEN_JDK_17),

    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.WINDOWS, EclipseVersion.ECLIPSE4_8, eclipseRuntimeJdk = Jdk.ORACLE_JDK_8),
    IndividualScenarioBuildType(ScenarioType.FULL_COVERAGE, OS.WINDOWS, EclipseVersion.ECLIPSE4_23, eclipseRuntimeJdk = Jdk.OPEN_JDK_11)
)
val individualBuildsForPhase4 = listOf(
    IndividualScenarioBuildType(ScenarioType.CROSS_VERSION, OS.LINUX, EclipseVersion.ECLIPSE4_8, eclipseRuntimeJdk = Jdk.ORACLE_JDK_8),
    // TODO Eclipse 4.8 can only run on Java 8 and below without further configuration https://wiki.eclipse.org/Configure_Eclipse_for_Java_9
    //IndividualScenarioBuildType(ScenarioType.CROSS_VERSION, OS.LINUX, EclipseVersion.ECLIPSE4_8, eclipseRuntimeJdk = Jdk.ORACLE_JDK_9),
    //IndividualScenarioBuildType(ScenarioType.CROSS_VERSION, OS.LINUX, EclipseVersion.ECLIPSE4_8, eclipseRuntimeJdk = Jdk.OPEN_JDK_10),
    IndividualScenarioBuildType(ScenarioType.CROSS_VERSION, OS.LINUX, EclipseVersion.ECLIPSE4_23, eclipseRuntimeJdk = Jdk.OPEN_JDK_11),
    IndividualScenarioBuildType(ScenarioType.CROSS_VERSION, OS.LINUX, EclipseVersion.ECLIPSE4_23, eclipseRuntimeJdk = Jdk.OPEN_JDK_12),
    IndividualScenarioBuildType(ScenarioType.CROSS_VERSION, OS.LINUX, EclipseVersion.ECLIPSE4_23, eclipseRuntimeJdk = Jdk.OPEN_JDK_13),
    IndividualScenarioBuildType(ScenarioType.CROSS_VERSION, OS.LINUX, EclipseVersion.ECLIPSE4_23, eclipseRuntimeJdk = Jdk.OPEN_JDK_14),
    IndividualScenarioBuildType(ScenarioType.CROSS_VERSION, OS.LINUX, EclipseVersion.ECLIPSE4_23, eclipseRuntimeJdk = Jdk.OPEN_JDK_15),
    IndividualScenarioBuildType(ScenarioType.CROSS_VERSION, OS.LINUX, EclipseVersion.ECLIPSE4_23, eclipseRuntimeJdk = Jdk.OPEN_JDK_16),
    IndividualScenarioBuildType(ScenarioType.CROSS_VERSION, OS.LINUX, EclipseVersion.ECLIPSE4_23, eclipseRuntimeJdk = Jdk.OPEN_JDK_17),
    // TODO JDK 18 is only supported in Eclipse 4.24
    //IndividualScenarioBuildType(ScenarioType.CROSS_VERSION, OS.LINUX, EclipseVersion.ECLIPSE4_23, eclipseRuntimeJdk = Jdk.OPEN_JDK_18)
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

val unsafeSnapshotPromotion = PromotionBuildType("snapshot (from sanity check)","snapshot",  tb1_1)
val snapshotPromotion = PromotionBuildType("snapshot", "snapshot", tb4_4, Trigger.DAILY_MASTER)
val milestonePromotion = PromotionBuildType("milestone","milestone", tb4_4)
val releasePromotion = PromotionBuildType("release","release", tb4_4)
val individualSnapshotPromotions = EclipseVersion.values().map { SinglePromotionBuildType("Snapshot Eclipse ${it.codeName}", "snapshot", it, tb1_1) } // TODO should depend on tb4_4
val individualReleasePromotions = EclipseVersion.values().map { SinglePromotionBuildType("Release Eclipse ${it.codeName}", "release", it, tb1_1) } // TODO should depend on tb4_4


class IndividualScenarioBuildType(type: ScenarioType, os: OS, eclipseVersion: EclipseVersion, eclipseRuntimeJdk: Jdk) : BuildType({
    createId("Individual", "${type.name.toLowerCase()}_Test_Coverage_${os.name.toLowerCase()}_Eclipse${eclipseVersion.versionNumber}_OnJava${eclipseRuntimeJdk.majorVersion}")
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
        param("eclipse.test.java.version", eclipseRuntimeJdk.majorVersion)
        param("env.JAVA_HOME", Jdk.OPEN_JDK_11.getJavaHomePath(os))
        param("gradle.tasks", type.gradleTasks)
        param("repository.mirrors", allMirrors())
        param("env.GRADLE_ENTERPRISE_ACCESS_KEY", "%ge.gradle.org.access.key%")
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
            gradleParams = "-Prepository.mirrors=\"%repository.mirrors%\" -Peclipse.version=%eclipse.version% -Pbuild.invoker=%build.invoker% -Prelease.type=%eclipse.release.type% -Peclipse.test.java.version=%eclipse.test.java.version% --info --stacktrace -Declipse.p2.mirror=false -Dscan \"-Dgradle.cache.remote.url=%gradle.cache.remote.url%\" \"-Dgradle.cache.remote.username=%gradle.cache.remote.username%\" \"-Dgradle.cache.remote.password=%gradle.cache.remote.password%\" ${Jdk.javaInstallationPathsProperty(os)}"
            jvmArgs = "-XX:MaxPermSize=256m"
            param("org.jfrog.artifactory.selectedDeployableServer.defaultModuleVersionConfiguration", "GLOBAL")
            jdkHome
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
        doesNotMatch("teamcity.agent.name", "ec2-.*")
    }
})

class PromotionBuildType(promotionName: String, typeName: String, dependency: BuildType, trigger: Trigger = Trigger.NONE) : BuildType({
    createId("Promotion", promotionName.capitalize())
    artifactRules = "org.eclipse.buildship.site/build/repository/** => .teamcity/update-site"
    trigger.applyOn(this)
    addCredentialsLeakFailureCondition()

    params {
        when(typeName) {
            "milestone" -> text("Confirm", "NO", label = "Do you want to proceed with the milestone?", description = "Confirm to publish a new milestone.", display = ParameterDisplay.PROMPT,regex = "YES", validationMessage = "Confirm by writing YES in order to proceed.")
            "release" -> password("github.token", "", label = "GitHub token", description = "Please specify your GitHub auth token to proceed with the release", display = ParameterDisplay.PROMPT)
        }
        param("eclipse.release.type", typeName)
        param("build.invoker", "ci")
        param("env.JAVA_HOME", Jdk.OPEN_JDK_11.getJavaHomePath(OS.LINUX))
        param("repository.mirrors", allMirrors())
        param("env.GRADLE_ENTERPRISE_ACCESS_KEY", "%ge.gradle.org.access.key%")
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
                gradleParams = "-Prepository.mirrors=\"%repository.mirrors%\" --exclude-task eclipseTest -Peclipse.version=${eclipseVersion.updateSiteVersion} -Pbuild.invoker=%build.invoker% -Prelease.type=%eclipse.release.type% -PECLIPSE_ORG_FTP_HOST=projects-storage.eclipse.org -PECLIPSE_ORG_FTP_USER=%eclipse.downloadServer.username% -PECLIPSE_ORG_FTP_PASSWORD=%eclipse.downloadServer.password% -PECLIPSE_ORG_FTP_UPDATE_SITES_PATH=downloads/buildship/updates -PECLIPSE_ORG_TEMP_PATH=tmp -PECLIPSE_ORG_MIRROR_PATH=/buildship/updates --stacktrace -Declipse.p2.mirror=false \"-Dgradle.cache.remote.url=%gradle.cache.remote.url%\" \"-Dgradle.cache.remote.username=%gradle.cache.remote.username%\" \"-Dgradle.cache.remote.password=%gradle.cache.remote.password%\" ${Jdk.javaInstallationPathsProperty(OS.LINUX)}"
                param("org.jfrog.artifactory.selectedDeployableServer.defaultModuleVersionConfiguration", "GLOBAL")
            }
        }

        if (typeName == "release") {
            gradle {
                name = "Tag revision and increment version number"
                tasks = "tag incrementVersion"
                buildFile = ""
                gradleParams = "-Prepository.mirrors=\"%repository.mirrors%\" --exclude-task eclipseTest -Peclipse.version=45 -Pbuild.invoker=%build.invoker% -Prelease.type=%eclipse.release.type% -PECLIPSE_ORG_FTP_HOST=projects-storage.eclipse.org -PECLIPSE_ORG_FTP_USER=%eclipse.downloadServer.username% -PECLIPSE_ORG_FTP_PASSWORD=%eclipse.downloadServer.password% -PECLIPSE_ORG_FTP_UPDATE_SITES_PATH=downloads/buildship/updates -PECLIPSE_ORG_TEMP_PATH=tmp -PECLIPSE_ORG_MIRROR_PATH=/buildship/updates -PgithubAccessKey=%github.token% --stacktrace \"-Dgradle.cache.remote.url=%gradle.cache.remote.url%\" \"-Dgradle.cache.remote.username=%gradle.cache.remote.username%\" \"-Dgradle.cache.remote.password=%gradle.cache.remote.password%\" ${Jdk.javaInstallationPathsProperty(OS.LINUX)}"
            }
        }
    }
})

class SinglePromotionBuildType(promotionName: String, typeName: String, eclipseVersion: EclipseVersion, dependency: BuildType, trigger: Trigger = Trigger.NONE) : BuildType({
    createId("Promotion", promotionName.capitalize())
    artifactRules = "org.eclipse.buildship.site/build/repository/** => .teamcity/update-site"
    trigger.applyOn(this)
    addCredentialsLeakFailureCondition()

    params {
        when(typeName) {
            "milestone" -> text("Confirm", "NO", label = "Do you want to proceed with the milestone?", description = "Confirm to publish a new milestone.", display = ParameterDisplay.PROMPT,regex = "YES", validationMessage = "Confirm by writing YES in order to proceed.")
            "release" -> password("github.token", "", label = "GitHub token", description = "Please specify your GitHub auth token to proceed with the release", display = ParameterDisplay.PROMPT)
        }
        param("eclipse.release.type", typeName)
        param("build.invoker", "ci")
        param("env.JAVA_HOME", Jdk.OPEN_JDK_11.getJavaHomePath(OS.LINUX))
        param("repository.mirrors", allMirrors())
        param("env.GRADLE_ENTERPRISE_ACCESS_KEY", "%ge.gradle.org.access.key%")
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
        gradle {
            name = "Build and upload update site for Eclipse ${eclipseVersion.codeName} (${eclipseVersion.versionNumber})"
            tasks = "clean build uploadUpdateSite"
            buildFile = ""
            gradleParams = "-Prepository.mirrors=\"%repository.mirrors%\" --exclude-task eclipseTest -Peclipse.version=${eclipseVersion.updateSiteVersion} -Pbuild.invoker=%build.invoker% -Prelease.type=%eclipse.release.type% -PECLIPSE_ORG_FTP_HOST=projects-storage.eclipse.org -PECLIPSE_ORG_FTP_USER=%eclipse.downloadServer.username% -PECLIPSE_ORG_FTP_PASSWORD=%eclipse.downloadServer.password% -PECLIPSE_ORG_FTP_UPDATE_SITES_PATH=downloads/buildship/updates -PECLIPSE_ORG_TEMP_PATH=tmp -PECLIPSE_ORG_MIRROR_PATH=/buildship/updates --stacktrace -Declipse.p2.mirror=false \"-Dgradle.cache.remote.url=%gradle.cache.remote.url%\" \"-Dgradle.cache.remote.username=%gradle.cache.remote.username%\" \"-Dgradle.cache.remote.password=%gradle.cache.remote.password%\" ${Jdk.javaInstallationPathsProperty(OS.LINUX)}"
            param("org.jfrog.artifactory.selectedDeployableServer.defaultModuleVersionConfiguration", "GLOBAL")
        }

        if (typeName == "release" && eclipseVersion.isLatest) {
            gradle {
                name = "Tag revision and increment version number"
                tasks = "tag incrementVersion"
                buildFile = ""
                gradleParams = "-Prepository.mirrors=\"%repository.mirrors%\" --exclude-task eclipseTest -Peclipse.version=45 -Pbuild.invoker=%build.invoker% -Prelease.type=%eclipse.release.type% -PECLIPSE_ORG_FTP_HOST=projects-storage.eclipse.org -PECLIPSE_ORG_FTP_USER=%eclipse.downloadServer.username% -PECLIPSE_ORG_FTP_PASSWORD=%eclipse.downloadServer.password% -PECLIPSE_ORG_FTP_UPDATE_SITES_PATH=downloads/buildship/updates -PECLIPSE_ORG_TEMP_PATH=tmp -PECLIPSE_ORG_MIRROR_PATH=/buildship/updates -PgithubAccessKey=%github.token% --stacktrace \"-Dgradle.cache.remote.url=%gradle.cache.remote.url%\" \"-Dgradle.cache.remote.username=%gradle.cache.remote.username%\" \"-Dgradle.cache.remote.password=%gradle.cache.remote.password%\" ${Jdk.javaInstallationPathsProperty(OS.LINUX)}"
            }
        }
    }
})


class CheckpointBuildType(triggerName: String, scenarios: List<IndividualScenarioBuildType>, previousCheckpoint: CheckpointBuildType?, trigger: Trigger = Trigger.NONE) : BuildType({
    createId("Checkpoint", triggerName)
    trigger.applyOn(this)

    vcs {
        root(GitHubVcsRoot)

        checkoutMode = CheckoutMode.ON_AGENT
        cleanCheckout = true
        showDependenciesChanges = true
    }

    if (previousCheckpoint != null) {
        triggers {
            finishBuildTrigger {
                buildType = previousCheckpoint.id!!.value
                successfulOnly = true
                branchFilter = "+:*"
            }
        }

        dependencies {
             snapshot(previousCheckpoint, FailWhenDependenciesFail)
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
        param("env.GRADLE_ENTERPRISE_ACCESS_KEY", "%ge.gradle.org.access.key%")
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
    buildTypesWithOrder(listOf(unsafeSnapshotPromotion, snapshotPromotion, milestonePromotion, releasePromotion) + individualSnapshotPromotions + individualReleasePromotions)
})


fun allMirrors() = EclipseVersion.values()
    .map { "https://download.eclipse.org/releases/${it.codeName.toLowerCase()}->https://repo.grdev.net/artifactory/eclipse-ide/releases/${it.codeName.toLowerCase()}" }
    .joinToString(",") +
        ",https://download.eclipse.org/tools/orbit/downloads/drops/R20210602031627/repository->https://repo.grdev.net/artifactory/eclipse-orbit" +
        ",https://download.eclipse.org/technology/swtbot/releases/2.2.1->https://repo.grdev.net/artifactory/eclipse-swtbot"
