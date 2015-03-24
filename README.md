# Buildship: Eclipse Plug-ins for Gradle

## Requirements

Buildship can be used with Eclipse 3.6.x or newer. Older versions might work but have not been tested explicitly. Depending on the version of Gradle that
Buildship interacts with, certain features of Buildship may not be available.


## Documentation

Documentation is available on [GitHub](https://github.com/gradleware/buildship).


## Usage Setup instructions

This section describes the steps to install the latest snapshot version of Buildship into Eclipse.

### Installing from snapshot update site

Each commit to the master repository creates a new snapshot version of Buildship on
our [Continuous Integration Server](https://builds.gradle.org/project.html?projectId=Tooling_Master_Eclipse&tab=projectOverview).

The following snapshot update sites are currently available for all the supported Eclipse versions:
  * `https://builds.gradle.org/repository/download/Tooling_Master_Commit_Eclipse45Build/.lastSuccessful/update-site` (latest 4.5 development snapshot)
  * `https://builds.gradle.org/repository/download/Tooling_Master_Commit_Eclipse44Build/.lastSuccessful/update-site` (latest 4.4 development snapshot)
  * `https://builds.gradle.org/repository/download/Tooling_Master_Commit_Eclipse43Build/.lastSuccessful/update-site` (latest 4.3 development snapshot)
  * `https://builds.gradle.org/repository/download/Tooling_Master_Commit_Eclipse42Build/.lastSuccessful/update-site` (latest 4.2 development snapshot)
  * `https://builds.gradle.org/repository/download/Tooling_Master_Commit_Eclipse37Build/.lastSuccessful/update-site` (latest 3.7 development snapshot)
  * `https://builds.gradle.org/repository/download/Tooling_Master_Commit_Eclipse36Build/.lastSuccessful/update-site` (latest 3.6 development snapshot)

Apply the following instructions to use one of the Eclipse update sites previously listed:

 1. In Eclipse, open the menu item _Help >> Install New Software_.
 1. Paste the update site link that matches your Eclipse version into the _Work with_ text box.
 1. Click the _Add_ button at the top of the screen, give the update site a name, and press _OK_.
 1. Ensure that the option _Group Items by Category_ is enabled.
 1. Select the top-level node _Buildship: Eclipse Plug-ins for Gradle_ once it appears. This may take a moment.
 1. Click _Next_.  This may take a while.
 1. Review the list of software that will be installed. Click _Next_ again.
 1. Review and accept the licence agreement and click _Finish_.

If you have already installed the plugin previously, you can update to the most recent version by opening the menu item _Help >> Check for Updates_.


## Development Setup Instructions

This section describes the steps to setup Eclipse such that it can be used for development of Buildship.

### Setting up Eclipse

We use Eclipse

 - as our development environment of Buildship and
 - as our target platform against which we compile and run Buildship.

We use our internally-packaged Eclipse distribution, but the latest Eclipse for RCP Developers package should be fine, too.

Proceed as following to get all the required software installed:

 1. Download the Eclipse distribution matching your OS from the http://dev1.gradle.org:8000/eclipse/distro/ site.
 1. Untar the downloaded distribution and start Eclipse.
 1. Install the latest version of Buildship from the e44 update site listed above.

### Getting the source code

The project and its source code is hosted on GitHub: `https://github.com/gradleware/buildship`.

Apply the following command to get a clone of the source code:

    git clone git@github.com:gradleware/buildship.git

#### Committers

Navigate into the created _buildship_ directory and set the git username and email address:

    git config user.name "johnsmith"
    git config user.email "john.smith@gradleware.com"

And make sure to properly map the part before the domain of your email address in TeamCity under _My Settings & Tools_ >>
_General_ >> _Version Control Username Settings_ >> _Default for all of the Git roots_.

    john.smith

From now on, when you commit to the _buildship_ project through Git, the change will be properly associated with your user in
TeamCity. You can verify that the setup is correct by seeing your full name next to each commit.

In order to avoid extra commits in the Git history after merging local changes with remote changes, apply the
_rebase_ strategy when pulling in the remote changes. By applying the _update_ alias listed below, you can conveniently
pull in the remote changes from master without ending up with ‘merge branch’ commits in the Git history later on.

    git config --global alias.update=“pull --rebase origin master”
    git update

### Importing the source code into Eclipse

The source code consists of a single root project and several sub-projects nested within it. You can use the
generic Eclipse 'Import Existing Projects' wizard. Select the root folder _buildship_ and
check the option 'Search for nested projects'. Select all _com.gradleware.*_ projects. You
can then press _Finish_.

### Setting the target platform

After importing all the projects into Eclipse, there will be some build errors due to code references to missing
plugins. To add these missing plugins to Eclipse, open the _tooling-e44.target_ file (or the one matching your
Eclipse version) located in the project root folder and click _Set as Target Platform_ in the top-right corner. This
will fix all compilation issues. Note, that it might take a while as it will download a whole SDK for the specified
version of Eclipse.

### Running the tests inside of Eclipse

To run the complete set of core tests from inside Eclipse, right-click
on the package _com.gradleware.tooling.eclipse.core.test_ and choose _Run As >> JUnit Plug-In-Test_
(not as a _JUnit Test_!). Individual tests can be run the same way.

To run the complete set of ui tests from inside Eclipse, right-click
on the package _com.gradleware.tooling.eclipse.ui.test_ and choose _Run As >> JUnit Plug-In-Test_
(not as a _JUnit Test_!). Individual tests can be run the same way.

### Running the Build

To run the full build, execute

    ./gradlew build

The final P2 repository will be created in the `com.gradleware.tooling.eclipse.site/build/repository` directory. If
the target platform had not been downloaded previously, it will appear in the _~/.tooling/eclipse/targetPlatforms_ folder.

To run the build without running the tests, exclude the `eclipseTest` task:

    ./gradlew build -x eclipseTest

To have full build ids in the name of the generated jars and in the manifest files, set the `build.invoker` property to _ci_:

    ./gradlew build -Pbuild.invoker=ci

The default Eclipse version to use as the target platform is defined in the root project's _build.gradle_ file. To override the
Eclipse version to use as the target platform, set the `eclipse.version` accordingly:

    ./gradlew build -Peclipse.version=44

The possible values for `eclipse.version` are: 36, 37, 42, 43, 44, and 45.

### Adding a new Eclipse plugin

* Create a new folder under the _buildship_ root folder
* Create a project in that folder with Eclipse and use the same name for the project as for the folder
* Create a _build.gradle_ file and apply the `BundlePlugin`
* Add the project to the _settings.gradle_ file

### References

* [Eclipse Testing](http://wiki.eclipse.org/Eclipse/Testing)
* [PDE Test Automation](http://www.eclipse.org/articles/article.php?file=Article-PDEJUnitAntAutomation/index.html)

## Continuous Integration

The automated builds for each supported Eclipse version can be found on TeamCity:
`https://builds.gradle.org/project.html?projectId=Tooling_Buildship&tab=projectOverview`

It is possible to reference the last successful run of a given TeamCity configuration like this:
`https://builds.gradle.org/repository/download/Tooling_Master_Commit_Eclipse44Build/.lastSuccessful/update-site`
