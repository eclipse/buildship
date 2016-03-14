#!/bin/sh
#export GIT_SSH="ssh -i ~/.ssh/id_rsa_gradlewaregitbot_buildship"
git commit -a -m"Increment version from $1 to $2"
git push origin master
