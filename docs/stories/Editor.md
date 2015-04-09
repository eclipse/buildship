# General

## Task Glossary Popup

### Requested Change

Provide a dialog that allows to search for a given task type and display information about the properties
and methods of a given task.

### Motivation

Most Gradle build file authors do not know the properties and methods available on each task by heart.


## Syntax highlighting

### Requested Change

Support syntax highlighting in Gradle build files. Implement a parser for the Gradle DSL that creates
an AST that can be consumed by the Eclipse syntax highlighting engine.

### Motivation

A build author expects syntax highlighting for Gradle build files to facilitate reading maintaining build files.


## Code completion

### Requested Change

Support code completion in Gradle build files for project properties and methods, tasks properties and methods,
dependency configuration, etc. Implement a parser for the Gradle DSL that creates an AST that can be consumed by
the Eclipse code completion engine.

### Motivation

A build author expects code completion for Gradle build files to facilitate writing and maintaining build files.


## Build script debugging

### Requested Change

Allow the Eclipse user to set break points in the Gradle build scripts such that when a Gradle build is run
in debug mode, the build stops when one of the set breakpoints is reached.

This story is about debugging the Gradle builds scripts themselves, and is not related to debugging the
execution of tests.

### Motivation

Debugging Gradle builds is currently very cumbersome and for the common user, it usually means resorting to
printing out variables to the console.
