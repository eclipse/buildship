package Buildship

import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

object GitHubVcsRoot : GitVcsRoot({
    name = "Buildship"
    url = "https://gradlewaregitbot@github.com/eclipse/buildship.git"
    branchSpec = "+:refs/heads/*"
    userForTags = "Gradleware GitHubVcsRoot Bot <gradlewa/**/regitbot@gradleware.com>"
    agentGitPath = "%env.TEAMCITY_GIT_PATH%"
    agentCleanFilesPolicy = GitVcsRoot.AgentCleanFilesPolicy.NON_IGNORED_ONLY
    useMirrors = false
    authMethod = anonymous()
})
