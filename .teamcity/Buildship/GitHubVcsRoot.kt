package Buildship

import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

object GitHubVcsRoot : GitVcsRoot({
    name = "Buildship"
    url = "https://github.com/eclipse/buildship.git"
    branchSpec = "+:refs/heads/*"
    agentGitPath = "%env.TEAMCITY_GIT_PATH%"
    agentCleanFilesPolicy = AgentCleanFilesPolicy.NON_IGNORED_ONLY
    useMirrors = false
    authMethod = anonymous()
})
