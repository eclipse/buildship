# Installation instructions

## Requirements

Buildship can be used with Eclipse 3.6.x or newer. Older versions might work but have not been tested explicitly. Depending on the
version of Gradle that Buildship interacts with, certain features of Buildship may not be available.


## Installing from eclipse.org update site

We propose you install Buildship from the [Eclipse Marketplace](http://marketplace.eclipse.org/content/buildship-gradle-integration).

Buildship is also available through one of the provided composite update sites listed on [eclipse.org](https://projects.eclipse.org/projects/tools.buildship/downloads).

For manual installation the complete list of update sites:

Eclipse Version | Type      | Update Site
--------------  | ----------| ------------
Neon (4.6)      | release   | `http://download.eclipse.org/buildship/updates/e46/releases/1.0`
                | milestone | `http://download.eclipse.org/buildship/updates/e46/milestones/1.0`
                | snapshot  | `http://download.eclipse.org/buildship/updates/e46/snapshots/1.0`
Mars (4.5)      | release   | `http://download.eclipse.org/buildship/updates/e45/releases/1.0`
                | milestone | `http://download.eclipse.org/buildship/updates/e45/milestones/1.0`
                | snapshot  | `http://download.eclipse.org/buildship/updates/e45/snapshots/1.0`
Luna (4.4)      | release   | `http://download.eclipse.org/buildship/updates/e44/releases/1.0`
                | milestone | `http://download.eclipse.org/buildship/updates/e44/milestones/1.0`
                | snapshot  | `http://download.eclipse.org/buildship/updates/e44/snapshots/1.0`
Kepler (4.3)    | release   | `http://download.eclipse.org/buildship/updates/e43/releases/1.0`
                | milestone | `http://download.eclipse.org/buildship/updates/e43/milestones/1.0`
                | snapshot  | `http://download.eclipse.org/buildship/updates/e43/snapshots/1.0`
Juno (4.2)      | release   | `http://download.eclipse.org/buildship/updates/e42/releases/1.0`
                | milestone | `http://download.eclipse.org/buildship/updates/e42/milestones/1.0`
                | snapshot  | `http://download.eclipse.org/buildship/updates/e42/snapshots/1.0`
Indigo (3.7)    | release   | `http://download.eclipse.org/buildship/updates/e37/releases/1.0`
                | milestone | `http://download.eclipse.org/buildship/updates/e37/milestones/1.0`
                | snapshot  | `http://download.eclipse.org/buildship/updates/e37/snapshots/1.0`
Helios (3.6)    | release   | `http://download.eclipse.org/buildship/updates/e36/releases/1.0`
                | milestone | `http://download.eclipse.org/buildship/updates/e36/milestones/1.0`
                | snapshot  | `http://download.eclipse.org/buildship/updates/e36/snapshots/1.0`

The continuous integration server generates nightly snapshot releases each day 23:00 CET which instantly become
available at the snapshot update sites above. In regular intervals, the Buildship team also creates new
milestone releases and makes them available at the milestone update sites.

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

  * `https://builds.gradle.org/repository/download/Tooling_Master_IntegrationTests_Linux_Eclipse46Build/.lastSuccessful/update-site` (latest 4.6 development snapshot)
  * `https://builds.gradle.org/repository/download/Tooling_Master_IntegrationTests_Linux_Eclipse45Build/.lastSuccessful/update-site` (latest 4.5 development snapshot)
  * `https://builds.gradle.org/repository/download/Tooling_Master_IntegrationTests_Linux_Eclipse44Build/.lastSuccessful/update-site` (latest 4.4 development snapshot)
  * `https://builds.gradle.org/repository/download/Tooling_Master_IntegrationTests_Linux_Eclipse43Build/.lastSuccessful/update-site` (latest 4.3 development snapshot)
  * `https://builds.gradle.org/repository/download/Tooling_Master_IntegrationTests_Linux_Eclipse42Build/.lastSuccessful/update-site` (latest 4.2 development snapshot)
  * `https://builds.gradle.org/repository/download/Tooling_Master_IntegrationTests_Linux_Eclipse37Build/.lastSuccessful/update-site` (latest 3.7 development snapshot)
  * `https://builds.gradle.org/repository/download/Tooling_Master_IntegrationTests_Linux_Eclipse36Build/.lastSuccessful/update-site` (latest 3.6 development snapshot)

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

If you have already installed Buildship, you can update to the most recent version by opening the menu item _Help >> Check for Updates_. Note, that the update works only if Buildship was installed from the updates sites from download.eclipse.org or from builds.gradle.org, as listed above. If Buildship comes preinstalled in your Eclipse (for instance if you use the standard [Eclipse for Java developers](https://www.eclipse.org/downloads/packages/eclipse-ide-java-developers/neon) package) then you have to do the update manually. To do that just follow the steps from the previous section.
