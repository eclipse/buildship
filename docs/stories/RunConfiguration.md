# Run Configurations

## ~~Have explicit context menu item label for creating new run configuration~~

### Requested Change

We should have different context menu item labels for creating a new run configuration versus
editing an existing run configuration.

### Motivation

The current behavior can be confusing since it is not clear to the user when a new run configuration will
be created and when an existing run configuration is edited.


## ~~Change color of run configuration arguments shown in the Gradle Console~~

### Requested Change

The run configuration arguments shown at the top of the Gradle Console must be displayed in a different color.

### Motivation

The run configuration arguments must be displayed in a different color for easy recognition and for better distinction
from the output of the actual Gradle build execution.


## Support colored output in Gradle Console Views

### Requested Change

For each `LongRunningOperation` enable _colorOutput_ and in the Run Console, handle the colors and other special characters
that are written to the output streams.

### Motivation

Having colors and in-place updates of characters in the Run Console will gives us a similarly powerful output as on the command line.


## ~~Provide run configuration option to switch to the Console when running a build~~

### Requested Change

Add an option to the run configuration dialog to select through a check box whether to jump to the Console View when the build is run.

### Motivation

The user must be able to configure whether when running the build through a given run configuration, the Console View that shows the build
output should should automatically become active or not.


## Run configuration in debug mode

### Requested Change

Allow to run a Gradle build in debug mode by Buildship setting the _--debug-jvm_ argument when executing the build. Once
the target JVM is started, Buildship should automatically start a remote debugging session from within Eclipse on behalf
of the user.

### Motivation

Debugging tests (and other Java executions) through Gradle is part of the deep integration into Eclipse.
