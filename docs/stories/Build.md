# Eclipse Plugin Build

## Include extension elements in uptodate checks

### Requested Change

Extend the uptodate checks of the tasks provided by the `eclipsebuild` package to also respect changes to
the extension element that they makes use of. Use convention mappings to solve this problem.

### Motivation

All tasks must properly participate in the uptodate checks.


## Make use of NamedDomainObjectContainer for target platform definitions

### Requested Change

Make use of the `NamedDomainObjectContainer` approach to define the target platforms in the root project.

### Motivation

The current DSL to define the target platforms in the `BuildDefinitionPlugin` in the root Gradle build file
is not the typical way of doing it in Gradle.


## Only declare plugin dependencies in the MANIFEST.MF file

### Requested Change

Only declare the Eclipse plugin dependencies in the _MANIFEST.MF_ file and have the _build.gradle_ file read
the dependency information described in the _MANIFEST.MF_ file.

### Motivation

Avoid the redundancy between the declaration of Eclipse plugin dependencies in _build.gradle_ and in _MANIFEST.MF_.


## Only declare target definitions in the .target files

### Requested Change

Only declare the target definitions in the .target files and have the build file of the Gradle root project read
the target platform information described in the .target definition files.

### Motivation

Avoid the redundancy between the declaration of target platforms in the .target definition files and the build
file of the Gradle root project.


## Upload daily snapshots to eclipse.org

### Requested Change

Upload each daily snapshot of Buildship to the eclipse.org downloads area via SFTP.

### Motivation

We want to offer the latest snapshot of Buildship to our users in an automated manner.


## Restrict Eclipse plugin dependency lookups to the mavenized repository

### Requested Change

Currently, all Eclipse plugin dependencies are declared with wildcard versions. Thus, each repository registered in
the build is queried to find the latest version for a given Eclipse plugin dependency. Avoid this broad lookup of
the Eclipse plugin dependencies:

 * Use fixed dependency versions (provide a map with dependency versions per target platform).
 * Limit the lookup to the mavenized repository for all groups named _eclipse_.
 * Provide an even deeper integration into Gradle dependency management.

### Motivation

The current behavior adds significantly to the build time and it increases the need to be online when building.
