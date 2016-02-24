#!/bin/sh
export GIT_SSH_COMMAND="ssh -i ssh -i ~/.ssh/id_rsa_gradlewaregitbot_buildship"
git tag -a REL_$1 -m"Create tag REL_$1"
git push origin REL_$1
