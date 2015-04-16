# Installation instructions

## Requirements

Buildship can be used with Eclipse 3.6.x or newer. Older versions might work but have not been tested explicitly. Depending on the
version of Gradle that Buildship interacts with, certain features of Buildship may not be available.


## Installing from eclipse.org update site

We propose you install Buildship from one of the provided composite update sites listed on [eclipse.org](https://projects.eclipse.org/projects/tools.buildship/downloads):

  * `http://download.eclipse.org/buildship/updates/milestones/1.0`
  * `http://download.eclipse.org/buildship/updates/snapshots/1.0`

Each commit to the Buildship project creates a new snapshot version that is ready for download from eclipse.org. In regular intervals, the Buildship team
creates a new milestone version and makes it available on eclipse.org.

Apply the following instructions to install the latest snapshot or milestone of Buildship into Eclipse.

 1. In Eclipse, open the menu item _Help >> Install New Software_.
 1. Paste the appropriate update site link into the _Work with_ text box.
 1. Click the _Add_ button at the top of the screen, give the update site a name, and press _OK_.
 1. Ensure that the option _Group Items by Category_ is enabled.
 1. Select the top-level node _Buildship: Eclipse Plug-ins for Gradle_ once it appears.
 1. Click _Next_. This may take a while.
 1. Review the list of software that will be installed. Click _Next_ again.
 1. Review and accept the licence agreement and click _Finish_.


## Installing from builds.gradle.org update site

We propose you install Buildship from eclipse.org. If, for any reason, you still want to install
from [builds.gradle.org](https://builds.gradle.org/project.html?projectId=Tooling_Buildship&tab=projectOverview), the following snapshot update sites
are available for all the supported Eclipse versions:

  * `https://builds.gradle.org/repository/download/Tooling_Master_Commit_Eclipse45Build/.lastSuccessful/update-site` (latest 4.5 development snapshot)
  * `https://builds.gradle.org/repository/download/Tooling_Master_Commit_Eclipse44Build/.lastSuccessful/update-site` (latest 4.4 development snapshot)
  * `https://builds.gradle.org/repository/download/Tooling_Master_Commit_Eclipse43Build/.lastSuccessful/update-site` (latest 4.3 development snapshot)
  * `https://builds.gradle.org/repository/download/Tooling_Master_Commit_Eclipse42Build/.lastSuccessful/update-site` (latest 4.2 development snapshot)
  * `https://builds.gradle.org/repository/download/Tooling_Master_Commit_Eclipse37Build/.lastSuccessful/update-site` (latest 3.7 development snapshot)
  * `https://builds.gradle.org/repository/download/Tooling_Master_Commit_Eclipse36Build/.lastSuccessful/update-site` (latest 3.6 development snapshot)

Apply the following instructions to install the latest snapshot of Buildship into your version of Eclipse.

 1. In Eclipse, open the menu item _Help >> Install New Software_.
 1. Paste the update site link that matches your Eclipse version into the _Work with_ text box.
 1. Click the _Add_ button at the top of the screen, give the update site a name, and press _OK_.
 1. If prompted, set the following credentials: username=guest, password=guest.
 1. Ensure that the option _Group Items by Category_ is enabled.
 1. Select the top-level node _Buildship: Eclipse Plug-ins for Gradle_ once it appears.
 1. Click _Next_. This may take a while.
 1. Review the list of software that will be installed. Click _Next_ again.
 1. Review and accept the licence agreement and click _Finish_.


## Updating from update site

If you have already installed Buildship, you can update to the most recent version by opening the menu item _Help >> Check for Updates_. This
works for the update sites on both eclipse.org and builds.gradle.org.
