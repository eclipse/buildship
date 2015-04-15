# Buildship: Eclipse Plug-ins for Gradle

## Requirements

Buildship can be used with Eclipse 3.6.x or newer. Older versions might work but have not been tested explicitly. Depending on the version of Gradle that
Buildship interacts with, certain features of Buildship may not be available.


## Documentation

Documentation is available on [GitHub](https://github.com/eclipse/buildship).


## Usage Setup instructions

### Installing from eclipse.org downloads section

This section describes the steps to install a recent snapshot version of Buildship into Eclipse.

In regular intervals, new snapshot versions of Buildship for Eclipse Mars are
posted on [eclipse.org](https://projects.eclipse.org/projects/tools.buildship/downloads) as a zipped Eclipse update site.

Apply the following instructions to use one of the zipped Eclipse update sites:

 1. Download and extract the .zip file to your local system.
 1. In Eclipse, open the menu item _Help >> Install New Software_.
 1. Click the _Add..._ button to add a new repository.
 1. Click the _Local..._ button, point to the root folder of the extracted .zip file, and press _OK_.
 1. Ensure that the option _Group Items by Category_ is enabled.
 1. Select the top-level node _Buildship: Eclipse Plug-ins for Gradle_ once it appears.
 1. Click _Next_. This may take a while.
 1. Review the list of software that will be installed. Click _Next_ again.
 1. Review and accept the licence agreement and click _Finish_.

### Installing from CI update site

This section describes the steps to install the very latest snapshot version of Buildship into Eclipse.

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
 1. If prompted, set the following credentials: username=guest, password=guest.
 1. Ensure that the option _Group Items by Category_ is enabled.
 1. Select the top-level node _Buildship: Eclipse Plug-ins for Gradle_ once it appears. This may take a moment.
 1. Click _Next_. This may take a while.
 1. Review the list of software that will be installed. Click _Next_ again.
 1. Review and accept the licence agreement and click _Finish_.

If you have already installed the plugin previously, you can update to the most recent version by opening the menu item _Help >> Check for Updates_.


## Development Setup Instructions

This section describes the steps to set up Eclipse such that it can be used for development of Buildship.

### Setting up Eclipse

We use Eclipse

 - as our development environment of Buildship and
 - as our target platform against which we compile and run Buildship.

To provide a consistent working environment for the development we use [Oomph](https://projects.eclipse.org/projects/tools.oomph). Oomph is a model-based tool to install custom Eclipse distributions. The model instructs Oomph to set up the environment such that

 - the JDK is aligned to the project,
 - the target platform is set,
 - the Buildship git repository is cloned and imported into the workspace, and
 - the code formatter settings are customized.

To get started apply the following instructions:

#### First installation

##### 1. Download the setup model and the latest Oomph installer

The setup model is available at the [project's git repository](https://raw.githubusercontent.com/eclipse/buildship/master/buildship.setup). The latest version of the installer is available at the [project's wiki page](https://wiki.eclipse.org/Eclipse_Oomph_Installer).
Download and extract the one matching to your local environment. The first screen of Oomph should look like this. 

![Oomph initial screen](/docs/development/oomph/install-1.png)

##### 2. Add the setup model

On the first screen choose _Eclipse Platform_ to install the minimum set of plugins for the IDE. Check if the _VM_ and the _Bundle pool_ to have proper values and click _Next_. Then, on the second page click on the plus sign on the top and specify the downloaded setup model. If it was successful, double-click on the _Buildship_ node. If everything is OK you should see the following screen. Click _Next_.

![Buildship setup model added](/docs/development/oomph/install-2.png)

##### 3. Specify installer properties

On the properties screen you have to specify the environment-specific variables: 
 
 - the location of the custom Eclipse distribution,
 - the location of workspace and the git clone, 
 - the JDK, and
 - the target platform against the project is compiled.
 
When done it should look something like this.

![Variables page](/docs/development/oomph/install-3.png)

Click _Next_ and _Finish_ to start the installation. *Note:* If you try to run the installer multiple times Oomph might remember some of the preferences. To make all variables visible, enable the _Show all variables_ checkbox.

##### 4. Start the IDE

When the installer finishes the IDE starts up automatically. There are a few setup tasks running on startup (importing projects, setting up preferences). Wait for these to finish before doing anything! When finished you should see the following workspace.

![Initial workspace](/docs/development/oomph/install-4.png)

#### Change the target platform 

The target platform selected during the installation can be changed, even if it's a bit hidden. In the menu click _Help_ > _Perform Setup tasks_ then click **_Back_**. Select the _Show all variables_ checkbox and change the _Buildship Target Platform_ to the desired version. Click _Next_ and _Finish_ then wait for the target platform to download. When finished the source code will be compiled against the new target platform.

![Specify new target platform](/docs/development/oomph/install-5.png)

#### Future plans

Right now one has to download the setup model manually. We plan to move the model into Oomph's central product catalog. From then on every time the model is changed the users of the model will also receive the updates. 

### Running the tests inside of Eclipse

To run the complete set of core tests from inside Eclipse, right-click
on the package _org.eclipse.buildship.core.test_ and choose _Run As >> JUnit Plug-In-Test_
(not as a _JUnit Test_!). Individual tests can be run the same way.

To run the complete set of ui tests from inside Eclipse, right-click
on the package _org.eclipse.buildship.ui.test_ and choose _Run As >> JUnit Plug-In-Test_
(not as a _JUnit Test_!). Individual tests can be run the same way.

### Running the Build

To run the full build, execute

    ./gradlew build

The final P2 repository will be created in the `org.eclipse.buildship.site/build/repository` directory. If
the target platform had not been downloaded previously, it will appear in the _~/.tooling/eclipse/targetPlatforms_ folder.

To run the build without running the tests, exclude the `eclipseTest` task:

    ./gradlew build -x eclipseTest

To have full build ids in the name of the generated jars and in the manifest files, set the `build.invoker` property to _ci_:

    ./gradlew build -Pbuild.invoker=ci

The available target platforms are defined in the root project's _build.gradle_ file, under the _eclipseBuild_ node.
By default, the build runs against target platform version _45_. To build against a different target platform version,
you can set the `eclipse.version` Gradle project property:

    ./gradlew build -Peclipse.version=44

### Continuous Integration

Buildship is continuously built on our [Continuous Integration Server](https://builds.gradle.org/project.html?projectId=Tooling_Buildship&tab=projectOverview).

### References

* [Eclipse Testing](http://wiki.eclipse.org/Eclipse/Testing)
* [PDE Test Automation](http://www.eclipse.org/articles/article.php?file=Article-PDEJUnitAntAutomation/index.html)

