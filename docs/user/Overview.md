# Current Functionality - A brief overview

This document describes briefly the functionality currently available in Buildship.


## Project Import

You can import an existing Gradle project through the Eclipse Import Wizard functionality. In distinct
steps, the import wizard allows you to specify the location of the Gradle project, configure the various
optional, advanced settings, and see a summary of how the project will be imported. The summary page will
also warn if you are using a target Gradle version for which Buildship is not able to offer all its functionality.

During the import, the Eclipse projects are created and added to the current workspace. In case of Java projects,
the source paths and the classpath container which contains the external dependencies and the project dependencies are
configured. The content of the classpath container is refreshed each time the project is opened.

You can stop the import at any time by pressing the Stop button in the Progress View.


## Project Creation

You can create a new Gradle project and add it the current workspace with the New Gradle Project wizard. The wizard allows you to specify the project name and location, the working sets, and the target Gradle distribution. To create the project, the wizard executes Gradle's built-in `init` task with the [java-library](https://docs.gradle.org/current/userguide/build_init_plugin.html#sec:build_init_tasks) template.  


## Task View

In the Gradle Task View, you can see the tasks of all the imported Gradle projects. The tasks can be sorted, filtered,
and executed. The selection in Eclipse can be linked to the selection of the task view, both ways. The content of
the task view can be refreshed, meaning the latest versions of the Gradle build files are loaded. You can navigate from
a project to its build file through the context menu.


## Task Execution

You can run Gradle tasks from the Task View through double-clicking, right-clicking, or via keyboard. In case multiple
tasks are selected, they are passed to Gradle in the order they were selected. Each time a Gradle build is executed with a
different set of tasks, a new Gradle run configuration is created. You can edit existing run configurations and create new
run configurations through the Run Configurations dialog. All settings can be configured in the run configurations dialog.

Whenever a Gradle build is executed, a new Gradle console is opened that contains the output from the build. You can cancel
the execution of the build by pressing the Stop button in the Gradle console. The Gradle consoles can be closed individually
or all at once.

## Executions View

Whenever a Gradle build is executed, a new Execution page is opened in the Executions View that displays the progress of running the build. You can see the different life-cycle phases of the build, the tasks and tests being run, and the success of each operation.

You can switch between the execution pages and you can jump to the corresponding Gradle console. You can also rerun a finished build. You can cancel the execution of the build by pressing the Stop button in the execution page. The execution pages can be closed individually or all at once.

This is available if the Gradle build is run with target Gradle version 2.5 or newer.


## Test execution

You can run tests from the Execution page through right-clicking on a node that represents a test class or test method. A new Gradle build is executed and the selected tests are run by Gradle. If the test task to which the tests belongs has setup and cleanup tasks configured, those are run accordingly. Once the build has finished and there are test failures, you can rerun all failed tests by pressing the Rerun Failed Tests button in the execution page.

This is available if the Gradle build is run with target Gradle version 2.6 or newer.

## Basic Web project support

If you import a web project and your Eclipse contains the Web Tools Platform plugin then Buildship will automatically generate the WTP configuration files. The generated files will respect all customizations that are defined in the eclipse-wtp Gradle plugin.

## Build scans

You can enable [build scans](https://scans.gradle.com/) from the preferences for the entire workspace as well as per project. If the preference is enabled and the project applies the `com.gradle.build-scan` plugin then a build scan is published for each build. You can open the published build scans by clicking on the URL in the console view or by clicking on the 'Open build scan' button on the Execution view's toolbar.

## Hierarchical preferences

There are three hierarchical levels of configuration in Buildship. You can define default workspace settings for the project import, the build scans capturing, and for the offline mode. You can override those workspace settings in the project configuration settings. Finally, you can override the project configuration settings on any run configuration.

## Cancellation

You can cancel all long-running operations like importing a project, executing tasks, refreshing the tasks, etc.

This is available if the Gradle build is run with target Gradle version 2.1 or newer.

## Offline mode

Just like the build scans, you can enable the offline mode from the workspace or from the project preferences. If enabled, the project synchronization and the build execution won't try to connect to the internet so that you can continue working without continuous connectivity. 

## Composite build support

Gradle 3.1 introduced the concept of [composite builds](https://docs.gradle.org/3.1/release-notes#composite-builds). To quote the release notes,
_"they allow you to integrate independent Gradle builds, effectively giving you something like a multi-project build while keeping the builds
separate"_. For more details, check out the [Gradle User Guide](https://docs.gradle.org/current/userguide/composite_builds.html).

Staring from version 2.0, Buildship can seamlessly import composite builds into Eclipse.

Imported composite builds have a few special traits:
- All included builds are synchronized together.
- Binary dependencies between included builds are replaced with Eclipse project dependencies. Also, the included build dependency substitution
rules declared in the `settings.gradle` file are respected.

Current limitations:
- Executing tasks in included builds is currently not supported. It will be enabled once the task addressing will be implemented in Gradle.
- WTP support is not working with included builds.
- The Executions view will not show events from included builds.
