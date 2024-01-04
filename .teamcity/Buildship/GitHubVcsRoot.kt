package Buildship

import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

object GitHubVcsRoot : GitVcsRoot({
    name = "Buildship"
    url = "https://github.com/eclipse/buildship.git"
    agentGitPath = "%env.TEAMCITY_GIT_PATH%"
    agentCleanFilesPolicy = AgentCleanFilesPolicy.NON_IGNORED_ONLY
    checkoutPolicy = AgentCheckoutPolicy.AUTO
    authMethod = anonymous()
})

object GitHubForkVcsRoot : GitVcsRoot({
    name = "BuildshipFork"
    url = "https://github.com/gradle/buildship.git"
    agentGitPath = "%env.TEAMCITY_GIT_PATH%"
    agentCleanFilesPolicy = AgentCleanFilesPolicy.NON_IGNORED_ONLY
    checkoutPolicy = AgentCheckoutPolicy.AUTO
    authMethod = anonymous()
    branch = "refs/heads/master"
    branchSpec = "+:*"
})

object GitHubBuildshipForkSettingsVcsRoot : GitVcsRoot({
    name = "BuildshipSettings"
    url = "https://github.com/gradle/buildship.git"
    agentGitPath = "%env.TEAMCITY_GIT_PATH%"
    agentCleanFilesPolicy = AgentCleanFilesPolicy.NON_IGNORED_ONLY
    checkoutPolicy = AgentCheckoutPolicy.AUTO
    authMethod = anonymous()
    branch = "refs/heads/teamcity-config"
})
