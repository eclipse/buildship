# Project import and configuration

## Fine-tune import wizard UI

### Requested Change

Based on UX feedback, improve the import wizard screens. Primarily, this means providing more context information
along each step of the wizard.

### Motivation

Using the import wizard must be as intuitive, informative, and self-explanatory as possible.


## Allow to configure Java home through dropdown

### Requested Change

Rather than displaying a text field and a file chooser to configure the Java home, allow the user to choose from
the installed runtime JREs of the current workspace through a dropdown.

### Motivation

We want to provide a deep integration into Eclipse, allowing users the means to configure a JRE the way they already know.


## Support import of Buildship into Buildship

### Requested Change

In order to allow development of Buildship with Buildship, the import logic needs to become Eclipse plugin-aware.

1. Read the JRE version from the MANIFEST file and set on the project.
1. Detect if the project uses the Groovy plugin and set the Groovy nature for those projects.

### Motivation

Buildship should support the development of Eclipse plugins written in Java.


## Support re-importing a project

### Requested Change

When importing a Gradle project that has already been imported into the workspace as a Gradle project before, override
the existing configuration rather than failing with an error that the project already exists. During the re-import, detect
if something has actually changed before applying the change.

### Motivation

It is a common scenario for users to re-import a project because its configuration has changed.


## Supported nested projects feature of Eclipse Mars

### Requested Change

For the Eclipse Mars distribution, display the Eclipse projects of a Gradle multi-project build in a hierarchy. Have an option
in the header of the Project Explorer to switch between flat view and nested view.

### Motivation

Displaying the Eclipse projects in a hierarchy closely reflects the Gradle project hierarchy of a multi-project build.


## Support partial imports

### Requested Change

It should probably be possible to import only a single Gradle project from a multi-project build.

### Motivation

It seems a very common use case for Eclipse developers to only import part of a multi-project build.


## Import buildSrc into workspace

### Requested Change

If available, import the _buildSrc_ folder into the workspace as a separate Eclipse project named _buildSrc_.

### Motivation

Build authors often make use the _buildSrc_ folder in non-trivial projects.


## Project creation wizard

### Requested Change

Provide a wizard that allows to configure the main aspects of a single-/multi-project build. Create the involved build
files, configure them according to what the user has specified, and then import the newly created project into the workspace.

### Motivation

Inexperienced Gradle users appreciate a project creation wizard that provides an easy way to get started with a new Gradle project.
