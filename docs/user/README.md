# Current Functionality - A brief overview

This document describes briefly the functionality currently available in Buildship.


## Project Import

You can import an existing Gradle project through the Eclipse Import Wizard functionality. In distinct
steps, the import wizard allows you to specify the location of the Gradle project, configure the various
optional, advanced settings, and see a summary of how the project will be imported.

During the import, the Eclipse projects are created and configured as Java projects with the proper
classpath and project dependencies. You can stop the import at any time by pressing the Stop button
in the Progress View.


## Task View

In the Gradle Task View, you can see the tasks of all the imported Gradle projects. The tasks can be sorted, filtered,
and executed. The selection in Eclipse can be key linked to the selection of the task view, both ways. The content of
the task view can be refreshed, meaning the latest version of the Gradle build files are loaded. You can stop the refresh
at any time by pressing the Stop button in the Progress View. You can navigate from a project to its build file through
the context menu.


## Task Execution

You can execute Gradle tasks from the Task View through double-clicking, right-clicking, or via keyboard. In case multiple
tasks are selected, they are passed to Gradle in the order they were selected. Each time a Gradle build is executed with a
different set of tasks, a new Gradle run configuration is created. You can edit existing run configurations and create new
run configurations through the Run Configurations dialog. All settings can be configured in the run configuration dialog.

Whenever a Gradle build is executed, a new Gradle console is opened that contains the output from the build. You can cancel
the execution of the build by pressing the Stop button in the Gradle console. The Gradle consoles can be closed individually
or all at once.


## Test Execution Progress

When running a build that executes tests, the progress of the test execution is shown in the Test Runner View. The tests
can be shown flat or hierarchical.
