package Buildship.Promotion30.buildTypes

import Buildship.Promotion30.Promotion30Template
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.schedule

object Snapshot : BuildType({
    id("Promote30_Snapshot")
    name = "Promote Snapshot"

    templates(Promotion30Template)

    params {
        param("env.JAVA_HOME", "%linux.java8.oracle.64bit%")
        param("eclipse.release.type", "snapshot")
        param("build.invoker", "ci")
    }

    steps {
        gradle {
            name = "Build and upload update site for Eclipse 4.3 (Kepler)"
            tasks = "clean build uploadUpdateSite"
            buildFile = ""
            gradleParams = """
                --exclude-task eclipseTest
                -Peclipse.version=43 -Pcompiler.location='%linux.java8.oracle.64bit%/bin/javac' -Pbuild.invoker=%build.invoker% -Prelease.type=%eclipse.release.type% -PECLIPSE_ORG_FTP_HOST=build.eclipse.org -PECLIPSE_ORG_FTP_USER=%eclipse.downloadServer.username% -PECLIPSE_ORG_FTP_PASSWORD=%eclipse.downloadServer.password% -PECLIPSE_ORG_FTP_UPDATE_SITES_PATH=/home/data/httpd/download.eclipse.org/buildship/updates -PECLIPSE_ORG_TEMP_PATH=/home/data/httpd/download.eclipse.org/buildship/temp -PECLIPSE_ORG_MIRROR_PATH=/buildship/updates
                --stacktrace -Declipse.p2.mirror=false
                -Penable.oomph.plugin=false
                "-Dgradle.cache.remote.url=%gradle.cache.remote.url%"
                "-Dgradle.cache.remote.username=%gradle.cache.remote.username%"
                "-Dgradle.cache.remote.password=%gradle.cache.remote.password%"
            """.trimIndent()
            param("org.jfrog.artifactory.selectedDeployableServer.defaultModuleVersionConfiguration", "GLOBAL")
        }
        gradle {
            name = "Build and upload update site for Eclipse 4.4 (Luna)"
            tasks = "clean build uploadUpdateSite"
            buildFile = ""
            gradleParams = """
                --exclude-task eclipseTest
                -Peclipse.version=44 -Pcompiler.location='%linux.java8.oracle.64bit%/bin/javac' -Pbuild.invoker=%build.invoker% -Prelease.type=%eclipse.release.type% -PECLIPSE_ORG_FTP_HOST=build.eclipse.org -PECLIPSE_ORG_FTP_USER=%eclipse.downloadServer.username% -PECLIPSE_ORG_FTP_PASSWORD=%eclipse.downloadServer.password% -PECLIPSE_ORG_FTP_UPDATE_SITES_PATH=/home/data/httpd/download.eclipse.org/buildship/updates -PECLIPSE_ORG_TEMP_PATH=/home/data/httpd/download.eclipse.org/buildship/temp -PECLIPSE_ORG_MIRROR_PATH=/buildship/updates
                --stacktrace -Declipse.p2.mirror=false
                -Penable.oomph.plugin=false
                "-Dgradle.cache.remote.url=%gradle.cache.remote.url%"
                "-Dgradle.cache.remote.username=%gradle.cache.remote.username%"
                "-Dgradle.cache.remote.password=%gradle.cache.remote.password%"
            """.trimIndent()
            param("org.jfrog.artifactory.selectedDeployableServer.defaultModuleVersionConfiguration", "GLOBAL")
        }
        gradle {
            name = "Build and upload update site for Eclipse 4.5 (Mars)"
            tasks = "clean build uploadUpdateSite"
            buildFile = ""
            gradleParams = """
                --exclude-task eclipseTest
                -Peclipse.version=45 -Pcompiler.location='%linux.java8.oracle.64bit%/bin/javac' -Pbuild.invoker=%build.invoker% -Prelease.type=%eclipse.release.type% -PECLIPSE_ORG_FTP_HOST=build.eclipse.org -PECLIPSE_ORG_FTP_USER=%eclipse.downloadServer.username% -PECLIPSE_ORG_FTP_PASSWORD=%eclipse.downloadServer.password% -PECLIPSE_ORG_FTP_UPDATE_SITES_PATH=/home/data/httpd/download.eclipse.org/buildship/updates -PECLIPSE_ORG_TEMP_PATH=/home/data/httpd/download.eclipse.org/buildship/temp -PECLIPSE_ORG_MIRROR_PATH=/buildship/updates
                --stacktrace -Declipse.p2.mirror=false
                "-Dgradle.cache.remote.url=%gradle.cache.remote.url%"
                "-Dgradle.cache.remote.username=%gradle.cache.remote.username%"
                "-Dgradle.cache.remote.password=%gradle.cache.remote.password%"
            """.trimIndent()
            param("org.jfrog.artifactory.selectedDeployableServer.defaultModuleVersionConfiguration", "GLOBAL")
        }
        gradle {
            name = "Build and upload update site for Eclipse 4.6 (Neon)"
            tasks = "clean build uploadUpdateSite"
            buildFile = ""
            gradleParams = """
                --exclude-task eclipseTest
                -Peclipse.version=46 -Pcompiler.location='%linux.java8.oracle.64bit%/bin/javac' -Pbuild.invoker=%build.invoker% -Prelease.type=%eclipse.release.type% -PECLIPSE_ORG_FTP_HOST=build.eclipse.org -PECLIPSE_ORG_FTP_USER=%eclipse.downloadServer.username% -PECLIPSE_ORG_FTP_PASSWORD=%eclipse.downloadServer.password% -PECLIPSE_ORG_FTP_UPDATE_SITES_PATH=/home/data/httpd/download.eclipse.org/buildship/updates -PECLIPSE_ORG_TEMP_PATH=/home/data/httpd/download.eclipse.org/buildship/temp -PECLIPSE_ORG_MIRROR_PATH=/buildship/updates
                --stacktrace -Declipse.p2.mirror=false
                "-Dgradle.cache.remote.url=%gradle.cache.remote.url%"
                "-Dgradle.cache.remote.username=%gradle.cache.remote.username%"
                "-Dgradle.cache.remote.password=%gradle.cache.remote.password%"
            """.trimIndent()
            param("org.jfrog.artifactory.selectedDeployableServer.defaultModuleVersionConfiguration", "GLOBAL")
        }
        gradle {
            name = "Build and upload update site for Eclipse 4.7 (Oxygen)"
            tasks = "clean build uploadUpdateSite"
            buildFile = ""
            gradleParams = """
                --exclude-task eclipseTest
                -Peclipse.version=47 -Pcompiler.location='%linux.java8.oracle.64bit%/bin/javac' -Pbuild.invoker=%build.invoker% -Prelease.type=%eclipse.release.type% -PECLIPSE_ORG_FTP_HOST=build.eclipse.org -PECLIPSE_ORG_FTP_USER=%eclipse.downloadServer.username% -PECLIPSE_ORG_FTP_PASSWORD=%eclipse.downloadServer.password% -PECLIPSE_ORG_FTP_UPDATE_SITES_PATH=/home/data/httpd/download.eclipse.org/buildship/updates -PECLIPSE_ORG_TEMP_PATH=/home/data/httpd/download.eclipse.org/buildship/temp -PECLIPSE_ORG_MIRROR_PATH=/buildship/updates
                --stacktrace -Declipse.p2.mirror=false
                "-Dgradle.cache.remote.url=%gradle.cache.remote.url%"
                "-Dgradle.cache.remote.username=%gradle.cache.remote.username%"
                "-Dgradle.cache.remote.password=%gradle.cache.remote.password%"
            """.trimIndent()
            param("org.jfrog.artifactory.selectedDeployableServer.defaultModuleVersionConfiguration", "GLOBAL")
        }
        gradle {
            name = "Build and upload update site for Eclipse 4.8 (Photon)"
            tasks = "clean build uploadUpdateSite"
            buildFile = ""
            gradleParams = """
                --exclude-task eclipseTest
                -Peclipse.version=48 -Pcompiler.location='%linux.java8.oracle.64bit%/bin/javac' -Pbuild.invoker=%build.invoker% -Prelease.type=%eclipse.release.type% -PECLIPSE_ORG_FTP_HOST=build.eclipse.org -PECLIPSE_ORG_FTP_USER=%eclipse.downloadServer.username% -PECLIPSE_ORG_FTP_PASSWORD=%eclipse.downloadServer.password% -PECLIPSE_ORG_FTP_UPDATE_SITES_PATH=/home/data/httpd/download.eclipse.org/buildship/updates -PECLIPSE_ORG_TEMP_PATH=/home/data/httpd/download.eclipse.org/buildship/temp -PECLIPSE_ORG_MIRROR_PATH=/buildship/updates
                --stacktrace -Declipse.p2.mirror=false
                "-Dgradle.cache.remote.url=%gradle.cache.remote.url%"
                "-Dgradle.cache.remote.username=%gradle.cache.remote.username%"
                "-Dgradle.cache.remote.password=%gradle.cache.remote.password%"
            """.trimIndent()
        }
        gradle {
            name = "Build and upload update site for Eclipse 2018-09"
            tasks = "clean build uploadUpdateSite"
            buildFile = ""
            gradleParams = """
                --exclude-task eclipseTest
                -Peclipse.version=49 -Pcompiler.location='%linux.java8.oracle.64bit%/bin/javac' -Pbuild.invoker=%build.invoker% -Prelease.type=%eclipse.release.type% -PECLIPSE_ORG_FTP_HOST=build.eclipse.org -PECLIPSE_ORG_FTP_USER=%eclipse.downloadServer.username% -PECLIPSE_ORG_FTP_PASSWORD=%eclipse.downloadServer.password% -PECLIPSE_ORG_FTP_UPDATE_SITES_PATH=/home/data/httpd/download.eclipse.org/buildship/updates -PECLIPSE_ORG_TEMP_PATH=/home/data/httpd/download.eclipse.org/buildship/temp -PECLIPSE_ORG_MIRROR_PATH=/buildship/updates
                --stacktrace -Declipse.p2.mirror=false
                "-Dgradle.cache.remote.url=%gradle.cache.remote.url%"
                "-Dgradle.cache.remote.username=%gradle.cache.remote.username%"
                "-Dgradle.cache.remote.password=%gradle.cache.remote.password%"
            """.trimIndent()
        }
        gradle {
            name = "Build and upload update site for Eclipse 2018-12"
            tasks = "clean build uploadUpdateSite"
            buildFile = ""
            gradleParams = """
                --exclude-task eclipseTest
                -Peclipse.version=410 -Pcompiler.location='%linux.java8.oracle.64bit%/bin/javac' -Pbuild.invoker=%build.invoker% -Prelease.type=%eclipse.release.type% -PECLIPSE_ORG_FTP_HOST=build.eclipse.org -PECLIPSE_ORG_FTP_USER=%eclipse.downloadServer.username% -PECLIPSE_ORG_FTP_PASSWORD=%eclipse.downloadServer.password% -PECLIPSE_ORG_FTP_UPDATE_SITES_PATH=/home/data/httpd/download.eclipse.org/buildship/updates -PECLIPSE_ORG_TEMP_PATH=/home/data/httpd/download.eclipse.org/buildship/temp -PECLIPSE_ORG_MIRROR_PATH=/buildship/updates
                --stacktrace -Declipse.p2.mirror=false
                "-Dgradle.cache.remote.url=%gradle.cache.remote.url%"
                "-Dgradle.cache.remote.username=%gradle.cache.remote.username%"
                "-Dgradle.cache.remote.password=%gradle.cache.remote.password%"
            """.trimIndent()
        }
        gradle {
            name = "Build and upload update site for Eclipse 2019-03"
            tasks = "clean build uploadUpdateSite"
            buildFile = ""
            gradleParams = """
                --exclude-task eclipseTest
                -Peclipse.version=411 -Pcompiler.location='%linux.java8.oracle.64bit%/bin/javac' -Pbuild.invoker=%build.invoker% -Prelease.type=%eclipse.release.type% -PECLIPSE_ORG_FTP_HOST=build.eclipse.org -PECLIPSE_ORG_FTP_USER=%eclipse.downloadServer.username% -PECLIPSE_ORG_FTP_PASSWORD=%eclipse.downloadServer.password% -PECLIPSE_ORG_FTP_UPDATE_SITES_PATH=/home/data/httpd/download.eclipse.org/buildship/updates -PECLIPSE_ORG_TEMP_PATH=/home/data/httpd/download.eclipse.org/buildship/temp -PECLIPSE_ORG_MIRROR_PATH=/buildship/updates
                --stacktrace -Declipse.p2.mirror=false
                "-Dgradle.cache.remote.url=%gradle.cache.remote.url%"
                "-Dgradle.cache.remote.username=%gradle.cache.remote.username%"
                "-Dgradle.cache.remote.password=%gradle.cache.remote.password%"
            """.trimIndent()
        }

        gradle {
            name = "Build and upload update site for Eclipse 2019-06"
            tasks = "clean build uploadUpdateSite"
            buildFile = ""
            gradleParams = """
                --exclude-task eclipseTest
                -Peclipse.version=412 -Pcompiler.location='%linux.java8.oracle.64bit%/bin/javac' -Pbuild.invoker=%build.invoker% -Prelease.type=%eclipse.release.type% -PECLIPSE_ORG_FTP_HOST=build.eclipse.org -PECLIPSE_ORG_FTP_USER=%eclipse.downloadServer.username% -PECLIPSE_ORG_FTP_PASSWORD=%eclipse.downloadServer.password% -PECLIPSE_ORG_FTP_UPDATE_SITES_PATH=/home/data/httpd/download.eclipse.org/buildship/updates -PECLIPSE_ORG_TEMP_PATH=/home/data/httpd/download.eclipse.org/buildship/temp -PECLIPSE_ORG_MIRROR_PATH=/buildship/updates
                --stacktrace -Declipse.p2.mirror=false
                "-Dgradle.cache.remote.url=%gradle.cache.remote.url%"
                "-Dgradle.cache.remote.username=%gradle.cache.remote.username%"
                "-Dgradle.cache.remote.password=%gradle.cache.remote.password%"
            """.trimIndent()
        }
        gradle {
            name = "Build and upload update site for Eclipse 2019-09"
            tasks = "clean build uploadUpdateSite"
            buildFile = ""
            gradleParams = """
                --exclude-task eclipseTest
                -Peclipse.version=413 -Pcompiler.location='%linux.java8.oracle.64bit%/bin/javac' -Pbuild.invoker=%build.invoker% -Prelease.type=%eclipse.release.type% -PECLIPSE_ORG_FTP_HOST=build.eclipse.org -PECLIPSE_ORG_FTP_USER=%eclipse.downloadServer.username% -PECLIPSE_ORG_FTP_PASSWORD=%eclipse.downloadServer.password% -PECLIPSE_ORG_FTP_UPDATE_SITES_PATH=/home/data/httpd/download.eclipse.org/buildship/updates -PECLIPSE_ORG_TEMP_PATH=/home/data/httpd/download.eclipse.org/buildship/temp -PECLIPSE_ORG_MIRROR_PATH=/buildship/updates
                --stacktrace -Declipse.p2.mirror=false
                "-Dgradle.cache.remote.url=%gradle.cache.remote.url%"
                "-Dgradle.cache.remote.username=%gradle.cache.remote.username%"
                "-Dgradle.cache.remote.password=%gradle.cache.remote.password%"
            """.trimIndent()
        }
        gradle {
            name = "Build and upload update site for Eclipse 2019-12"
            tasks = "clean build uploadUpdateSite"
            buildFile = ""
            gradleParams = """
                --exclude-task eclipseTest
                -Peclipse.version=414 -Pcompiler.location='%linux.java8.oracle.64bit%/bin/javac' -Pbuild.invoker=%build.invoker% -Prelease.type=%eclipse.release.type% -PECLIPSE_ORG_FTP_HOST=build.eclipse.org -PECLIPSE_ORG_FTP_USER=%eclipse.downloadServer.username% -PECLIPSE_ORG_FTP_PASSWORD=%eclipse.downloadServer.password% -PECLIPSE_ORG_FTP_UPDATE_SITES_PATH=/home/data/httpd/download.eclipse.org/buildship/updates -PECLIPSE_ORG_TEMP_PATH=/home/data/httpd/download.eclipse.org/buildship/temp -PECLIPSE_ORG_MIRROR_PATH=/buildship/updates
                --stacktrace -Declipse.p2.mirror=false
                "-Dgradle.cache.remote.url=%gradle.cache.remote.url%"
                "-Dgradle.cache.remote.username=%gradle.cache.remote.username%"
                "-Dgradle.cache.remote.password=%gradle.cache.remote.password%"
            """.trimIndent()
        }

        gradle {
            name = "Build and upload update site for Eclipse 2020-03"
            tasks = "clean build uploadUpdateSite"
            buildFile = ""
            gradleParams = """
                --exclude-task eclipseTest
                -Peclipse.version=413 -Pcompiler.location='%linux.java8.oracle.64bit%/bin/javac' -Pbuild.invoker=%build.invoker% -Prelease.type=%eclipse.release.type% -PECLIPSE_ORG_FTP_HOST=build.eclipse.org -PECLIPSE_ORG_FTP_USER=%eclipse.downloadServer.username% -PECLIPSE_ORG_FTP_PASSWORD=%eclipse.downloadServer.password% -PECLIPSE_ORG_FTP_UPDATE_SITES_PATH=/home/data/httpd/download.eclipse.org/buildship/updates -PECLIPSE_ORG_TEMP_PATH=/home/data/httpd/download.eclipse.org/buildship/temp -PECLIPSE_ORG_MIRROR_PATH=/buildship/updates
                --stacktrace -Declipse.p2.mirror=false
                "-Dgradle.cache.remote.url=%gradle.cache.remote.url%"
                "-Dgradle.cache.remote.username=%gradle.cache.remote.username%"
                "-Dgradle.cache.remote.password=%gradle.cache.remote.password%"
            """.trimIndent()
        }
    }

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
})
