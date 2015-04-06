# Run Configurations

## Have explicit context menu item label for creating new run configuration

### Requested Change

We should have different context menu item labels for creating a new run configuration versus
editing an existing run configuration.

### Motivation

The current behavior can be confusing since it is not clear to the user when a new run configuration will
be created and when an existing run configuration is edited.


## Change color of run configuration arguments shown in the Gradle Console

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


## Add buttons for showing the console when the output changes

### Requested Change

For the Gradle Background Console, provide buttons to display the Run Console when something is written to its output streams.

 * _Show Console When Standard Out Changes_
 * _Show Console When Standard Err Changes_

### Motivation

This will provide the same functionality as offered by other Eclipse Consoles that the user is already familiar with.
