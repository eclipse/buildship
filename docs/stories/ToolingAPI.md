
# Tooling API and Tooling Client

## Set additional builders and natures on the projects

### Motivation
The `ide-integraion.md` document in Gradle core specifies a story ["Expose more Eclipse settings for projects"](https://github.com/gradle/gradle/blob/master/design-docs/ide-integration.md#story---expose-natures-and-builders-for-projects). The goal is to make use of the enhancements from there in Buildship.

### Estimate

- 1 day

### Implementation
- Wait for the corresponding Tooling API story to be finished and upgrade tooling-commons to use the latest TAPI version
- Add `Optional<List<String>> OmniEclipseVersion.getProjectNatures()` and propagate the result returned by the TAPI
    - It doesn't return the Gradle nature unless the the build configuration explicitly defines it
- Add `Optional<List<String>> OmniEclipseVersion.getBuildCommands()` and propagate the result returned by the TAPI
- Upgrade tooling-commons used in Buildship
- Merge the builders/natures list with the existing ones upon project synchronization
    - If the builders-natures can be retrieved
        - If a project descriptor exists, then preserve the existing builders and natures
        - Always add the Gradle nature, if not exist
        - Add the natures/builders from the model, if not exist
    - If the builders/natures can't be retrieved
        - Use existing assumption (Java nature is needed when there is at east one source folder available)

### Test cases
- Setting builders and natures on new project
    - no builders/natures specified in the model
    - some builders/natures specified in the model
- Setting builders and natures on existing project
    - imported project does not define builders/natures
    - imported project defines builders/natures
        - contains the Gradle nature
        - doesn't contain the Gradle nature
        - doesn't overlap with the ones from the model
        - overlaps with the model
- Verify sensible defaults
    -  if the project defines a source folder, a java nature should be on the default list


## Set source level for Java projects

### Motivation

The `ide-integraion.md` document in Gradle core specifies a story ["expose Java source level for Java projects to Eclipse"](https://github.com/gradle/gradle/blob/master/design-docs/ide-integration.md#story---expose-java-source-level-for-java-projects-to-eclipse). The goal is to utilize this TAPI enhancement in Buildship.

### Estimate

- 2 days

### Implementation
- add new features to tooling-commons
    - Define the `OmniJavaLanguageLevel`, `OmniJavaSourceSettings` interfaces with the same methods as their TAPI-counterparts.
    - Define the `Maybe<OmniJavaSourceSettings> getJavaSourceSettings()` method on the `OmniEclipseProject` interface
        - Returns absent value for older Gradle versions
        - Returns null if the project is not a Java project
        - Returns valid values otherwise
- make use of the new features in Buildship
    - Configure the project as Java project based on the existence of the source settings
        - For older Gradle versions use the existing assumption (e.g. if there is a source folder then the project is a Java project)
        - If the source settings is not null, then configure the Java project
    - Upon project refresh ensure that the workspace project source compatibility is in sync with the model
        - Read the source compatibility level and update the project settings  in `WorkspaceGradleOperations#synchronizeGradleProjectWithWorkspaceProject()` method
            - Convert the representation to Eclipse constants
            - If the source level is not available in the Distribution (Java 8 for Eclipse Helios): define the highest available and log the the problem

### Test cases
- Older Gradle version is used
    - project has/doesn't have source folders
- null is returned for the source settings (non-Java project)
- Model defines different source compatibility, than the project
- Model defines invalid source compatibility level (Java 8 for Eclipse Helios)
    - Log it as a warning and set the highest source compatibility level available in the tooling.


## Configure the target JDK for Java projects

### Motivation

The `ide-integraion.md` document in Gradle core specifies a story ["expose target JDK for Java projects to Eclipse"](https://github.com/gradle/gradle/blob/master/design-docs/ide-integration.md#story---expose-target-jdk-for-java-projects-to-eclipse-3d). The goal is to make use of the enhancements from there in Buildship.

### Estimate

- 2 days

### Implementation
- Wait for the corresponding Tooling API story to be finished and upgrade Buildship to the latest TAPI/Tooling-commons version
- Add method to the tooling-commons API to load the information about the target JVM 
- Upgrade tooling-commons used in Buildship
- Synchronize model fields with the related Eclipse workspace project upon refresh
     - update used JDK, if mismatch

### Test cases
- Update JDK defined by the Tooling API
- No JDK is available from the Tooling API
    - Use the default from Eclipse

## Make use of the JavaProject model

### Motivation
The `ide-integraion.md` document in Gradle core specifies a story ["introduce JavaProject"](https://github.com/gradle/gradle/blob/master/design-docs/ide-integration.md#story---introduce-javaproject-35d). The goal is to make use of the enhancements from there in Buildship.

### Estimate

- 1 day

### Implementation
- Wait for the corresponding Tooling API story to be finished and upgrade Buildship to the latest TAPI/Tooling-commons version
- Add method tooling-commons counterpart for the `JavaProject` class 
- Upgrade tooling-commons used in Buildship
- Synchronize model fields with the related Eclipse workspace project upon refresh
     - update the source compatibility if not the same
     - update target compatibility if not the same

### Test cases
- Update to same source/target compatibility level
- Update to different source/target compatibility level

### Open questions
TBD


## Allow to close a single DefaultGradleConnector instance

### Requested Change

Currently, when calling the static method `DefaultGradleConnector#close`, the entire `ConnectorServices#singletonRegistry` is
closed. Change the behavior such a specific `DefaultGradleConnector` instance can be closed.

### Motivation

Over its life-time, a single `ToolingClient` instance will create one or more GradleConnector instances, depending
on the provided connector parameters. When the tooling client is closed, it must be possible to close exactly those connectors
that the tooling client had created. Currently, it is only possible to close all connectors, regardless of who created them.


## Provide API for source folder includes / excludes

### Requested Change

For each source folder of a given Eclipse project, also provide its includes/excludes patterns.

### Motivation

The source folders and their include/exclude patterns should be fully in-sync between Gradle and the IDE.


## Add new API JavaEnvironment#getAllJvmArguments

### Requested Change

Provide a new API `JavaEnvironment#getAllJvmArguments` (or change the behavior of the existing `JavaEnvironment#getJvmArguments` API)
such that _all_ JVM arguments that make up the target JVM are returned.

### Motivation

In the Gradle project import wizard, we want to display the JVM arguments that make up the target JVM. Currently, the arguments returned by
`JavaEnvironment#getJvmArguments` are quite confusing to the user since they do not represent the total set of arguments that make up the
target JVM. For example, system properties defined by the user in the import wizard are not part of `JavaEnvironment#getJvmArguments`
that are currently shown to the user.


## ~~Add a new API Task#getGroup~~

### Requested Change

Provide a new API `org.gradle.tooling.model.Task#getGroup` that provides the group to which the task belongs.

### Motivation

In the Task View, we want the user to be able to see the tasks grouped by their _group_ attribute. This display is very similar
to how tasks are displayed on the command line.

Since all of the task view is displayed using the task information from the `GradleProject` model, adding the _group_ attribute to the `Task` class is sufficient for Buildship.


## Add new API BuildLauncher#withTaskArguments(Map<String, List<String>)

### Requested Change

Allow to pass task-specific arguments via a new API `BuildLauncher#withTaskArguments(Map<String, List<String>)` where the keys of the map
represent the task names and the values the arguments for the given task name.

### Motivation

Currently, the only way to pass task-specific arguments is by mixing them into the call of `BuildLauncher#forTasks(java.lang.String...)`. Instead,
we need a specific API for task-specific arguments.


## Provide model information about JDK, sourceCompatibility, and targetCompatibility

### Requested Change

For each Java project, provide model information about the Java version to use for compilation and execution. Also provide
model information about the sourceCompatibility and targetCompatibility.

### Motivation

For each project that we set up as a Java project in Eclipse during the import, we need the information about JDK, sourceCompatibility,
and targetCompatibility in order to properly set up the Eclipse project.


## Provide new Plugins Model

### Requested Change

Provide a `Plugins' model that, for a given project, returns the plugins applied to that project. For each plugin, its id, version, and
origin (build, remote repository, etc.) should be available.

### Motivation

We want to show a Gradle Plugins View in Buildship based on the `Plugins` model. We also need the plugins information to decide
what projects to set up as Java projects (and potentially as Groovy projects) in Eclipse.


## Add new EclipseImport model with APIs EclipseImport#getPreImportTasks and EclipseImport#getPostImportTasks

### Requested Change

Extend the Gradle DSL such that it is possible to model what Gradle tasks to run _before_ the consumer does a project import through
the Tooling API and what Gradle tasks to run _after_ the consumer has done a project import through the Tooling API. Provide this information
as part of a new `EclipseImport` model. It is up to the consumer of the Tooling API to make use of that information (or to ignore it).

As an alternative, it could be modeled in the Gradle build that a certain task is run before each model request. This seems to apply
too broadly, though.

### Motivation

It has been a repeated request that a certain task is run _before_ doing a project import into the IDE. For example, to run some code
generation task before importing a project, or to repackage some external library before importing the project (importing as in fetching
a model). And to run a certain task _after_ the import has completed.

We want to ensure that during the import of a Gradle project into Eclipse and during the refresh of a project in Eclipse, some tasks
are run before and after the import, without the IDE user having to trigger or configure these tasks explicitly.


## ~~Allow to listen to build progress and task execution progress~~

### Requested Change

Analogous to the TestProgressListener API, provide new APIs `LongRunningOperation#addBuildProgressListener` and `LongRunningOperation#addTaskProgressListener` to
listen to the progress of running a build (build started/finished, build evaluated, configuration phase started/finished, execution phase started/finished) and
to listen to the progress of the tasks being executed (task started/finished, task uptodate, task skipped, etc.). All events are _strongly typed_ and besides
all of them having a timestamp, they also contain event-specific information, e.g. the name and path of the task being started.

### Motivation

Providing progress visualization about a running build is a key story of Buildship. We already visualize test progress. We need to extend this story
to provide broader visualization of running the build.


## Provide new APIs to run tests by method, by class, or by package

### Requested Change

Provide a new API `BuildLauncher#forTests(TestTarget target)` where `TestTarget` describes either a single test method, a test class, or
a test package. The Tooling API provider figures out to which `Test` task the described test target belongs and runs that task.

### Motivation

As part of the deep integration into Eclipse, the user of Buildship can select in the Project/Package Explorer the test (method, class, package) that
he wants to run. We currently assume that the selected test can be run by the `Test` task named _test_. This is a weak assumption and already falls
apart, for example, when having a separate `Test` task for the integration tests.


## Allow to run tests in debug mode

### Requested Change

Provide an API to run tests in remote debug mode such that the consumer can connect to the remote VM and debug the tests in Eclipse.

### Motivation

Once we can run tests from Eclipse through Gradle, debugging tests through Gradle is the next logical step. This functionality is part
of the deep integration since it allows the user to work with the Debugger UIs that he is already familiar with.


## ~~Make dependencies collection more correct~~

### Requested Change

Fix all known issues related to the dependencies collection returned by the Tooling API.

### Motivation

The dependency resolution process should be as similar as possible when the build is invoked from the Tooling API as when it is
invoked from the command line.


## Notify Tooling API consumer about Gradle build files changes

### Requested Change

Detect changes in files that are part of the Gradle build (build.gradle, gradle.properties, etc.) and notify the
Tooling API consumer via an update event.

### Motivation

When a build file changes, we want to automatically update the project configuration and views in Eclipse. For example, if
a user adds a new task to one of his build files, Buildship needs to be notified about it such that it can automatically
refresh the Gradle Task View.
