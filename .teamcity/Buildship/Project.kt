package Buildship

import jetbrains.buildServer.configs.kotlin.v2019_2.ParameterDisplay
import jetbrains.buildServer.configs.kotlin.v2019_2.Project

object Project : Project({
    description = "Eclipse plugins for Gradle http://eclipse.org/buildship"

    vcsRoot(GitHubVcsRoot)

    template(EclipseBuildTemplate)

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

    subProject(Buildship.Promotion30.Project)
    subProject(Buildship.Check30.Project)
})